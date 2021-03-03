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
import tech.cuda.woden.common.service.dao.ContainerDAO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.po.dtype.ContainerRole
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class ContainerServiceTest : TestWithMaria({
    "分页查询" {
        val validContainerCount = 188
        val pageSize = 13
        val queryTimes = validContainerCount / pageSize + 1
        val lastPageContainerCount = validContainerCount % pageSize
        for (page in 1..queryTimes) {
            val (containers, count) = ContainerService.listing(page, pageSize)
            containers.size shouldBe if (page == queryTimes) lastPageContainerCount else pageSize
            count shouldBe validContainerCount
        }
    }

    "查询存活的 master" {
        val (containers, count) = ContainerService.listingActiveMaster()
        containers.size shouldBe 3
        count shouldBe 3
        containers.map { it.hostname } shouldContainExactlyInAnyOrder listOf("tejxajfq", "nknvleif", "myjweyss")
        containers.map { it.id } shouldContainExactlyInAnyOrder listOf(1, 3, 5)
    }

    "查询存活的 slave" {
        val (containers, count) = ContainerService.listingActiveSlave()
        containers.size shouldBe 156
        count shouldBe 156
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validContainerCount = 188
        var pageSize = 13
        var queryTimes = validContainerCount / pageSize + 1
        var lastPageGroupCount = validContainerCount % pageSize
        for (page in 1..queryTimes) {
            with(ContainerService.listing(page, pageSize, null)) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
            with(ContainerService.listing(page, pageSize, " NULL")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
            with(ContainerService.listing(page, pageSize, " ")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
        }

        // 提供一个相似词
        validContainerCount = 58
        pageSize = 5
        queryTimes = validContainerCount / pageSize + 1
        lastPageGroupCount = validContainerCount % pageSize
        for (page in 1..queryTimes) {
            with(ContainerService.listing(page, pageSize, " a ")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
            with(ContainerService.listing(page, pageSize, " a NULL")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
            with(ContainerService.listing(page, pageSize, " null a")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
        }

        // 提供两个相似词
        validContainerCount = 15
        pageSize = 2
        queryTimes = validContainerCount / pageSize + 1
        lastPageGroupCount = validContainerCount % pageSize
        for (page in 1..queryTimes) {
            with(ContainerService.listing(page, pageSize, " a b")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
            with(ContainerService.listing(page, pageSize, " b a NULL")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
            with(ContainerService.listing(page, pageSize, "a null b")) {
                val (containers, count) = this
                containers.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validContainerCount
            }
        }
    }

    "按 id 查找容器" {
        val container = ContainerService.findById(11)
        container shouldNotBe null
        container!!
        container.hostname shouldBe "nncxrbwz"
        container.cpuLoad shouldBe 16
        container.memLoad shouldBe 11
        container.isActive shouldBe false
        container.role shouldBe ContainerRole.MASTER
        container.diskUsage shouldBe 46
        container.createTime shouldBe "2012-03-28 03:42:07".toLocalDateTime()
        container.updateTime shouldBe "2012-09-03 04:11:46".toLocalDateTime()

        ContainerService.findById(12) shouldBe null
        ContainerService.findById(300) shouldBe null
    }

    "按 hostname 查找容器" {
        val container = ContainerService.findByHostname("nknvleif")
        container shouldNotBe null
        container!!
        container.id shouldBe 3
        container.hostname shouldBe "nknvleif"
        container.cpuLoad shouldBe 98
        container.memLoad shouldBe 48
        container.diskUsage shouldBe 31
        container.isActive shouldBe true
        container.role shouldBe ContainerRole.MASTER
        container.createTime shouldBe "2035-11-05 14:17:43".toLocalDateTime()
        container.updateTime shouldBe "2036-03-31 18:40:59".toLocalDateTime()

        ContainerService.findByHostname("mioohisg") shouldBe null
        ContainerService.findByHostname("not exists") shouldBe null
    }

    "查询负载最低的容器" {
        val container = ContainerService.findSlackContainer()
        container.id shouldBe 58
        container.hostname shouldBe "kaefkqop"
        container.memLoad shouldBe 0
        container.cpuLoad shouldBe 59
    }

    "创建容器" {
        val container = ContainerService.create("test_create")
        container.hostname shouldBe "test_create"
        container.cpuLoad shouldBe 0
        container.memLoad shouldBe 0
        container.diskUsage shouldBe 0
        container.isActive shouldBe true
        container.role shouldBe ContainerRole.SLAVE
        container.createTime shouldBeLessThanOrEqualTo LocalDateTime.now()
        container.updateTime shouldBeLessThanOrEqualTo LocalDateTime.now()

        // hostname 已存在
        shouldThrow<DuplicateException> {
            ContainerService.create("tejxajfq")
        }.message shouldBe "主机名 tejxajfq 已存在"

    }

    "更新容器信息" {
        ContainerService.update(id = 1, hostname = "new_name")
        var container = ContainerService.findById(1)!!
        container.hostname shouldBe "new_name"
        val updateTime = container.updateTime
        updateTime shouldNotBe "2032-06-08 19:36:03".toLocalDateTime()

        ContainerService.update(id = 1, cpuLoad = 1, memLoad = 2, diskUsage = 3)
        container = ContainerService.findById(1)!!
        container.hostname shouldBe "new_name"
        container.cpuLoad shouldBe 1
        container.memLoad shouldBe 2
        container.diskUsage shouldBe 3
        container.isActive shouldBe true
        container.role shouldBe ContainerRole.MASTER
        container.updateTime shouldBeGreaterThanOrEqualTo updateTime

        ContainerService.update(id = 1, isActive = false, role = ContainerRole.SLAVE)
        container = ContainerService.findById(1)!!
        container.isActive shouldBe false
        container.role shouldBe ContainerRole.SLAVE

        // 已删除
        shouldThrow<NotFoundException> {
            ContainerService.update(7, hostname = "anything")
        }.message shouldBe "调度容器 7 不存在或已被删除"

        // 不存在
        shouldThrow<NotFoundException> {
            ContainerService.update(300, hostname = "anything")
        }.message shouldBe "调度容器 300 不存在或已被删除"

        // hostname 已存在
        shouldThrow<DuplicateException> {
            ContainerService.update(1, hostname = "tipwcfcc")
        }.message shouldBe "主机名 tipwcfcc 已存在"
    }

    "删除容器" {
        ContainerService.findById(1) shouldNotBe null
        ContainerService.remove(1)
        ContainerService.findById(1) shouldBe null

        // 不存在
        shouldThrow<NotFoundException> {
            ContainerService.remove(7)
        }.message shouldBe "调度容器 7 不存在或已被删除"

        // 已删除
        shouldThrow<NotFoundException> {
            ContainerService.remove(300)
        }.message shouldBe "调度容器 300 不存在或已被删除"
    }

}, ContainerDAO)