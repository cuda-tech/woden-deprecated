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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.datahub.TestWithMaria
import tech.cuda.datahub.service.dao.JobDAO
import tech.cuda.datahub.service.dao.TaskDAO
import tech.cuda.datahub.service.dto.TaskDTO
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.exception.OperationNotAllowException
import tech.cuda.datahub.service.po.dtype.*
import tech.cuda.datahub.toLocalDateTime
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class JobServiceTest : TestWithMaria({
    "根据 ID 查找" {
        val job = JobService.findById(4)
        job shouldNotBe null
        job!!
        job.taskId shouldBe 229
        job.status shouldBe JobStatus.SUCCESS
        job.hour shouldBe 16
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
        val (tasks, count) = JobService.listing(1, 13, taskId = 3)
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

    "按状态分页查询" {
        val status = JobStatus.SUCCESS
        val total = 58
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

    "按时间查询" {
        val before = "2020-01-01 00:00:00".toLocalDateTime()
        val after = "2010-01-01 00:00:00".toLocalDateTime()
        JobService.listing(1, 13, before = before).second shouldBe 105
        JobService.listing(1, 13, after = after).second shouldBe 226
        JobService.listing(1, 13, before = before, after = after).second shouldBe 52
    }

    "复合查询" {
        val taskId = 3
        val status = JobStatus.SUCCESS
        JobService.listing(1, 13, taskId = taskId, status = status).second shouldBe 3
        JobService.listing(1, 13, taskId = 4, status = status).second shouldBe 0
    }

    "创建作业" {
        val now = LocalDateTime.now()

        // 小时级任务
        JobService.create(TaskService.findById(8)!!).size shouldBe 24
        with(JobService.listing(1, 100, taskId = 8, before = now, after = now)) {
            this.second shouldBe 24
            val jobs = this.first.map { it.hour to it }.toMap()
            for (hr in 0..23) {
                val job = jobs[hr]
                job shouldNotBe null
                job!!
                job.hour shouldBe hr
                job.minute shouldBe 38
                job.status shouldBe JobStatus.INIT
            }
        }

        // 非小时级任务
        JobService.create(TaskService.findById(112)!!).size shouldBe 1
        with(JobService.listing(1, 100, taskId = 112, before = now, after = now)) {
            this.second shouldBe 1
            val job = this.first.first()
            job.status shouldBe JobStatus.INIT
            job.hour shouldBe 16
            job.minute shouldBe 59
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
        }.message shouldBe "调度时间格式 非法"
    }

    "更新作业" {

        JobService.update(4, JobStatus.INIT)
        val job = JobService.findById(4)!!
        job.status shouldBe JobStatus.INIT
        job.updateTime shouldNotBe "2026-01-26 23:09:37".toLocalDateTime()

        shouldThrow<NotFoundException> {
            JobService.update(1, JobStatus.SUCCESS)
        }.message shouldBe "调度作业 1 不存在或已被删除"

        shouldThrow<NotFoundException> {
            JobService.update(10086, JobStatus.SUCCESS)
        }.message shouldBe "调度作业 10086 不存在或已被删除"
    }

}, TaskDAO, JobDAO)
