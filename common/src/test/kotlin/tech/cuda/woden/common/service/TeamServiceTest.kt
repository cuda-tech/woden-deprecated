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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.common.service.dao.TeamDAO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class TeamServiceTest : TestWithMaria({

    "分页查询" {
        val validTeamCount = 32
        val pageSize = 7
        val queryTimes = validTeamCount / pageSize + 1
        val lastPageTeamCount = validTeamCount % pageSize
        for (page in 1..queryTimes) {
            val (teams, count) = TeamService.listing(page, pageSize)
            count shouldBe validTeamCount
            teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
        }
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validTeamCount = 32
        var pageSize = 5
        var queryTimes = validTeamCount / pageSize + 1
        var lastPageTeamCount = validTeamCount % pageSize
        for (page in 1..queryTimes) {
            with(TeamService.listing(page, pageSize, " null")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
            with(TeamService.listing(page, pageSize, " NULL ")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
            with(TeamService.listing(page, pageSize, "   ")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
            with(TeamService.listing(page, pageSize, null)) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
        }

        // 提供一个相似词
        validTeamCount = 12
        pageSize = 5
        queryTimes = validTeamCount / pageSize + 1
        lastPageTeamCount = validTeamCount % pageSize
        for (page in 1..queryTimes) {
            with(TeamService.listing(page, pageSize, " f ")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
            with(TeamService.listing(page, pageSize, " NULL f ")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
            with(TeamService.listing(page, pageSize, "f")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
        }

        // 提供两个相似词
        validTeamCount = 5
        pageSize = 2
        queryTimes = validTeamCount / pageSize + 1
        lastPageTeamCount = validTeamCount % pageSize
        for (page in 1..queryTimes) {
            with(TeamService.listing(page, pageSize, " f r")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
            with(TeamService.listing(page, pageSize, " r NULL f ")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
            with(TeamService.listing(page, pageSize, "f r null")) {
                val (teams, count) = this
                teams.size shouldBe if (page == queryTimes) lastPageTeamCount else pageSize
                count shouldBe validTeamCount
            }
        }
    }

    "按 id 查找项目组" {
        with(TeamService.findById(2)) {
            this shouldNotBe null
            this!!
            this.name shouldBe "vijhgvhx"
            this.createTime shouldBe "2029-05-26 23:17:01".toLocalDateTime()
            this.updateTime shouldBe "2029-08-03 07:16:51".toLocalDateTime()
        }
        TeamService.findById(7) shouldBe null
        TeamService.findById(40) shouldBe null
    }

    "按 name 查找项目组" {
        with(TeamService.findByName("vijhgvhx")) {
            this shouldNotBe null
            this!!
            this.id shouldBe 2
            this.createTime shouldBe "2029-05-26 23:17:01".toLocalDateTime()
            this.updateTime shouldBe "2029-08-03 07:16:51".toLocalDateTime()
        }
        TeamService.findByName("uryuohul") shouldBe null
        TeamService.findByName("not exists") shouldBe null
    }

    "创建项目组" {
        val nextTeamId = 40
        val name = "test_create"
        TeamService.findById(nextTeamId) shouldBe null
        val team = TeamService.create(name)
        team.id shouldBe nextTeamId
    }

    "创建同名项目组抛异常" {
        val duplicateName = "vijhgvhx"
        val exception = shouldThrow<DuplicateException> {
            TeamService.create(duplicateName)
        }
        exception.message shouldBe "项目组 $duplicateName 已存在"
    }

    "更新项目组" {
        val newName = "new_name"
        TeamService.update(25, name = newName) shouldNotBe null
        val team = TeamService.findById(25)
        team shouldNotBe null
        team!!
        team.name shouldBe newName
        team.updateTime shouldNotBe "2051-07-10 20:16:48".toLocalDateTime()
    }

    "更新不存在或已删除的项目组抛异常" {
        shouldThrow<NotFoundException> {
            TeamService.update(7, name = "failed")
        }.message shouldBe "项目组 7 不存在或已被删除"

        shouldThrow<NotFoundException> {
            TeamService.update(98, name = "failed")
        }.message shouldBe "项目组 98 不存在或已被删除"
    }

    "更新同名项目组抛异常" {
        shouldThrow<DuplicateException> {
            TeamService.update(25, name = "root")
        }.message shouldBe "项目组 root 已存在"
    }

    "删除项目组" {
        TeamService.findById(2) shouldNotBe null
        TeamService.remove(2)
        TeamService.findById(2) shouldBe null
    }

    "删除不存在或已删除的项目组抛异常" {
        shouldThrow<NotFoundException> {
            TeamService.remove(7)
        }.message shouldBe "项目组 7 不存在或已被删除"

        shouldThrow<NotFoundException> {
            TeamService.remove(98)
        }.message shouldBe "项目组 98 不存在或已被删除"
    }

}, TeamDAO)