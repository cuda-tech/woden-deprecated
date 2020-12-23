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
package tech.cuda.woden.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.service.dao.JobDAO
import tech.cuda.woden.service.dao.MachineDAO
import tech.cuda.woden.service.dao.TaskDAO
import tech.cuda.woden.service.dto.TaskDTO
import tech.cuda.woden.service.exception.NotFoundException
import tech.cuda.woden.service.exception.OperationNotAllowException
import tech.cuda.woden.service.po.dtype.JobStatus
import tech.cuda.woden.service.po.dtype.ScheduleFormat
import tech.cuda.woden.service.po.dtype.SchedulePeriod
import tech.cuda.woden.service.po.dtype.SchedulePriority
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class JobServiceTest : TestWithMaria({
    "根据 ID 查找" {
        val job = JobService.findById(4)
        job shouldNotBe null
        job!!
        job.taskId shouldBe 35
        job.machineId shouldBe 1
        job.status shouldBe JobStatus.READY
        job.hour shouldBe 16
        job.runCount shouldBe 1
        job.minute shouldBe 30
        job.createTime shouldBe "2025-01-10 16:56:13".toLocalDateTime()
        job.updateTime shouldBe "2026-01-26 23:09:37".toLocalDateTime()

        JobService.findById(1) shouldBe null
        JobService.findById(10086) shouldBe null
    }

    "分页查询" {
        val total = 279
        val pageSize = 13
        val queryTimes = total / pageSize + 1
        val lastPageCount = total % pageSize
        for (page in 1..queryTimes) {
            val (jobs, count) = JobService.listing(page, pageSize)
            jobs.size shouldBe if (page == queryTimes) lastPageCount else pageSize
            count shouldBe total
        }
    }

    "按任务 ID 分页查询" {
        val taskId = 3
        val total = 26
        val pageSize = 5
        val queryTimes = total / pageSize + 1
        val lastPageCount = total % pageSize
        for (page in 1..queryTimes) {
            val (jobs, count) = JobService.listing(page, pageSize, taskId = taskId)
            jobs.size shouldBe if (page == queryTimes) lastPageCount else pageSize
            count shouldBe total
            jobs.forEach { it.taskId shouldBe taskId }
        }
    }

    "按执行时间查询" {
        val (jobs, count) = JobService.listing(1, 1000, status = JobStatus.SUCCESS, hour = 5)
        count shouldBe 8
        jobs.map { it.id } shouldContainExactlyInAnyOrder listOf(26, 58, 64, 104, 108, 153, 224, 283)
    }

    "按状态分页查询" {
        val status = JobStatus.SUCCESS
        val total = 110
        val pageSize = 5
        val queryTimes = total / pageSize + 1
        val lastPageCount = total % pageSize
        for (page in 1..queryTimes) {
            val (jobs, count) = JobService.listing(page, pageSize, status = status)
            jobs.size shouldBe if (page == queryTimes) lastPageCount else pageSize
            count shouldBe total
            jobs.forEach { it.status shouldBe status }
        }
    }

    "按创建时间查询" {
        val before = "2020-01-01 00:00:00".toLocalDateTime()
        val after = "2010-01-01 00:00:00".toLocalDateTime()
        JobService.listing(1, 13, before = before).second shouldBe 105
        JobService.listing(1, 13, after = after).second shouldBe 226
        JobService.listing(1, 13, before = before, after = after).second shouldBe 52
    }

    "复合查询" {
        val taskId = 3
        val status = JobStatus.SUCCESS
        JobService.listing(1, 13, taskId = taskId, status = status).second shouldBe 14
        JobService.listing(1, 13, taskId = taskId, status = status, machineId = 15).second shouldBe 2
        JobService.listing(1, 13, taskId = 4, status = status).second shouldBe 0
    }

    "创建作业" {
        val now = LocalDateTime.now()

        // 小时级任务
        JobService.create(TaskService.findById(8)!!).size shouldBe 24
        with(JobService.listing(1, 100, taskId = 8, before = now, after = now)) {
            this.second shouldBe 24
            val jobs = this.first.map { it.hour to it }.toMap()
            val existsJobsTimestamp = (0..23).map { hr ->
                val job = jobs[hr]
                job shouldNotBe null
                job!!
                job.machineId shouldBe null
                job.hour shouldBe hr
                job.minute shouldBe 38
                job.runCount shouldBe 0
                job.status shouldBe JobStatus.INIT
                job.createTime
            }
            // 再次创建只会返回已创建的作业
            Thread.sleep(1000)
            val jobsCreateAgain = JobService.create(TaskService.findById(8)!!)
            jobsCreateAgain.size shouldBe 24
            jobsCreateAgain.forEach {
                it.createTime shouldBe existsJobsTimestamp[it.hour]
            }
        }

        // 非小时级任务
        JobService.create(TaskService.findById(112)!!).size shouldBe 1
        with(JobService.listing(1, 100, taskId = 112, before = now, after = now)) {
            this.second shouldBe 1
            val job = this.first.first()
            job.machineId shouldBe null
            job.status shouldBe JobStatus.INIT
            job.hour shouldBe 16
            job.runCount shouldBe 0
            job.minute shouldBe 59
            Thread.sleep(1000)
            val jobCreateAgain = JobService.create(TaskService.findById(112)!!)
            jobCreateAgain.size shouldBe 1
            jobCreateAgain.first().createTime shouldBe job.createTime
        }

        // 当天不应调度的任务
        JobService.create(TaskService.findById(139)!!).size shouldBe 0

        // 非法操作
        shouldThrow<OperationNotAllowException> {
            JobService.create(TaskService.findById(33)!!)
        }.message shouldBe "调度任务 33 已失效"

        shouldThrow<OperationNotAllowException> {
            JobService.create(TaskDTO(
                id = 1, mirrorId = 1, groupId = 1, name = "", owners = setOf(), args = mapOf(), isSoftFail = false,
                period = SchedulePeriod.DAY, format = ScheduleFormat(year = 2020),  // 非法的格式
                queue = "", priority = SchedulePriority.HIGH, pendingTimeout = 0, runningTimeout = 0, parent = mapOf(),
                children = setOf(), retries = 0, retryDelay = 0, isValid = true, createTime = LocalDateTime.now(),
                updateTime = LocalDateTime.now()
            ))
        }.message shouldBe "调度任务 1 调度时间格式 非法"
    }

    "更新作业" {
        JobService.update(4, status = JobStatus.INIT, machineId = 1, runCount = 123)
        val job = JobService.findById(4)!!
        job.status shouldBe JobStatus.INIT
        job.machineId shouldBe 1
        job.runCount shouldBe 123
        job.updateTime shouldNotBe "2026-01-26 23:09:37".toLocalDateTime()

        shouldThrow<NotFoundException> {
            JobService.update(1, JobStatus.SUCCESS)
        }.message shouldBe "调度作业 1 不存在或已被删除"

        shouldThrow<NotFoundException> {
            JobService.update(10086, JobStatus.SUCCESS)
        }.message shouldBe "调度作业 10086 不存在或已被删除"

        shouldThrow<NotFoundException> {
            JobService.update(4, machineId = 2)
        }.message shouldBe "调度服务器 2 不存在或已被删除"

        shouldThrow<NotFoundException> {
            JobService.update(4, machineId = 247)
        }.message shouldBe "调度服务器 247 不存在或已被删除"
    }

    "Ready状态测试" {

        // RUNNING 状态直接返回 false
        JobService.isReady(JobService.findById(14)!!) shouldBe false

        // READY 状态直接返回 true
        JobService.isReady(JobService.findById(4)!!) shouldBe true

        // 上游不依赖任何任务
        JobService.isReady(JobService.findById(11)!!) shouldBe true

        // 上游为 ONCE 调度
        JobService.isReady(JobService.findById(2)!!) shouldBe true // 归属任务 411, 依赖已完成任务 419
        JobService.isReady(JobService.findById(3)!!) shouldBe false // 归属任务 383, 依赖未完成任务 384

        // 上游为 YEAR 调度
        JobService.isReady(JobService.findById(35)!!) shouldBe true // 归属任务 289, 依赖已完成任务 282
        JobService.isReady(JobService.findById(37)!!) shouldBe false // 归属任务 289, 依赖未完成任务 282

        // 上游为 MONTH 调度
        JobService.isReady(JobService.findById(47)!!) shouldBe true  // 归属任务 356, 依赖于已完成任务 346
        JobService.isReady(JobService.findById(52)!!) shouldBe false  // 归属任务 356, 依赖于未完成任务 346

        // 上游为 WEEK 调度
        JobService.isReady(JobService.findById(60)!!) shouldBe true  // 归属任务 333, 依赖于已完成任务 330
        JobService.isReady(JobService.findById(63)!!) shouldBe false  // 归属任务 333, 依赖于未完成任务 330

        // 上游为 DAY 调度
        JobService.isReady(JobService.findById(71)!!) shouldBe true  // 归属任务 442, 依赖于已完成任务 447
        JobService.isReady(JobService.findById(73)!!) shouldBe false  // 归属任务 442, 依赖于未完成任务 447

        // 上游为 HOUR 调度且当前为 HOUR 调度
        JobService.isReady(JobService.findById(84)!!) shouldBe true  // 归属任务 117, 依赖于 22 时已完成任务 122
        JobService.isReady(JobService.findById(88)!!) shouldBe false  // 归属任务 117, 依赖于 23 时未完成任务 122

        // 上游为 HOUR 调度且当前不为 HOUR 调度
        JobService.isReady(JobService.findById(116)!!) shouldBe true  // 归属任务 244，依赖小时级任务 218
        JobService.isReady(JobService.findById(118)!!) shouldBe false  // 归属任务 244，依赖小时级任务 218
    }

    "可重试状态测试" {
        // 重试次数超限
        JobService.canRetry(5) shouldBe false

        // 允许重试
        JobService.canRetry(3) shouldBe true

        // 作业状态不为 FAILED
        JobService.canRetry(9) shouldBe false // INIT
        JobService.canRetry(4) shouldBe false // READY
        JobService.canRetry(14) shouldBe false // RUNNING
        JobService.canRetry(13) shouldBe false // SUCCESS
        JobService.canRetry(15) shouldBe false // KILLED

        // 作业不存在
        shouldThrow<NotFoundException> {
            JobService.canRetry(351)
        }

        // job 已删除
        shouldThrow<NotFoundException> {
            JobService.canRetry(1)
        }
    }

}, TaskDAO, JobDAO, MachineDAO)