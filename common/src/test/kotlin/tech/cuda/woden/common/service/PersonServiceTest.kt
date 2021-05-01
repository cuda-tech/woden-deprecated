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

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.common.service.dao.PersonDAO
import tech.cuda.woden.common.service.dao.PersonTeamMappingDAO
import tech.cuda.woden.common.service.dao.TeamDAO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.exception.PermissionException

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class PersonServiceTest : TestWithMaria({

    "按 name 查找用户" {
        with(PersonService.findByName("WjWUMovObM")) {
            this shouldNotBe null
            this!!
            teams.map { it.id } shouldContainExactlyInAnyOrder setOf(8, 7, 3, 6, 2, 1)
            name shouldBe "WjWUMovObM"
            email shouldBe "WjWUMovObM@139.com"
            createTime shouldBe "2042-06-02 09:25:38".toLocalDateTime()
            updateTime shouldBe "2043-01-26 13:59:27".toLocalDateTime()
        }
        PersonService.findByName("someone did not exists") shouldBe null
        PersonService.findByName("NCiUmXrvkC") shouldBe null
    }

    "按 id 查找用户" {
        with(PersonService.findById(66)) {
            this shouldNotBe null
            this!!
            teams.map { it.id } shouldContainExactlyInAnyOrder setOf(8, 7, 3, 6, 2, 1)
            name shouldBe "WjWUMovObM"
            email shouldBe "WjWUMovObM@139.com"
            createTime shouldBe "2042-06-02 09:25:38".toLocalDateTime()
            updateTime shouldBe "2043-01-26 13:59:27".toLocalDateTime()
        }
        PersonService.findById(67) shouldBe null
        PersonService.findById(180) shouldBe null
    }

    "用户检查" {
        shouldNotThrowAny { PersonService.requireExists(66) }
        shouldThrow<NotFoundException> { PersonService.requireExists(67) }
        shouldThrow<NotFoundException> { PersonService.requireExists((180)) }

        shouldNotThrowAny { PersonService.requireExists(listOf(1, 2, 66)) }
        shouldThrow<NotFoundException> { PersonService.requireExists(listOf(1, 2, 67)) }

        shouldNotThrowAny { PersonService.requireTeamMember(66, 7) }
        shouldThrow<PermissionException> { PersonService.requireTeamMember(66, 10) }

        shouldNotThrowAny { PersonService.requireTeamMember(66, listOf(8, 7, 3, 6, 2, 1)) }
        shouldThrow<PermissionException> { PersonService.requireTeamMember(66, listOf(1, 10)) }
    }

    "分页查询" {
        val validPersonCount = 143
        val pageSize = 13
        val queryTimes = validPersonCount / pageSize + 1
        val lastPagePersonCount = validPersonCount % pageSize
        for (page in 1..queryTimes) {
            val (persons, count) = PersonService.listing(page, pageSize)
            count shouldBe validPersonCount
            persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
        }
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validPersonCount = 143
        var pageSize = 13
        var queryTimes = validPersonCount / pageSize + 1
        var lastPagePersonCount = validPersonCount % pageSize
        for (page in 1..queryTimes) {
            with(PersonService.listing(page, pageSize, null)) {
                val (persons, count) = this
                persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
                count shouldBe validPersonCount
            }

            with(PersonService.listing(page, pageSize, "   ")) {
                val (persons, count) = this
                persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
                count shouldBe validPersonCount
            }

            with(PersonService.listing(page, pageSize, " NULL  ")) {
                val (persons, count) = this
                persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
                count shouldBe validPersonCount
            }
        }

        // 提供 1 个相似词
        validPersonCount = 43
        pageSize = 7
        queryTimes = validPersonCount / pageSize + 1
        lastPagePersonCount = validPersonCount % pageSize
        for (page in 1..queryTimes) {
            with(PersonService.listing(page, pageSize, "a")) {
                val (persons, count) = this
                persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
                count shouldBe validPersonCount
            }

            with(PersonService.listing(page, pageSize, "  a null")) {
                val (persons, count) = this
                persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
                count shouldBe validPersonCount
            }
        }

        // 提供 2 个相似词
        validPersonCount = 8
        pageSize = 3
        queryTimes = validPersonCount / pageSize + 1
        lastPagePersonCount = validPersonCount % pageSize
        for (page in 1..queryTimes) {
            with(PersonService.listing(page, pageSize, "a b")) {
                val (persons, count) = this
                persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
                count shouldBe validPersonCount
            }

            with(PersonService.listing(page, pageSize, " b  a null")) {
                val (persons, count) = this
                persons.size shouldBe if (page == queryTimes) lastPagePersonCount else pageSize
                count shouldBe validPersonCount
            }
        }
    }

    "生成 token" {
        PersonService.sign("root", "root") shouldNotBe null
        PersonService.sign("root", "wrong password") shouldBe null
        PersonService.sign("not exists person", "wrong password") shouldBe null
    }

    "通过 token 获取用户" {
        val rootPerson = PersonService.getPersonByToken(PersonService.sign("root", "root")!!)
        rootPerson shouldNotBe null
        rootPerson!!
        rootPerson.name shouldBe "root"
        rootPerson.id shouldBe 1

        val guestPerson = PersonService.getPersonByToken(PersonService.sign("guest", "guest")!!)
        guestPerson shouldNotBe null
        guestPerson!!
        guestPerson.name shouldBe "guest"
        guestPerson.id shouldBe 2

        PersonService.getPersonByToken("wrong token") shouldBe null
    }

    "校验 token 是否为数据库中某条记录生成" {
        PersonService.verify(PersonService.sign("root", "root")!!) shouldBe true
        PersonService.verify(PersonService.sign("guest", "guest")!!) shouldBe true
        PersonService.verify("wrong token") shouldBe false
    }

    "创建用户" {
        val nextPersonId = 180
        val name = "test_create"
        val password = "test_password"
        val teamIds = setOf(31, 27)
        val email = "test_create@woden.com"

        val newPerson = PersonService.create(name, password, teamIds, email)
        newPerson.id shouldBe nextPersonId
        newPerson.name shouldBe name
        newPerson.teams.map { it.id } shouldContainExactlyInAnyOrder teamIds
        newPerson.email shouldBe email
        newPerson.createTime shouldNotBe null
        newPerson.updateTime shouldNotBe null

        shouldThrow<NotFoundException> {
            PersonService.create("someone", "password", setOf(131, 27), "xxx@xx")
        }.message shouldBe "项目组 131 不存在或已被删除"
    }

    "创建同名用户抛出异常" {
        val duplicateName = "OHzXwnDAAd"
        val exception = shouldThrow<DuplicateException> {
            PersonService.create(duplicateName, "", setOf(), "")
        }
        exception.message shouldBe "用户 $duplicateName 已存在"
    }

    "删除用户" {
        PersonService.findById(3) shouldNotBe null
        PersonService.remove(3)
        PersonService.findById(3) shouldBe null
    }

    "删除不存在或已被删除用户时抛出异常" {
        shouldThrow<NotFoundException> {
            PersonService.remove(4)
        }.message shouldBe "用户 4 不存在或已被删除"

        shouldThrow<NotFoundException> {
            PersonService.remove(180)
        }.message shouldBe "用户 180 不存在或已被删除"
    }

    "更新用户名" {
        val oldToken = PersonService.sign("root", "root")
        oldToken shouldNotBe null
        oldToken!!

        val newName = "root_new_name"
        PersonService.update(1, name = newName)
        PersonService.getPersonByToken(oldToken) shouldBe null
        PersonService.verify(oldToken) shouldBe false

        val newToken = PersonService.sign(newName, "root")
        newToken shouldNotBe null
        newToken!!
        PersonService.getPersonByToken(newToken) shouldNotBe null
        PersonService.verify(newToken) shouldBe true

        PersonService.findByName(newName)!!.updateTime shouldNotBe "2051-03-13 21:06:23".toLocalDateTime()
    }

    "更新同名用户抛异常" {
        shouldThrow<DuplicateException> {
            PersonService.update(1, name = "guest")
        }.message shouldBe "用户 guest 已存在"
    }

    "更新密码" {
        val oldToken = PersonService.sign("root", "root")
        oldToken shouldNotBe null
        oldToken!!

        val newPassword = "new_password"
        PersonService.update(1, password = newPassword)
        PersonService.getPersonByToken(oldToken) shouldBe null
        PersonService.verify(oldToken) shouldBe false

        val newToken = PersonService.sign("root", newPassword)
        newToken shouldNotBe null
        newToken!!
        PersonService.getPersonByToken(newToken) shouldNotBe null
        PersonService.verify(newToken) shouldBe true

        PersonService.findById(1)!!.updateTime shouldNotBe "2051-03-13 21:06:23".toLocalDateTime()
    }

    "更新权限组 & 邮箱" {
        val oldPerson = PersonService.findById(1)
        oldPerson shouldNotBe null
        oldPerson!!
        oldPerson.email shouldBe "root@woden.com"
        oldPerson.teams.map { it.id } shouldContainExactlyInAnyOrder setOf(1)

        val newEmail = "new_email@woden.com"
        val newTeamIds = setOf(37, 38)
        PersonService.update(id = 1, email = newEmail, teamIds = newTeamIds)
        val newPerson = PersonService.findById(1)
        newPerson shouldNotBe null
        newPerson!!
        newPerson.email shouldBe newEmail
        newPerson.teams.map { it.id } shouldContainExactlyInAnyOrder newTeamIds
        newPerson.updateTime shouldNotBe "2051-03-13 21:06:23".toLocalDateTime()

        shouldThrow<NotFoundException> {
            PersonService.update(id = 1, teamIds = setOf(137, 149, 38))
        }.message shouldBe "项目组 137,149 不存在或已被删除"
    }

    "更新不存在或已被删除用户时抛出异常" {
        shouldThrow<NotFoundException> {
            PersonService.update(4, "anything")
        }.message shouldBe "用户 4 不存在或已被删除"

        shouldThrow<NotFoundException> {
            PersonService.update(180, "anything")
        }.message shouldBe "用户 180 不存在或已被删除"
    }

}, PersonDAO, TeamDAO, PersonTeamMappingDAO)
