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
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.desc
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.global.add
import me.liuwj.ktorm.global.global
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.dao.InstanceDAO
import tech.cuda.woden.common.service.dto.InstanceDTO
import tech.cuda.woden.common.service.dto.toInstanceDTO
import tech.cuda.woden.common.service.dto.JobDTO
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.exception.OperationNotAllowException
import tech.cuda.woden.common.service.po.InstancePO
import tech.cuda.woden.common.service.po.dtype.InstanceStatus
import tech.cuda.woden.common.service.po.dtype.JobStatus
import tech.cuda.woden.common.utils.Checker
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object InstanceService : Service(InstanceDAO) {
    /**
     * 通过[id]查找实例
     * 如果找不到或已被删除，则返回 null
     */
    fun findById(id: Int) = find<InstancePO>(InstanceDAO.id eq id and (InstanceDAO.isRemove eq false))?.toInstanceDTO()

    /**
     * 分页查询实例，按创建时间降序返回
     * 如果提供了[jobId]，则只返回该任务的作业
     * 如果提供了[status]，则只返回对应状态的作业
     */
    fun listing(pageId: Int, pageSize: Int, jobId: Int? = null, status: InstanceStatus? = null): Pair<List<InstanceDTO>, Int> {
        val conditions = mutableListOf(InstanceDAO.isRemove eq false)
        jobId?.let { conditions.add(InstanceDAO.jobId eq jobId) }
        status?.let { conditions.add(InstanceDAO.status eq status) }

        val (instances, count) = batch<InstancePO>(
            pageId = pageId,
            pageSize = pageSize,
            filter = conditions.reduce { a, b -> a and b },
            orderBy = InstanceDAO.createTime.desc()
        )
        return instances.map { it.toInstanceDTO() } to count
    }

    /**
     * 根据[job]的信息一个调度实例并返回
     * 如果[job]的状态不为 READY，则抛出 OperationNotAllowException
     */
    fun create(job: JobDTO): InstanceDTO = Database.global.useTransaction {
        if (job.status != JobStatus.READY) {
            throw OperationNotAllowException(I18N.job, job.id, I18N.status, job.status, I18N.createInstanceNotAllow)
        }
        val instance = InstancePO {
            jobId = job.id
            status = InstanceStatus.RUNNING
            log = ""
            isRemove = false
            createTime = LocalDateTime.now()
            updateTime = LocalDateTime.now()
        }
        InstanceDAO.add(instance)
        return instance.toInstanceDTO()
    }

    /**
     * 更新指定实例[id]的信息
     * 如果指定[id]的实例不存在或已被删除，则抛出 NotFoundException
     * 为了保证状态的单向性，状态只能从 Running -> Success | Failed
     * 因此如果试图将状态[status]更新为 Running，或者实例[id]的状态不为 Running， 则抛出 OperationNotAllowException
     */
    fun update(id: Int, status: InstanceStatus? = null, log: String? = null): InstanceDTO = Database.global.useTransaction {
        val instance = find<InstancePO>(InstanceDAO.id eq id and (InstanceDAO.isRemove eq false))
            ?: throw NotFoundException(I18N.instance, I18N.notExistsOrHasBeenRemove)
        status?.let {
            if (status == InstanceStatus.RUNNING || instance.status != InstanceStatus.RUNNING) {
                throw OperationNotAllowException(I18N.instance, id, I18N.status, instance.status, I18N.canNotUpdateTo, status)
            }
            instance.status = status
        }
        log?.let {
            instance.log = log
        }
        anyNotNull(status, log)?.let {
            instance.updateTime = LocalDateTime.now()
            instance.flushChanges()
        }
        return instance.toInstanceDTO()
    }

    /**
     * 清理 ID 为[id]或者归属作业 ID 为[jobId]的实例
     * 如果[id]和[jobId]都没有指定，则抛出异常
     */
    fun remove(id: Int? = null, jobId: Int? = null) = Database.global.useTransaction {
        if (Checker.allNull(id, jobId)) {
            throw OperationNotAllowException()
        }
        val conditions = mutableListOf(InstanceDAO.isRemove eq false)
        id?.let { conditions.add(InstanceDAO.id eq id) }
        jobId?.let { conditions.add(InstanceDAO.jobId eq jobId) }
        val (instances, count) = batch<InstancePO>(filter = conditions.reduce { a, b -> a and b })
        val now = LocalDateTime.now()
        instances.forEach {
            it.isRemove = true
            it.updateTime = now
            it.flushChanges()
        }
    }

    /**
     * 对指定[id]的实例日志追加[logBuffer]
     * 如果指定[id]的实例不存在或已被删除，则抛出 NotFoundException
     * 如果指定[id]的实例状态不为 Running，则抛出 OperationNotAllowException
     */
    fun appendLog(id: Int, logBuffer: String): InstanceDTO = Database.global.useTransaction {
        val instance = find<InstancePO>(InstanceDAO.id eq id and (InstanceDAO.isRemove eq false))
            ?: throw NotFoundException(I18N.instance, id, I18N.notExistsOrHasBeenRemove)
        if (instance.status != InstanceStatus.RUNNING) {
            throw OperationNotAllowException(I18N.instance, instance.id, I18N.status, instance.status, I18N.updateNotAllow)
        }
        instance.log = instance.log + logBuffer
        instance.updateTime = LocalDateTime.now()
        instance.flushChanges()
        return instance.toInstanceDTO()
    }

}