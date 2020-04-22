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
import tech.cuda.datahub.service.dao.Groups
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.toLocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class GroupServiceTest : TestWithMaria({

    "分页查询" {
        val validGroupCount = 32
        val pageSize = 7
        val queryTimes = validGroupCount / pageSize + 1
        val lastPageGroupCount = validGroupCount % pageSize
        for (page in 1..queryTimes) {
            val (groups, count) = GroupService.listing(page, pageSize)
            count shouldBe validGroupCount
            groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
        }
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validGroupCount = 32
        var pageSize = 5
        var queryTimes = validGroupCount / pageSize + 1
        var lastPageGroupCount = validGroupCount % pageSize
        for (page in 1..queryTimes) {
            with(GroupService.listing(page, pageSize, " null")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
            with(GroupService.listing(page, pageSize, " NULL ")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
            with(GroupService.listing(page, pageSize, "   ")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
            with(GroupService.listing(page, pageSize, null)) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
        }

        // 提供一个相似词
        validGroupCount = 12
        pageSize = 5
        queryTimes = validGroupCount / pageSize + 1
        lastPageGroupCount = validGroupCount % pageSize
        for (page in 1..queryTimes) {
            with(GroupService.listing(page, pageSize, " f ")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
            with(GroupService.listing(page, pageSize, " NULL f ")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
            with(GroupService.listing(page, pageSize, "f")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
        }

        // 提供两个相似词
        validGroupCount = 5
        pageSize = 2
        queryTimes = validGroupCount / pageSize + 1
        lastPageGroupCount = validGroupCount % pageSize
        for (page in 1..queryTimes) {
            with(GroupService.listing(page, pageSize, " f r")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
            with(GroupService.listing(page, pageSize, " r NULL f ")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
            with(GroupService.listing(page, pageSize, "f r null")) {
                val (groups, count) = this
                groups.size shouldBe if (page == queryTimes) lastPageGroupCount else pageSize
                count shouldBe validGroupCount
            }
        }
    }

    "按 id 查找项目组" {
        with(GroupService.findById(2)) {
            this shouldNotBe null
            this!!
            this.name shouldBe "vijhgvhx"
            this.createTime shouldBe "2029-05-26 23:17:01".toLocalDateTime()
            this.updateTime shouldBe "2029-08-03 07:16:51".toLocalDateTime()
        }
        GroupService.findById(7) shouldBe null
        GroupService.findById(40) shouldBe null
    }

    "按 name 查找项目组" {
        with(GroupService.findByName("vijhgvhx")) {
            this shouldNotBe null
            this!!
            this.id shouldBe 2
            this.createTime shouldBe "2029-05-26 23:17:01".toLocalDateTime()
            this.updateTime shouldBe "2029-08-03 07:16:51".toLocalDateTime()
        }
        GroupService.findByName("uryuohul") shouldBe null
        GroupService.findByName("not exists") shouldBe null
    }

    "创建项目组" {
        val nextGroupId = 40
        val name = "test_create"
        GroupService.findById(nextGroupId) shouldBe null
        val group = GroupService.create(name)
        group.id shouldBe nextGroupId
    }

    "创建同名项目组抛异常" {
        val duplicateName = "vijhgvhx"
        val exception = shouldThrow<DuplicateException> {
            GroupService.create(duplicateName)
        }
        exception.message shouldBe "项目组 $duplicateName 已存在"
    }

    "更新项目组" {
        val newName = "new_name"
        GroupService.update(25, name = newName) shouldNotBe null
        val group = GroupService.findById(25)
        group shouldNotBe null
        group!!
        group.name shouldBe newName
        group.updateTime shouldNotBe "2051-07-10 20:16:48".toLocalDateTime()
    }

    "更新不存在或已删除的项目组抛异常" {
        shouldThrow<NotFoundException> {
            GroupService.update(7, name = "failed")
        }.message shouldBe "项目组 7 不存在或已被删除"

        shouldThrow<NotFoundException> {
            GroupService.update(98, name = "failed")
        }.message shouldBe "项目组 98 不存在或已被删除"
    }

    "删除项目组" {
        GroupService.findById(2) shouldNotBe null
        GroupService.remove(2)
        GroupService.findById(2) shouldBe null
    }

    "删除不存在或已删除的项目组抛异常" {
        shouldThrow<NotFoundException> {
            GroupService.remove(7)
        }.message shouldBe "项目组 7 不存在或已被删除"

        shouldThrow<NotFoundException> {
            GroupService.remove(98)
        }.message shouldBe "项目组 98 不存在或已被删除"
    }

}, Groups)