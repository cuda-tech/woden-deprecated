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
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.datahub.TestWithMaria
import tech.cuda.datahub.service.dao.MachineDAO
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.toLocalDateTime
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
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
        machine.ip shouldBe "237.223.177.192"
        machine.mac shouldBe "7E-C5-BA-DE-97-6F"
        machine.cpuLoad shouldBe 16
        machine.memLoad shouldBe 11
        machine.diskUsage shouldBe 46
        machine.createTime shouldBe "2012-03-28 03:42:07".toLocalDateTime()
        machine.updateTime shouldBe "2012-09-03 04:11:46".toLocalDateTime()

        MachineService.findById(12) shouldBe null
        MachineService.findById(300) shouldBe null
    }

    "按 ip 查找服务器" {
        val machine = MachineService.findByIP("17.212.169.100")
        machine shouldNotBe null
        machine!!
        machine.id shouldBe 3
        machine.hostname shouldBe "nknvleif"
        machine.mac shouldBe "9E-EE-49-FA-00-F4"
        machine.cpuLoad shouldBe 98
        machine.memLoad shouldBe 48
        machine.diskUsage shouldBe 31
        machine.createTime shouldBe "2035-11-05 14:17:43".toLocalDateTime()
        machine.updateTime shouldBe "2036-03-31 18:40:59".toLocalDateTime()
        MachineService.findByIP("131.236.90.140") shouldBe null
        MachineService.findByIP("257.0.0.1") shouldBe null
    }

    "按 hostname 查找服务器" {
        val machine = MachineService.findByHostname("nknvleif")
        machine shouldNotBe null
        machine!!
        machine.id shouldBe 3
        machine.hostname shouldBe "nknvleif"
        machine.ip shouldBe "17.212.169.100"
        machine.mac shouldBe "9E-EE-49-FA-00-F4"
        machine.cpuLoad shouldBe 98
        machine.memLoad shouldBe 48
        machine.diskUsage shouldBe 31
        machine.createTime shouldBe "2035-11-05 14:17:43".toLocalDateTime()
        machine.updateTime shouldBe "2036-03-31 18:40:59".toLocalDateTime()

        MachineService.findByHostname("mioohisg") shouldBe null
        MachineService.findByHostname("not exists") shouldBe null
    }

    "创建服务器" {
        val machine = MachineService.create("192.168.1.1")
        machine.hostname shouldBe ""
        machine.ip shouldBe "192.168.1.1"
        machine.cpuLoad shouldBe 0
        machine.memLoad shouldBe 0
        machine.diskUsage shouldBe 0
        machine.createTime shouldBeLessThanOrEqualTo LocalDateTime.now()
        machine.updateTime shouldBeLessThanOrEqualTo LocalDateTime.now()
    }

    "创建同 IP 服务器抛异常" {
        shouldThrow<DuplicateException> {
            MachineService.create("107.116.90.29")
        }.message shouldBe "服务器地址 107.116.90.29 已存在"
    }

    "更新服务器信息" {
        MachineService.update(id = 1, hostname = "new_name", ip = "192.168.1.1")
        var machine = MachineService.findById(1)!!
        machine.hostname shouldBe "new_name"
        machine.ip shouldBe "192.168.1.1"
        val updateTime = machine.updateTime
        updateTime shouldNotBe "2032-06-08 19:36:03".toLocalDateTime()

        MachineService.update(id = 1, cpuLoad = 1, memLoad = 2, diskUsage = 3)
        machine = MachineService.findById(1)!!
        machine.hostname shouldBe "new_name"
        machine.ip shouldBe "192.168.1.1"
        machine.cpuLoad shouldBe 1
        machine.memLoad shouldBe 2
        machine.diskUsage shouldBe 3
        machine.updateTime shouldBeGreaterThanOrEqualTo updateTime
    }

    "更新不存在或已删除的服务器抛异常" {
        shouldThrow<NotFoundException> {
            MachineService.update(7, hostname = "anything")
        }.message shouldBe "服务器 7 不存在或已被删除"
        shouldThrow<NotFoundException> {
            MachineService.update(300, hostname = "anything")
        }.message shouldBe "服务器 300 不存在或已被删除"
    }

    "更新已存在的 IP 抛异常" {
        shouldThrow<DuplicateException> {
            MachineService.update(1, ip = "17.212.169.100")
        }.message shouldBe "服务器地址 17.212.169.100 已存在"
    }

    "删除服务器" {
        MachineService.findById(1) shouldNotBe null
        MachineService.remove(1)
        MachineService.findById(1) shouldBe null
    }

    "删除不存在或已删除的服务器抛异常" {
        shouldThrow<NotFoundException> {
            MachineService.remove(7)
        }.message shouldBe "服务器 7 不存在或已被删除"

        shouldThrow<NotFoundException> {
            MachineService.remove(300)
        }.message shouldBe "服务器 300 不存在或已被删除"
    }

}, MachineDAO)