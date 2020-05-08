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
package tech.cuda.datahub.service

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.desc
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.inList
import me.liuwj.ktorm.entity.add
import tech.cuda.datahub.service.dao.TaskDAO
import tech.cuda.datahub.service.dto.TaskDTO
import tech.cuda.datahub.service.dto.toTaskDTO
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.exception.OperationNotAllowException
import tech.cuda.datahub.service.exception.PermissionException
import tech.cuda.datahub.service.i18n.I18N
import tech.cuda.datahub.service.mysql.function.contains
import tech.cuda.datahub.service.po.TaskPO
import tech.cuda.datahub.service.po.dtype.ScheduleDependencyInfo
import tech.cuda.datahub.service.po.dtype.SchedulePeriod
import tech.cuda.datahub.service.po.dtype.SchedulePriority
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object TaskService : Service(TaskDAO) {

    /**
     * 分页查询任务列表
     * 如果提供了[nameLike]，则对任务名进行模糊查询
     * 如果提供了[ownerId]，并且该用户存在且未被删除，则过滤出指定用户负责的任务，否则抛出 NotFoundException
     * 如果提供了[groupId]，并且该项目组存在且未被删除，则过滤出指定项目组的任务，否则抛出 NotFoundException
     * 如果提供了[period]，则过滤出指定调度周期的任务
     * 如果提供了[queue]，则过滤出指定调度队列的任务
     * 如果提供了[isValid]，则过滤出调度生效/不生效的任务
     */
    fun listing(
        page: Int,
        pageSize: Int,
        nameLike: String? = null,
        ownerId: Int? = null,
        period: SchedulePeriod? = null,
        queue: String? = null,
        groupId: Int? = null,
        isValid: Boolean? = null
    ): Pair<List<TaskDTO>, Int> {
        val conditions = mutableListOf(TaskDAO.isRemove eq false)
        ownerId?.let {
            UserService.findById(ownerId) ?: throw NotFoundException(I18N.user, ownerId, I18N.notExistsOrHasBeenRemove)
            conditions.add(TaskDAO.owners.contains(ownerId) eq true)
        }
        period?.let { conditions.add(TaskDAO.period eq period) }
        queue?.let { conditions.add(TaskDAO.queue eq queue) }
        groupId?.let {
            GroupService.findById(groupId)
                ?: throw NotFoundException(I18N.group, groupId, I18N.notExistsOrHasBeenRemove)
            conditions.add(TaskDAO.groupId eq groupId)
        }
        isValid?.let { conditions.add(TaskDAO.isValid eq isValid) }
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
     * 查找指定 id 的任务
     * 如果任务不存在或已被删除，则返回 null
     */
    fun findById(id: Int) = find<TaskPO>(
        where = TaskDAO.isRemove eq false
            and (TaskDAO.id eq id)
    )?.toTaskDTO()

    /**
     * 创建任务，并返回 DTO
     * 如果镜像[mirrorId]不存在或已被删除，则抛出 NotFoundException
     * 如果镜像[mirrorId]对应的文件不存在或已删除，则抛出 NotFoundException
     * 如果[ownerIds]中存在用户没有[mirrorId]所归属的项目组权限，则抛出 PermissionException
     * 如果[ownerIds]中存在用户已被删除或查找不到，则抛出 NotFoundException
     * 如果依赖的父任务[parent]有失效的，则抛出 OperationNotAllowException
     * 如果依赖的父任务[parent]有被删除的或已失效的，则抛出 NotFoundException
     */
    fun create(
        mirrorId: Int,
        name: String,
        ownerIds: Set<Int>,
        args: Map<String, String> = mapOf(),
        isSoftFail: Boolean = false,
        period: SchedulePeriod,
        queue: String,
        priority: SchedulePriority = SchedulePriority.VERY_LOW,
        pendingTimeout: Int = Int.MAX_VALUE,
        runningTimeout: Int = Int.MAX_VALUE,
        parent: Map<Int, ScheduleDependencyInfo>,
        retries: Int = 0,
        retryDelay: Int = 5
    ): TaskDTO = Database.global.useTransaction {
        // 查找项目组 ID
        val mirror = FileMirrorService.findById(mirrorId)
            ?: throw NotFoundException(I18N.fileMirror, mirrorId, I18N.notExistsOrHasBeenRemove)
        val file = FileService.findById(mirror.fileId)
            ?: throw NotFoundException(I18N.file, mirror.fileId, I18N.notExistsOrHasBeenRemove)
        val groupId = file.groupId

        // 检查用户权限
        ownerIds.forEach {
            val user = UserService.findById(it) ?: throw NotFoundException(I18N.user, it, I18N.notExistsOrHasBeenRemove)
            if (!user.groups.contains(groupId)) throw PermissionException(I18N.user, it, I18N.notBelongTo, I18N.group, groupId)
        }

        // 校验依赖的父任务
        val parentTasks = parent.keys.map {
            val parentTask = find<TaskPO>(
                where = TaskDAO.isRemove eq false
                    and (TaskDAO.id eq it)
            ) ?: throw NotFoundException(I18N.parentTask, it, I18N.notExistsOrHasBeenRemove)
            if (!parentTask.isValid) throw OperationNotAllowException(I18N.parentTask, it, I18N.invalid, ",", I18N.dependencyNotAllow)
            parentTask
        }

        val task = TaskPO {
            this.mirrorId = mirrorId
            this.groupId = groupId
            this.name = name
            this.owners = ownerIds
            this.args = args
            this.isSoftFail = isSoftFail
            this.period = period
            this.queue = queue
            this.priority = priority
            this.pendingTimeout = pendingTimeout
            this.runningTimeout = runningTimeout
            this.parent = parent
            this.children = setOf()
            this.retries = retries
            this.retryDelay = retryDelay
            this.isValid = true
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
        }.also { TaskDAO.add(it) }

        // 更新父任务的子任务字段
        parentTasks.forEach {
            it.children = it.children.plus(task.id)
            it.flushChanges()
        }
        return task.toTaskDTO()
    }

    /**
     * 更新指定任务[id]的信息
     * 如果任务[id]不存在或已删除，则抛出 NotFoundException
     * 如果试图更新[mirrorId]，且镜像/文件不存在/已删除，则抛出 NotFoundException
     * 如果试图更新[mirrorId]，且镜像归属的文件跟旧镜像归属的文件不是同一个，则抛出 OperationNotAllowException
     * 如果试图更新[ownerIds]，且列表中存在用户查找不到，则抛出 NotFoundException
     * 如果试图更新[ownerIds]，且列表中存在没有该任务归属项目组的用户，则抛出 PermissionException
     * 如果试图更新[parent]，且列表中存在已删除的或不存在的任务，则抛出 NotFoundException
     * 如果试图更新[parent]，且列表中存在失效的任务，则抛出 IllegalArgumentException
     * 如果试图更新[isValid]为 false，且子任务存在未失效的任务，则抛出 IllegalArgumentException
     */
    fun update(
        id: Int,
        mirrorId: Int? = null,
        name: String? = null,
        ownerIds: Set<Int>? = null,
        args: Map<String, Any>? = null,
        isSoftFail: Boolean? = null,
        period: SchedulePeriod? = null,
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
            where = TaskDAO.isRemove eq false
                and (TaskDAO.id eq id)
        ) ?: throw NotFoundException(I18N.task, id, I18N.notExistsOrHasBeenRemove)
        mirrorId?.let {
            val mirror = FileMirrorService.findById(mirrorId)
                ?: throw NotFoundException(I18N.fileMirror, mirrorId, I18N.notExistsOrHasBeenRemove)
            val file = FileService.findById(mirror.fileId)
                ?: throw NotFoundException(I18N.file, mirror.fileId, I18N.notExistsOrHasBeenRemove)
            if (file.id != FileMirrorService.findById(task.mirrorId)?.fileId) throw OperationNotAllowException(I18N.crossFileUpdateMirrorNotAllow)
            task.mirrorId = mirrorId
        }
        name?.let { task.name = name }
        ownerIds?.let {
            it.forEach { userId ->
                val user = UserService.findById(userId)
                    ?: throw NotFoundException(I18N.user, userId, I18N.notExistsOrHasBeenRemove)
                if (!user.groups.contains(task.groupId)) throw PermissionException(I18N.user, userId, I18N.notBelongTo, I18N.group, task.groupId)
            }
            task.owners = ownerIds
        }
        args?.let { task.args = args }
        isSoftFail?.let { task.isSoftFail = isSoftFail }
        period?.let { task.period = period }
        queue?.let { task.queue = queue }
        priority?.let { task.priority = priority }
        pendingTimeout?.let { task.pendingTimeout = pendingTimeout }
        runningTimeout?.let { task.runningTimeout = runningTimeout }
        parent?.let {
            // 校验依赖的父任务，并绑定 children
            parent.keys.forEach {
                val parentTask = find<TaskPO>(
                    where = TaskDAO.isRemove eq false
                        and (TaskDAO.id eq it)
                ) ?: throw NotFoundException(I18N.parentTask, it, I18N.notExistsOrHasBeenRemove)
                if (!parentTask.isValid) throw OperationNotAllowException(I18N.parentTask, it, I18N.invalid, ",", I18N.dependencyNotAllow)
                parentTask.children = parentTask.children.plus(task.id)
                parentTask.flushChanges()
            }

            // 解除原来的父任务 children 绑定
            task.parent.keys.forEach {
                with(find<TaskPO>(where = TaskDAO.isRemove eq false and (TaskDAO.id eq it))!!) {
                    this.children = this.children.minus(task.id)
                    this.flushChanges()
                }
            }

            // 绑定新的父任务
            task.parent = parent
        }
        retries?.let { task.retries = retries }
        retryDelay?.let { task.retryDelay = retryDelay }
        isValid?.let {
            // 如果失效任务，则需要确保所有的子任务均失效
            if (isValid == false) {
                task.children.forEach {
                    if (findById(it)?.isValid == true) throw OperationNotAllowException(I18N.childrenTask, it, I18N.isValid, ",", I18N.parentTask, I18N.invalidNotAllow)
                }
            }
            task.isValid = isValid
        }
        anyNotNull(mirrorId, name, ownerIds, args, isSoftFail,
            period, queue, priority, pendingTimeout, runningTimeout,
            parent, retries, retryDelay, isValid)?.let {
            task.updateTime = LocalDateTime.now()
            task.flushChanges()
        }
        return task.toTaskDTO()
    }

    /**
     * 删除指定[id]的任务
     * 如果任务[id]不存在或已被删除，则抛出 NotFoundException
     * 如果任务[id]处于调度生效状态，则抛出，则抛出 OperationNotAllowException
     * 如果任务[id]的子任务存在未失效的子任务，则抛出 OperationNotAllowException
     */
    fun remove(id: Int) = Database.global.useTransaction {
        val task = find<TaskPO>(
            where = TaskDAO.isRemove eq false
                and (TaskDAO.id eq id)
        ) ?: throw NotFoundException(I18N.task, id, I18N.notExistsOrHasBeenRemove)
        if (task.isValid) throw OperationNotAllowException(I18N.task, I18N.isValid, ",", I18N.removeNotAllow)
        if (batch<TaskPO>(filter = TaskDAO.isRemove eq false
                and (TaskDAO.id.inList(task.children) eq true)
                and (TaskDAO.isValid eq true)).second > 0) throw OperationNotAllowException(I18N.childrenTask, I18N.isValid, ",", I18N.removeNotAllow)
        task.isRemove = true
        task.updateTime = LocalDateTime.now()
        task.flushChanges()
    }
}