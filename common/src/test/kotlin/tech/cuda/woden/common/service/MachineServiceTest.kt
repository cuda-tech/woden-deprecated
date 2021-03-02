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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.common.service.dao.MachineDAO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.po.dtype.MachineRole
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class MachineServiceTest : TestWithMaria({
    "分页查询" {
        val validMachineCount = 188
        val pageSize = 13
        val queryTimes = validMachineCount / pageSize + 1
        val lastPageMachineCount = validMachineCount % pageSize
        for (page in 1..queryTimes) {
            val (machines, count) = MachineService.listing(page, pageSize)
            machines.size shouldBe if (page == queryTimes) lastPageMachineCount else pageSize
            count shouldBe validMachineCount
        }
    }

    "查询存活的 master" {
        val (machines, count) = MachineService.listingActiveMaster()
        machines.size shouldBe 3
        count shouldBe 3
        machines.map { it.hostname } shouldContainExactlyInAnyOrder listOf("tejxajfq", "nknvleif", "myjweyss")
        machines.map { it.id } shouldContainExactlyInAnyOrder listOf(1, 3, 5)
    }

    "查询存活的 slave" {
        val (machines, count) = MachineService.listingActiveSlave()
        machines.size shouldBe 156
        count shouldBe 156
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validMachineCount = 188
        var pageSize = 13
        var queryTimes = validMachineCount / pageSize + 1
        var lastPageGroupCount = validMachineCount % pageSize
        for (page in 1..queryTimes) {
            with(MachineService.listing(page, pageSize, null)) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
            with(MachineService.listing(page, pageSize, " NULL")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
            with(MachineService.listing(page, pageSize, " ")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
        }

        // 提供一个相似词
        validMachineCount = 58
        pageSize = 5
        queryTimes = validMachineCount / pageSize + 1
        lastPageGroupCount = validMachineCount % pageSize
        for (page in 1..queryTimes) {
            with(MachineService.listing(page, pageSize, " a ")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
            with(MachineService.listing(page, pageSize, " a NULL")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
            with(MachineService.listing(page, pageSize, " null a")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
        }

        // 提供两个相似词
        validMachineCount = 15
        pageSize = 2
        queryTimes = validMachineCount / pageSize + 1
        lastPageGroupCount = validMachineCount % pageSize
        for (page in 1..queryTimes) {
            with(MachineService.listing(page, pageSize, " a b")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
            with(MachineService.listing(page, pageSize, " b a NULL")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
            with(MachineService.listing(page, pageSize, "a null b")) {
                val (machines, count) = this
                machines.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validMachineCount
            }
        }
    }

    "按 id 查找服务器" {
        val machine = MachineService.findById(11)
        machine shouldNotBe null
        machine!!
        machine.hostname shouldBe "nncxrbwz"
        machine.cpuLoad shouldBe 16
        machine.memLoad shouldBe 11
        machine.isActive shouldBe false
        machine.role shouldBe MachineRole.MASTER
        machine.diskUsage shouldBe 46
        machine.createTime shouldBe "2012-03-28 03:42:07".toLocalDateTime()
        machine.updateTime shouldBe "2012-09-03 04:11:46".toLocalDateTime()

        MachineService.findById(12) shouldBe null
        MachineService.findById(300) shouldBe null
    }

    "按 hostname 查找服务器" {
        val machine = MachineService.findByHostname("nknvleif")
        machine shouldNotBe null
        machine!!
        machine.id shouldBe 3
        machine.hostname shouldBe "nknvleif"
        machine.cpuLoad shouldBe 98
        machine.memLoad shouldBe 48
        machine.diskUsage shouldBe 31
        machine.isActive shouldBe true
        machine.role shouldBe MachineRole.MASTER
        machine.createTime shouldBe "2035-11-05 14:17:43".toLocalDateTime()
        machine.updateTime shouldBe "2036-03-31 18:40:59".toLocalDateTime()

        MachineService.findByHostname("mioohisg") shouldBe null
        MachineService.findByHostname("not exists") shouldBe null
    }

    "查询负载最低的服务器" {
        val machine = MachineService.findSlackMachine()
        machine.id shouldBe 58
        machine.hostname shouldBe "kaefkqop"
        machine.memLoad shouldBe 0
        machine.cpuLoad shouldBe 59
    }

    "创建服务器" {
        val machine = MachineService.create("test_create")
        machine.hostname shouldBe "test_create"
        machine.cpuLoad shouldBe 0
        machine.memLoad shouldBe 0
        machine.diskUsage shouldBe 0
        machine.isActive shouldBe true
        machine.role shouldBe MachineRole.SLAVE
        machine.createTime shouldBeLessThanOrEqualTo LocalDateTime.now()
        machine.updateTime shouldBeLessThanOrEqualTo LocalDateTime.now()

        // hostname 已存在
        shouldThrow<DuplicateException> {
            MachineService.create("tejxajfq")
        }.message shouldBe "主机名 tejxajfq 已存在"

    }

    "更新服务器信息" {
        MachineService.update(id = 1, hostname = "new_name")
        var machine = MachineService.findById(1)!!
        machine.hostname shouldBe "new_name"
        val updateTime = machine.updateTime
        updateTime shouldNotBe "2032-06-08 19:36:03".toLocalDateTime()

        MachineService.update(id = 1, cpuLoad = 1, memLoad = 2, diskUsage = 3)
        machine = MachineService.findById(1)!!
        machine.hostname shouldBe "new_name"
        machine.cpuLoad shouldBe 1
        machine.memLoad shouldBe 2
        machine.diskUsage shouldBe 3
        machine.isActive shouldBe true
        machine.role shouldBe MachineRole.MASTER
        machine.updateTime shouldBeGreaterThanOrEqualTo updateTime

        MachineService.update(id = 1, isActive = false, role = MachineRole.SLAVE)
        machine = MachineService.findById(1)!!
        machine.isActive shouldBe false
        machine.role shouldBe MachineRole.SLAVE

        // 已删除
        shouldThrow<NotFoundException> {
            MachineService.update(7, hostname = "anything")
        }.message shouldBe "调度服务器 7 不存在或已被删除"

        // 不存在
        shouldThrow<NotFoundException> {
            MachineService.update(300, hostname = "anything")
        }.message shouldBe "调度服务器 300 不存在或已被删除"

        // hostname 已存在
        shouldThrow<DuplicateException> {
            MachineService.update(1, hostname = "tipwcfcc")
        }.message shouldBe "主机名 tipwcfcc 已存在"
    }

    "删除服务器" {
        MachineService.findById(1) shouldNotBe null
        MachineService.remove(1)
        MachineService.findById(1) shouldBe null

        // 不存在
        shouldThrow<NotFoundException> {
            MachineService.remove(7)
        }.message shouldBe "调度服务器 7 不存在或已被删除"

        // 已删除
        shouldThrow<NotFoundException> {
            MachineService.remove(300)
        }.message shouldBe "调度服务器 300 不存在或已被删除"
    }

}, MachineDAO)