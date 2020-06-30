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
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.add
import tech.cuda.datahub.i18n.I18N
import tech.cuda.datahub.service.dao.JobDAO
import tech.cuda.datahub.service.dto.JobDTO
import tech.cuda.datahub.service.dto.TaskDTO
import tech.cuda.datahub.service.dto.toJobDTO
import tech.cuda.datahub.service.exception.DirtyDataException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.exception.OperationNotAllowException
import tech.cuda.datahub.service.mysql.function.toDate
import tech.cuda.datahub.service.po.JobPO
import tech.cuda.datahub.service.po.dtype.JobStatus
import tech.cuda.datahub.service.po.dtype.SchedulePeriod
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@Suppress("DEPRECATION")
object JobService : Service(JobDAO) {

    /**
     * 通过[id]查找实例
     * 如果找不到或已被删除，则返回 null
     */
    fun findById(id: Int) = find<JobPO>(JobDAO.id eq id and (JobDAO.isRemove eq false))?.toJobDTO()

    /**
     * 分页查询作业信息，结果按创建时间倒序返回
     * 如果提供了[taskId]，则只返回该任务的作业
     * 如果提供了[machineId]，则只返回该机器执行的作业
     * 如果提供了[status]，则只返回对应状态的作业
     * 如果提供了[after]，则只返回创建日期晚于它的记录
     * 如果提供了[before]，则只返回创建日期早于它的记录
     */
    fun listing(
        pageId: Int,
        pageSize: Int,
        taskId: Int? = null,
        machineId: Int? = null,
        status: JobStatus? = null,
        after: LocalDateTime? = null,
        before: LocalDateTime? = null
    ): Pair<List<JobDTO>, Int> {
        val conditions = mutableListOf(JobDAO.isRemove eq false)
        taskId?.let { conditions.add(JobDAO.taskId eq taskId) }
        machineId?.let { conditions.add(JobDAO.machineId eq machineId) }
        status?.let { conditions.add(JobDAO.status eq status) }
        after?.let { conditions.add(JobDAO.createTime.toDate() greaterEq after.toLocalDate()) }
        before?.let { conditions.add(JobDAO.createTime.toDate() lessEq before.toLocalDate()) }

        val (jobs, count) = batch<JobPO>(
            pageId = pageId,
            pageSize = pageSize,
            filter = conditions.reduce { a, b -> a and b },
            orderBy = JobDAO.createTime.desc()
        )
        return jobs.map { it.toJobDTO() } to count
    }

    /**
     * 根据[task]的信息生成当天的调度作业，并返回生成的作业列表
     * 如果任务当天应该调度，并且是小时级任务，则返回 24 个作业，否则返回一个作业
     * 如果任务当天不应该调度，则返回空列表
     * 如果[task]已失效，则抛出 OperationNotAllowException
     * 如果[task]调度时间格式非法，则抛出 OperationNotAllowException
     */
    fun create(task: TaskDTO): List<JobDTO> = Database.global.useTransaction {
        if (!task.isValid) {
            throw OperationNotAllowException(I18N.task, task.id, I18N.invalid)
        }
        if (!task.format.isValid(task.period)) {
            throw OperationNotAllowException(I18N.scheduleFormat, I18N.illegal)
        }
        // 只有当天需要调度的任务才会生成作业, 如果当天的作业已生成，则跳过创建，直接返回
        if (task.format.shouldSchedule(task.period)) {
            val now = LocalDateTime.now()
            val (existsJobs, existsCount) = listing(1, 25, taskId = task.id, after = now, before = now)
            if (task.period != SchedulePeriod.HOUR) { // 非小时任务只会生成一个作业
                val job = JobPO {
                    taskId = task.id
                    machineId = null
                    status = JobStatus.INIT
                    hour = task.format.hour!! // 非小时 hour 一定不为 null
                    minute = task.format.minute
                    isRemove = false
                    createTime = now
                    updateTime = now
                }
                return when (existsCount) {
                    0 -> JobDAO.add(job).run { listOf(job.toJobDTO()) }
                    1 -> existsJobs
                    else -> throw DirtyDataException(I18N.task, task.id, task.period, I18N.job, existsJobs.map { it.id }.joinToString(", "))
                }
            } else { // 小时任务会生成 24 个作业
                return when (existsCount) {
                    0 -> (0..23).map { hr ->
                        val job = JobPO {
                            taskId = task.id
                            machineId = null
                            status = JobStatus.INIT
                            hour = hr
                            minute = task.format.minute
                            isRemove = false
                            createTime = now
                            updateTime = now
                        }
                        JobDAO.add(job)
                        job.toJobDTO()
                    }
                    24 -> existsJobs
                    else -> throw DirtyDataException(I18N.task, task.id, task.period, I18N.job, existsJobs.map { it.id }.joinToString(", "))
                }
            }
        }
        return listOf() // 如果任务当天不应该调度，则直接返回空列表
    }

    /**
     * 更新指定[id]的作业信息
     * 如果指定[id]的作业不存在或已被删除，则抛出 NotFoundException
     * 如果试图更新[machineId]，并且该机器不存在或已被删除，则抛出 NotFoundException
     */
    fun update(id: Int, status: JobStatus? = null, machineId: Int? = null): JobDTO {
        val job = find<JobPO>(JobDAO.id eq id and (JobDAO.isRemove eq false))
            ?: throw NotFoundException(I18N.job, id, I18N.notExistsOrHasBeenRemove)
        status?.let {
            // todo: 作业状态可达性判断
            job.status = status
        }
        machineId?.let {
            MachineService.findById(machineId)
                ?: throw NotFoundException(I18N.machine, machineId, I18N.notExistsOrHasBeenRemove)
            // todo: 机器存活性判断
            job.machineId = machineId
        }
        anyNotNull(status, machineId)?.let {
            job.updateTime = LocalDateTime.now()
            job.flushChanges()
        }
        return job.toJobDTO()
    }

}