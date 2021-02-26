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
package tech.cuda.woden.webserver.controller

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.common.service.UserService
import tech.cuda.woden.common.service.dto.UserDTO
import tech.cuda.woden.common.service.toLocalDateTime
import tech.cuda.woden.webserver.RestfulTestToolbox

/**
 * @author Jensen Qi
 * @since 0.1.0
 */
open class UserControllerTest : RestfulTestToolbox("users") {

    @Test
    fun listing() {
        val validCount = 143
        with(postman.get("/api/user").shouldSuccess) {
            val users = this.getList<UserDTO>("users")
            val count = this.get<Int>("count")
            users.size shouldBe validCount
            count shouldBe validCount
        }

        val pageSize = 13
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/user", mapOf("page" to page, "pageSize" to pageSize)).shouldSuccess) {
                val users = this.getList<UserDTO>("users")
                val count = this.get<Int>("count")
                users.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun search() {
        // 提供空或 null 的相似词
        var validCount = 143
        var pageSize = 13
        var queryTimes = validCount / pageSize + 1
        var lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/user", mapOf("page" to page, "pageSize" to pageSize, "like" to null)).shouldSuccess) {
                val users = this.getList<UserDTO>("users")
                val count = this.get<Int>("count")
                users.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
            with(postman.get("/api/user", mapOf("page" to page, "pageSize" to pageSize, "like" to "  ")).shouldSuccess) {
                val users = this.getList<UserDTO>("users")
                val count = this.get<Int>("count")
                users.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 1 个相似词
        validCount = 43
        pageSize = 7
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/user", mapOf("page" to page, "pageSize" to pageSize, "like" to " a")).shouldSuccess) {
                val users = this.getList<UserDTO>("users")
                val count = this.get<Int>("count")
                users.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 2 个相似词
        validCount = 8
        pageSize = 3
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/user", mapOf("page" to page, "pageSize" to pageSize, "like" to " a b")).shouldSuccess) {
                val users = this.getList<UserDTO>("users")
                val count = this.get<Int>("count")
                users.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun currentUser() {
        postman.login("root", "root")
        postman.get("/api/user/current").shouldSuccess.get<UserDTO>("user").withExpect {
            it.groups shouldContainExactlyInAnyOrder setOf(1)
            it.name shouldBe "root"
            it.email shouldBe "root@woden.com"
            it.createTime shouldBe "2048-08-14 06:10:35".toLocalDateTime()
            it.updateTime shouldBe "2051-03-13 21:06:23".toLocalDateTime()
        }

        postman.login("guest", "guest")
        postman.get("/api/user/current").shouldSuccess.get<UserDTO>("user").withExpect {
            it.groups shouldContainExactlyInAnyOrder setOf(6, 8, 1, 9, 7, 5, 2)
            it.name shouldBe "guest"
            it.email shouldBe "guest@woden.com"
            it.createTime shouldBe "2041-02-10 19:37:55".toLocalDateTime()
            it.updateTime shouldBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun find() {
        postman.get("/api/user/66").shouldSuccess.get<UserDTO>("user").withExpect {
            it.groups shouldContainExactlyInAnyOrder setOf(8, 7, 3, 6, 2, 1)
            it.name shouldBe "WjWUMovObM"
            it.email shouldBe "WjWUMovObM@139.com"
            it.createTime shouldBe "2042-06-02 09:25:38".toLocalDateTime()
            it.updateTime shouldBe "2043-01-26 13:59:27".toLocalDateTime()
        }

        postman.get("/api/user/67").shouldFailed.withError("用户 67 不存在或已被删除")
    }

    @Test
    fun create() {
        // 禁止创建重名用户
        postman.post("/api/user", mapOf("name" to "root", "password" to "", "groupIds" to listOf(1), "email" to ""))
            .shouldFailed.withError("用户 root 已存在")

        val nextUserId = 180
        val name = "test_create"
        val password = "test_password"
        val groupIds = setOf(131, 127)
        val email = "test_create@woden.com"

        postman.post("/api/user", mapOf("name" to name, "password" to password, "groupIds" to groupIds, "email" to email))
            .shouldSuccess.get<UserDTO>("user").withExpect {
            it.id shouldBe nextUserId
            it.name shouldBe name
            it.email shouldBe email
            it.groups shouldContainExactlyInAnyOrder groupIds
        }

        postman.get("/api/user/$nextUserId").shouldSuccess.get<UserDTO>("user").withExpect {
            it.groups shouldContainExactlyInAnyOrder groupIds
            it.name shouldBe name
            it.email shouldBe email
        }

        val token = postman.post("/api/login", mapOf("username" to name, "password" to password))
            .shouldSuccess.get<String>("token").toString()
        UserService.getUserByToken(token)?.name shouldBe name
    }

    @Test
    fun updateName() {
        val oldName = "root"
        val newName = "root_new_name"

        postman.put("/api/user/1", mapOf("name" to "guest")).shouldFailed.withError("用户 guest 已存在")

        postman.post("/api/login", mapOf("username" to oldName, "password" to "root")).shouldSuccess
        postman.put("/api/user/1", mapOf("name" to newName)).shouldSuccess.get<UserDTO>("user").withExpect {
            it.name shouldBe newName
        }
        postman.post("/api/login", mapOf("username" to oldName, "password" to "root")).shouldFailed.withError("登录失败")
        val token = postman.post("/api/login", mapOf("username" to newName, "password" to "root"))
            .shouldSuccess.get<String>("token")
        UserService.getUserByToken(token)?.name shouldBe newName

    }

    @Test
    fun updatePassword() {
        val oldPassword = "root"
        val newPassword = "root_new_password"

        postman.post("/api/login", mapOf("username" to "root", "password" to oldPassword)).shouldSuccess
        postman.put("/api/user/1", mapOf("password" to newPassword)).shouldSuccess
        postman.post("/api/login", mapOf("username" to "root", "password" to oldPassword)).shouldFailed.withError("登录失败")

        val token = postman.post("/api/login", mapOf("username" to "root", "password" to newPassword))
            .shouldSuccess.get<String>("token")
        UserService.getUserByToken(token)?.name shouldBe "root"
    }

    @Test
    fun updateEmail() {
        val newEmail = "new_email@woden.com"
        postman.put("/api/user/2", mapOf("email" to newEmail)).shouldSuccess.get<UserDTO>("user").withExpect {
            it.email shouldBe newEmail
        }
        postman.get("/api/user/2").shouldSuccess.get<UserDTO>("user").withExpect {
            it.email shouldBe newEmail
            it.updateTime shouldNotBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun updateGroupIds() {
        val newGroupIds = setOf(137, 149)
        postman.put("/api/user/2", mapOf("groupIds" to newGroupIds)).shouldSuccess.get<UserDTO>("user").withExpect {
            it.groups shouldContainExactlyInAnyOrder newGroupIds
        }
        postman.get("/api/user/2").shouldSuccess.get<UserDTO>("user").withExpect {
            it.groups shouldContainExactlyInAnyOrder newGroupIds
            it.updateTime shouldNotBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun updateAll() {
        val newName = "new_name"
        val newPassword = "new_password"
        val newEmail = "new_email@woden.com"
        val newGroupIds = setOf(137, 149)
        postman.put("/api/user/2", mapOf("name" to newName, "password" to newPassword, "email" to newEmail, "groupIds" to newGroupIds))
            .shouldSuccess.get<UserDTO>("user").withExpect {
            it.name shouldBe newName
            it.email shouldBe newEmail
            it.groups shouldContainExactlyInAnyOrder newGroupIds
        }

        val token = postman.post("/api/login", mapOf("username" to newName, "password" to newPassword))
            .shouldSuccess.get<String>("token").toString()
        UserService.getUserByToken(token)?.name shouldBe newName

        postman.get("/api/user/2").shouldSuccess.get<UserDTO>("user").withExpect {
            it.groups shouldContainExactlyInAnyOrder newGroupIds
            it.email shouldBe newEmail
            it.updateTime shouldNotBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun updateInvalidUser() {
        postman.put("/api/user/4", mapOf("name" to "user who has been remove")).shouldFailed.withError("用户 4 不存在或已被删除")
        postman.put("/api/user/180", mapOf("name" to "user not exists")).shouldFailed.withError("用户 180 不存在或已被删除")
    }

    @Test
    fun remove() {
        postman.get("/api/user/2").shouldSuccess
        postman.delete("/api/user/2").shouldSuccess.withMessage("用户 2 已被删除")
        postman.get("/api/user/2").shouldFailed.withError("用户 2 不存在或已被删除")
        postman.delete("/api/user/4").shouldFailed.withError("用户 4 不存在或已被删除")

        postman.delete("/api/user/180").shouldFailed.withError("用户 180 不存在或已被删除")
    }
}