/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.cuda.woden.common.service

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.global.*
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.dao.TaskDAO
import tech.cuda.woden.common.service.dao.TaskDependencyDAO
import tech.cuda.woden.common.service.dto.TaskDTO
import tech.cuda.woden.common.service.dto.toTaskDTO
import tech.cuda.woden.common.service.enum.TaskType
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.exception.OperationNotAllowException
import tech.cuda.woden.common.service.po.TaskDependencyPO
import tech.cuda.woden.common.service.po.TaskPO
import tech.cuda.woden.common.service.po.dtype.ScheduleDependencyInfo
import tech.cuda.woden.common.service.po.dtype.ScheduleFormat
import tech.cuda.woden.common.service.po.dtype.SchedulePeriod
import tech.cuda.woden.common.service.po.dtype.SchedulePriority
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object TaskService : Service(TaskDAO) {

    /**
     * 分页查询任务列表
     * 如果提供了[nameLike]，则对任务名进行模糊查询
     * 如果提供了[ownerId]，则过滤出指定用户负责的任务
     * 如果提供了[teamId]，则过滤出指定项目组的任务
     * 如果提供了[period]，则过滤出指定调度周期的任务
     * 如果提供了[queue]，则过滤出指定调度队列的任务
     * 如果提供了[isValid]，则过滤出调度生效/不生效的任务
     */
    fun listing(
        page: Int = 1,
        pageSize: Int = Int.MAX_VALUE,
        nameLike: String? = null,
        ownerId: Int? = null,
        period: SchedulePeriod? = null,
        queue: String? = null,
        teamId: Int? = null,
        isValid: Boolean? = null,
        ids: List<Int>? = null
    ): Pair<List<TaskDTO>, Int> {
        val conditions = mutableListOf(TaskDAO.isRemove eq false)
        ownerId?.let { conditions.add(TaskDAO.ownerId eq ownerId) }
        period?.let { conditions.add(TaskDAO.period eq period) }
        queue?.let { conditions.add(TaskDAO.queue eq queue) }
        teamId?.let {
            val team = TeamService.findById(teamId)
            require(team != null)
            conditions.add(TaskDAO.filePath.like("/${team.name}/%"))
        }
        isValid?.let { conditions.add(TaskDAO.isValid eq isValid) }
        ids?.let { conditions.add(TaskDAO.id.inList(ids) eq true) }
        val (tasks, count) = batch<TaskPO>(
            pageId = page,
            pageSize = pageSize,
            filter = conditions.reduce { a, b -> a and b },
            like = TaskDAO.name.match(nameLike),
            orderBy = TaskDAO.id.desc()
        )
        return tasks.map { it.toTaskDTO() } to count
    }

    /**
     * 查询任务[taskId]的子任务
     */
    fun listingChildren(taskId: Int): List<TaskDTO> {
        val downstreamDependencies = TaskDependencyDAO.select()
            .where { (TaskDependencyDAO.isRemove eq false) and (TaskDependencyDAO.parentId eq taskId) }
            .map { TaskDependencyDAO.createEntity(it) }
        return if (downstreamDependencies.isEmpty()) {
            listOf()
        } else {
            TaskDAO.select()
                .where { (TaskDAO.isRemove eq false) and (TaskDAO.id.inList(downstreamDependencies.map { it.childId }) eq true) }
                .map { TaskDAO.createEntity(it).toTaskDTO() }
        }
    }


    /**
     * 查询任务[taskId]的父任务
     */
    fun listingParent(taskId: Int): List<TaskDTO> {
        val upstreamDependencies = TaskDependencyDAO.select()
            .where { (TaskDependencyDAO.isRemove eq false) and (TaskDependencyDAO.childId eq taskId) }
            .map { TaskDependencyDAO.createEntity(it) }
        return if (upstreamDependencies.isEmpty()) {
            listOf()
        } else {
            TaskDAO.select()
                .where { (TaskDAO.isRemove eq false) and (TaskDAO.id.inList(upstreamDependencies.map { it.parentId }) eq true) }
                .map { TaskDAO.createEntity(it).toTaskDTO() }
        }
    }


    /**
     * 查找指定 id 的任务
     * 如果任务不存在或已被删除，则返回 null
     */
    fun findById(id: Int) = find<TaskPO>(where = TaskDAO.isRemove eq false and (TaskDAO.id eq id))?.toTaskDTO()


    private fun checkParentAllValid(parentIds: Set<Int>): List<TaskDTO> {
        val parentTasks = listing(ids = parentIds.toList()).first
        if (parentTasks.size != parentIds.size) {
            throw NotFoundException(
                I18N.parentTask,
                (parentIds - parentTasks.map { it.id }).joinToString(","),
                I18N.notExistsOrHasBeenRemove
            )
        }
        val invalidIds = parentTasks.filter { !it.isValid }.map { it.id }
        if (invalidIds.isNotEmpty()) {
            throw OperationNotAllowException(
                I18N.parentTask,
                invalidIds.joinToString(","),
                I18N.invalid,
                ",",
                I18N.dependencyNotAllow
            )
        }
        return parentTasks
    }

    /**
     * 通过[filePath]的后缀判断任务类型
     */
    fun getTaskTypeByFilePath(filePath: String): TaskType {
        val fileName = filePath.split("/").last().also {
            require(it.contains(".")) { "suffix missing" }
        }
        val suffix = fileName.split(".").last()
        return TaskType.values().find { it.suffix == suffix }
            ?: throw IllegalArgumentException("unsupported suffix $suffix")
    }

    /**
     * 创建任务，并返回 DTO
     * 如果[ownerId]用户没有项目组权限，则抛出 PermissionException
     * 如果[ownerId]用户已被删除或查找不到，则抛出 NotFoundException
     * 如果依赖的父任务[parent]有失效的，则抛出 OperationNotAllowException
     * 如果依赖的父任务[parent]有被删除的或已失效的，则抛出 NotFoundException
     * 如果调度格式[format]非法，则抛出 OperationNotAllowException
     */
    fun create(
        filePath: String,
        name: String,
        ownerId: Int,
        args: Map<String, String> = mapOf(),
        isSoftFail: Boolean = false,
        period: SchedulePeriod,
        format: ScheduleFormat,
        queue: String,
        priority: SchedulePriority = SchedulePriority.VERY_LOW,
        pendingTimeout: Int = Int.MAX_VALUE,
        runningTimeout: Int = Int.MAX_VALUE,
        parent: Map<Int, ScheduleDependencyInfo>,
        retries: Int = 0,
        retryDelay: Int = 5
    ): TaskDTO = Database.global.useTransaction {
        format.requireValid(period)
        val team = TeamService.findByFilePath(filePath)
        require(team != null) { "team ${TeamService.findByFilePath(filePath)} not exists" }
        PersonService.requireExists(ownerId).requireTeamMember(ownerId, team.id)
        checkParentAllValid(parent.keys)

        // 创建任务及依赖
        val now = LocalDateTime.now()
        val task = TaskPO {
            this.filePath = filePath
            this.name = name
            this.ownerId = ownerId
            this.args = args
            this.isSoftFail = isSoftFail
            this.period = period
            this.format = format
            this.queue = queue
            this.priority = priority
            this.pendingTimeout = pendingTimeout
            this.runningTimeout = runningTimeout
            this.retries = retries
            this.retryDelay = retryDelay
            this.isValid = true
            this.isRemove = false
            this.createTime = now
            this.updateTime = now
        }.also { t -> TaskDAO.addEntity(t) }

        parent.map { (parentId, dependency) ->
            TaskDependencyDAO.addEntity(
                TaskDependencyPO {
                    this.parentId = parentId
                    this.childId = task.id
                    this.waitTimeout = dependency.waitTimeout
                    this.offsetDay = dependency.offsetDay
                    this.isRemove = false
                    this.createTime = now
                    this.updateTime = now
                }
            )
        }
        return task.toTaskDTO()
    }

    /**
     * 更新指定任务[id]的信息
     * 如果任务[id]不存在或已删除，则抛出 NotFoundException
     * 如果试图更新[ownerId]，且用户查找不到，则抛出 NotFoundException
     * 如果试图更新[ownerId]，且用户不归属于任务的项目组，则抛出 PermissionException
     * 如果试图更新[parent]，且列表中存在已删除的或不存在的任务，则抛出 NotFoundException
     * 如果试图更新[parent]，且列表中存在失效的任务，则抛出 IllegalArgumentException
     * 如果试图更新[period]，且没有提供[format]或[format]非法，则抛出 OperationNotAllowException
     * 如果试图更新[format]，且格式非法，则抛出 OperationNotAllowException
     */
    fun update(
        id: Int,
        name: String? = null,
        ownerId: Int? = null,
        args: Map<String, Any>? = null,
        isSoftFail: Boolean? = null,
        period: SchedulePeriod? = null,
        format: ScheduleFormat? = null,
        queue: String? = null,
        priority: SchedulePriority? = null,
        pendingTimeout: Int? = null,
        runningTimeout: Int? = null,
        parent: Map<Int, ScheduleDependencyInfo>? = null,
        retries: Int? = null,
        retryDelay: Int? = null,
        isValid: Boolean? = null
    ): TaskDTO = Database.global.useTransaction {
        val task = find<TaskPO>(
            where = TaskDAO.isRemove eq false and (TaskDAO.id eq id)
        ) ?: throw NotFoundException(I18N.task, id, I18N.notExistsOrHasBeenRemove)
        val now = LocalDateTime.now()
        name?.let { task.name = name }
        ownerId?.let {
            val team = TeamService.findByFilePath(task.filePath)
            require(team != null)
            PersonService.requireExists(it).requireTeamMember(it, teamId = team.id)
        }
        args?.let { task.args = args }
        isSoftFail?.let { task.isSoftFail = isSoftFail }
        when {
            period != null && format == null -> throw OperationNotAllowException(I18N.scheduleFormat, I18N.missing)
            period == null && format != null -> {
                format.requireValid(task.period)
                task.format = format
            }
            period != null && format != null -> {
                format.requireValid(period)
                task.period = period
                task.format = format
            }
        }
        queue?.let { task.queue = queue }
        priority?.let { task.priority = priority }
        pendingTimeout?.let { task.pendingTimeout = pendingTimeout }
        runningTimeout?.let { task.runningTimeout = runningTimeout }
        parent?.let {
            val targetParentTask = checkParentAllValid(parent.keys).map { it.id }
            val currentParentTask = listingParent(task.id).map { it.id }
            val toBeRemove = currentParentTask - targetParentTask
            val toBeInsert = targetParentTask - currentParentTask
            val toBeUpdate = currentParentTask.filter { targetParentTask.contains(it) }
            TaskDependencyDAO.update {
                set(it.isRemove, true)
                set(it.updateTime, now)
                where {
                    (it.parentId.inList(toBeRemove) eq true) and (it.childId eq task.id)
                }
            }
            toBeInsert.forEach {
                TaskDependencyDAO.addEntity(
                    TaskDependencyPO {
                        this.parentId = it
                        this.childId = task.id
                        this.waitTimeout = parent[it]!!.waitTimeout
                        this.offsetDay = parent[it]!!.offsetDay
                        this.isRemove = false
                        this.createTime = now
                        this.updateTime = now
                    }
                )
            }
            toBeUpdate.forEach { parentId ->
                TaskDependencyDAO.update {
                    set(it.waitTimeout, parent[parentId]!!.waitTimeout)
                    set(it.offsetDay, parent[parentId]!!.offsetDay)
                    set(it.updateTime, now)
                    where {
                        (it.parentId eq parentId) and (it.childId eq task.id)
                    }
                }
            }
        }
        retries?.let { task.retries = retries }
        retryDelay?.let { task.retryDelay = retryDelay }
        isValid?.let { task.isValid = isValid }
        anyNotNull(
            name, ownerId, args, isSoftFail,
            period, queue, priority, pendingTimeout, runningTimeout,
            retries, retryDelay, isValid
        )?.let {
            task.updateTime = LocalDateTime.now()
            task.flushChanges()
        }
        return task.toTaskDTO()
    }

    /**
     * 删除指定[id]的任务，并清理它归属的任务和实例
     * 如果任务[id]不存在或已被删除，则抛出 NotFoundException
     * 如果任务[id]处于调度生效状态，则抛出，则抛出 OperationNotAllowException
     * 如果任务[id]的子任务存在未失效的子任务，则抛出 OperationNotAllowException
     */
    fun remove(id: Int) = Database.global.useTransaction {
        val task = find<TaskPO>(
            where = TaskDAO.isRemove eq false and (TaskDAO.id eq id)
        ) ?: throw NotFoundException(I18N.task, id, I18N.notExistsOrHasBeenRemove)
        if (task.isValid) throw OperationNotAllowException(I18N.task, I18N.isValid, ",", I18N.removeNotAllow)

        val validChildren = listingChildren(task.id).filter { it.isValid }.map { it.id }
        if (validChildren.isNotEmpty()) {
            throw OperationNotAllowException(
                I18N.childrenTask,
                validChildren.joinToString(","),
                I18N.isValid,
                ",",
                I18N.removeNotAllow
            )
        }
        val now = LocalDateTime.now()

        // 清理归属的作业
        JobService.remove(taskId = task.id)

        // 最后清理任务
        task.isRemove = true
        task.updateTime = now
        task.flushChanges()
    }
}