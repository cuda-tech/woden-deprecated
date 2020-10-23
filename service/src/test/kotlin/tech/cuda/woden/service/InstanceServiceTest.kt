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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.service.dao.InstanceDAO
import tech.cuda.woden.service.dao.JobDAO
import tech.cuda.woden.service.exception.NotFoundException
import tech.cuda.woden.service.exception.OperationNotAllowException
import tech.cuda.woden.service.po.dtype.InstanceStatus

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class InstanceServiceTest : TestWithMaria({
    "根据 ID 查找" {
        InstanceService.findById(3) shouldBe null
        InstanceService.findById(458) shouldBe null
        val instance = InstanceService.findById(1)
        instance shouldNotBe null
        instance!!
        instance.jobId shouldBe 36
        instance.status shouldBe InstanceStatus.FAILED
        instance.log shouldBe "ynqlypzs"
        instance.createTime shouldBe "2017-02-20 21:27:27".toLocalDateTime()
        instance.updateTime shouldBe "2020-03-31 23:31:16".toLocalDateTime()
    }

    "分页查询" {
        val total = 362
        val pageSize = 13
        val queryTimes = total / pageSize + 1
        val lastPageCount = total % pageSize
        for (page in 1..queryTimes) {
            val (instances, count) = InstanceService.listing(page, pageSize)
            instances.size shouldBe if (page == queryTimes) lastPageCount else pageSize
            count shouldBe total
        }
    }

    "根据 job ID 批量查询" {
        val total = 11
        val pageSize = 3
        val queryTimes = total / pageSize + 1
        val lastPageCount = total % pageSize
        for (page in 1..queryTimes) {
            val (instances, count) = InstanceService.listing(page, pageSize, jobId = 3)
            instances.size shouldBe if (page == queryTimes) lastPageCount else pageSize
            count shouldBe total
        }
        InstanceService.listing(1, 100, jobId = 3, status = InstanceStatus.RUNNING).second shouldBe 3
    }

    "创建实例" {
        val instance = InstanceService.create(JobService.findById(20)!!)
        instance.id shouldBe 458
        instance.jobId shouldBe 20
        instance.status shouldBe InstanceStatus.RUNNING
        instance.log shouldBe ""

        shouldThrow<OperationNotAllowException> {
            InstanceService.create(JobService.findById(9)!!)
        }.message shouldBe "调度作业 9 状态 INIT 禁止创建调度实例"

        shouldThrow<OperationNotAllowException> {
            InstanceService.create(JobService.findById(2)!!)
        }.message shouldBe "调度作业 2 状态 FAILED 禁止创建调度实例"

        shouldThrow<OperationNotAllowException> {
            InstanceService.create(JobService.findById(13)!!)
        }.message shouldBe "调度作业 13 状态 SUCCESS 禁止创建调度实例"

        shouldThrow<OperationNotAllowException> {
            InstanceService.create(JobService.findById(15)!!)
        }.message shouldBe "调度作业 15 状态 KILLED 禁止创建调度实例"

        shouldThrow<OperationNotAllowException> {
            InstanceService.create(JobService.findById(23)!!)
        }.message shouldBe "调度作业 23 状态 SUCCESS 禁止创建调度实例"
    }

    "更新实例" {
        // RUNNING -> SUCCESS
        with(InstanceService.findById(4)!!) {
            this.status shouldBe InstanceStatus.RUNNING
            this.updateTime shouldBe "2008-05-07 09:46:51".toLocalDateTime()
        }
        with(InstanceService.update(4, InstanceStatus.SUCCESS)) {
            this.status shouldBe InstanceStatus.SUCCESS
            this.updateTime shouldNotBe "2008-05-07 09:46:51".toLocalDateTime()
        }
        with(InstanceService.findById(4)!!) {
            this.status shouldBe InstanceStatus.SUCCESS
            this.updateTime shouldNotBe "2008-05-07 09:46:51".toLocalDateTime()
        }

        // RUNNING -> FAILED
        with(InstanceService.findById(6)!!) {
            this.status shouldBe InstanceStatus.RUNNING
            this.updateTime shouldBe "2031-08-14 05:57:33".toLocalDateTime()
        }
        with(InstanceService.update(6, InstanceStatus.FAILED)) {
            this.status shouldBe InstanceStatus.FAILED
            this.updateTime shouldNotBe "2031-08-14 05:57:33".toLocalDateTime()
        }
        with(InstanceService.findById(6)!!) {
            this.status shouldBe InstanceStatus.FAILED
            this.updateTime shouldNotBe "2031-08-14 05:57:33".toLocalDateTime()
        }

        // 更新 log
        with(InstanceService.findById(9)!!) {
            this.status shouldBe InstanceStatus.RUNNING
            this.log shouldBe "jqgonydh"
            this.updateTime shouldBe "2028-09-17 16:27:27".toLocalDateTime()
        }
        InstanceService.update(9, log = "update log")
        with(InstanceService.findById(9)!!) {
            this.status shouldBe InstanceStatus.RUNNING
            this.log shouldBe "update log"
            this.updateTime shouldNotBe "2028-09-17 16:27:27".toLocalDateTime()
        }


        shouldThrow<OperationNotAllowException> {
            InstanceService.update(4, InstanceStatus.FAILED)
        }.message shouldBe "调度实例 4 状态 SUCCESS 禁止更新为 FAILED"

        shouldThrow<OperationNotAllowException> {
            InstanceService.update(6, InstanceStatus.SUCCESS)
        }.message shouldBe "调度实例 6 状态 FAILED 禁止更新为 SUCCESS"

        shouldThrow<OperationNotAllowException> {
            InstanceService.update(9, InstanceStatus.RUNNING)
        }.message shouldBe "调度实例 9 状态 RUNNING 禁止更新为 RUNNING"
    }

    "追加日志" {
        with(InstanceService.findById(4)!!) {
            this.log shouldBe "aaihdphh"
            this.updateTime shouldBe "2008-05-07 09:46:51".toLocalDateTime()
        }
        with(InstanceService.appendLog(4, "anything")) {
            this.log shouldBe "aaihdphhanything"
            this.updateTime shouldNotBe "2008-05-07 09:46:51".toLocalDateTime()
        }
        with(InstanceService.findById(4)!!) {
            this.log shouldBe "aaihdphhanything"
            this.updateTime shouldNotBe "2008-05-07 09:46:51".toLocalDateTime()
        }

        shouldThrow<OperationNotAllowException> {
            InstanceService.appendLog(447, "anything")
        }.message shouldBe "调度实例 447 状态 SUCCESS 禁止更新"

        shouldThrow<OperationNotAllowException> {
            InstanceService.appendLog(441, "anything")
        }.message shouldBe "调度实例 441 状态 FAILED 禁止更新"

        shouldThrow<NotFoundException> {
            InstanceService.appendLog(3, "anything")
        }.message shouldBe "调度实例 3 不存在或已被删除"

        shouldThrow<NotFoundException> {
            InstanceService.appendLog(458, "anything")
        }.message shouldBe "调度实例 458 不存在或已被删除"
    }

}, InstanceDAO, JobDAO)