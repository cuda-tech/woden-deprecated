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
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.datahub.TestWithMaria
import tech.cuda.datahub.service.dao.Users
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.toLocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class UserServiceTest : TestWithMaria({

    "按 name 查找用户" {
        with(UserService.findByName("WjWUMovObM")) {
            this shouldNotBe null
            this!!
            groups shouldContainExactlyInAnyOrder setOf(8, 7, 3, 6, 2, 1)
            name shouldBe "WjWUMovObM"
            email shouldBe "WjWUMovObM@139.com"
            createTime shouldBe "2042-06-02 09:25:38".toLocalDateTime()
            updateTime shouldBe "2043-01-26 13:59:27".toLocalDateTime()
            password shouldBe ""
        }
    }

    "按 name 查找用户如果不存在或已被删除则返回 null" {
        UserService.findByName("someone did not exists") shouldBe null
        UserService.findByName("NCiUmXrvkC") shouldBe null
    }

    "按 id 查找用户" {
        with(UserService.findById(66)) {
            this shouldNotBe null
            this!!
            groups shouldContainExactlyInAnyOrder setOf(8, 7, 3, 6, 2, 1)
            name shouldBe "WjWUMovObM"
            email shouldBe "WjWUMovObM@139.com"
            createTime shouldBe "2042-06-02 09:25:38".toLocalDateTime()
            updateTime shouldBe "2043-01-26 13:59:27".toLocalDateTime()
            password shouldBe ""
        }
    }

    "按 id 查找用户如果不存在或已被删除则返回 null"{
        UserService.findById(67) shouldBe null
        UserService.findById(180) shouldBe null
    }

    "分页查询" {
        val validUserCount = 143
        val pageSize = 13
        val queryTimes = validUserCount / pageSize + 1
        val lastPageUserCount = validUserCount % pageSize
        for (page in 1..queryTimes) {
            val (users, count) = UserService.listing(page, pageSize)
            count shouldBe validUserCount
            users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
            users.forEach {
                it.password shouldBe ""
            }
        }
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validUserCount = 143
        var pageSize = 13
        var queryTimes = validUserCount / pageSize + 1
        var lastPageUserCount = validUserCount % pageSize
        for (page in 1..queryTimes) {
            with(UserService.listing(page, pageSize, null)) {
                val (users, count) = this
                users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
                count shouldBe validUserCount
                users.forEach { it.password shouldBe "" }
            }

            with(UserService.listing(page, pageSize, "   ")) {
                val (users, count) = this
                users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
                count shouldBe validUserCount
                users.forEach { it.password shouldBe "" }
            }

            with(UserService.listing(page, pageSize, " NULL  ")) {
                val (users, count) = this
                users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
                count shouldBe validUserCount
                users.forEach { it.password shouldBe "" }
            }
        }

        // 提供 1 个相似词
        validUserCount = 43
        pageSize = 7
        queryTimes = validUserCount / pageSize + 1
        lastPageUserCount = validUserCount % pageSize
        for (page in 1..queryTimes) {
            with(UserService.listing(page, pageSize, "a")) {
                val (users, count) = this
                users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
                count shouldBe validUserCount
                users.forEach { it.password shouldBe "" }
            }

            with(UserService.listing(page, pageSize, "  a null")) {
                val (users, count) = this
                users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
                count shouldBe validUserCount
                users.forEach { it.password shouldBe "" }
            }
        }

        // 提供 2 个相似词
        validUserCount = 8
        pageSize = 3
        queryTimes = validUserCount / pageSize + 1
        lastPageUserCount = validUserCount % pageSize
        for (page in 1..queryTimes) {
            with(UserService.listing(page, pageSize, "a b")) {
                val (users, count) = this
                users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
                count shouldBe validUserCount
                users.forEach { it.password shouldBe "" }
            }

            with(UserService.listing(page, pageSize, " b  a null")) {
                val (users, count) = this
                users.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
                count shouldBe validUserCount
                users.forEach { it.password shouldBe "" }
            }
        }
    }

    "生成 token" {
        UserService.sign("root", "root") shouldNotBe null
        UserService.sign("root", "wrong password") shouldBe null
        UserService.sign("not exists user", "wrong password") shouldBe null
    }

    "通过 token 获取用户" {
        val rootUser = UserService.getUserByToken(UserService.sign("root", "root")!!)
        rootUser shouldNotBe null
        rootUser!!
        rootUser.name shouldBe "root"
        rootUser.password shouldBe ""
        rootUser.id shouldBe 1

        val guestUser = UserService.getUserByToken(UserService.sign("guest", "guest")!!)
        guestUser shouldNotBe null
        guestUser!!
        guestUser.name shouldBe "guest"
        guestUser.password shouldBe ""
        guestUser.id shouldBe 2

        UserService.getUserByToken("wrong token") shouldBe null
    }

    "校验 token 是否为数据库中某条记录生成" {
        UserService.verify(UserService.sign("root", "root")!!) shouldBe true
        UserService.verify(UserService.sign("guest", "guest")!!) shouldBe true
        UserService.verify("wrong token") shouldBe false
    }

    "创建用户" {
        val nextUserId = 180
        val name = "test_create"
        val password = "test_password"
        val groupIds = setOf(131, 127)
        val email = "test_create@datahub.com"

        val newUser = UserService.create(name, password, groupIds, email)
        newUser.id shouldBe nextUserId
        newUser.name shouldBe name
        newUser.password shouldBe ""
        newUser.groups shouldContainExactlyInAnyOrder groupIds
        newUser.email shouldBe email
        newUser.createTime shouldNotBe null
        newUser.updateTime shouldNotBe null
    }

    "创建同名用户抛出异常" {
        val duplicateName = "OHzXwnDAAd"
        val exception = shouldThrow<DuplicateException> {
            UserService.create(duplicateName, "", setOf(), "")
        }
        exception.message shouldBe "用户 $duplicateName 已存在"
    }

    "删除用户" {
        UserService.findById(3) shouldNotBe null
        UserService.remove(3)
        UserService.findById(3) shouldBe null
    }

    "删除不存在或已被删除用户时抛出异常" {
        shouldThrow<NotFoundException> {
            UserService.remove(4)
        }.message shouldBe "用户 4 不存在或已被删除"

        shouldThrow<NotFoundException> {
            UserService.remove(180)
        }.message shouldBe "用户 180 不存在或已被删除"
    }

    "更新用户名" {
        val oldToken = UserService.sign("root", "root")
        oldToken shouldNotBe null
        oldToken!!

        val newName = "root_new_name"
        UserService.update(1, name = newName)
        UserService.getUserByToken(oldToken) shouldBe null
        UserService.verify(oldToken) shouldBe false

        val newToken = UserService.sign(newName, "root")
        newToken shouldNotBe null
        newToken!!
        UserService.getUserByToken(newToken) shouldNotBe null
        UserService.verify(newToken) shouldBe true
    }

    "更新密码" {
        val oldToken = UserService.sign("root", "root")
        oldToken shouldNotBe null
        oldToken!!

        val newPassword = "new_password"
        UserService.update(1, password = newPassword)
        UserService.getUserByToken(oldToken) shouldBe null
        UserService.verify(oldToken) shouldBe false

        val newToken = UserService.sign("root", newPassword)
        newToken shouldNotBe null
        newToken!!
        UserService.getUserByToken(newToken) shouldNotBe null
        UserService.verify(newToken) shouldBe true
    }

    "更新权限组 & 邮箱" {
        val newEmail = "new_email@datahub.com"
        val newGroupIds = setOf(137, 149)
        UserService.update(id = 1, email = newEmail, groups = newGroupIds)
        val user = UserService.findById(1)
        user shouldNotBe null
        user!!
        user.email shouldBe newEmail
        user.groups shouldContainExactlyInAnyOrder newGroupIds
    }

    "更新不存在或已被删除用户时抛出异常" {
        shouldThrow<NotFoundException> {
            UserService.update(4, "anything")
        }.message shouldBe "用户 4 不存在或已被删除"

        shouldThrow<NotFoundException> {
            UserService.update(180, "anything")
        }.message shouldBe "用户 180 不存在或已被删除"
    }

}, Users)
