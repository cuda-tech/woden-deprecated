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
import tech.cuda.woden.common.service.PersonService
import tech.cuda.woden.common.service.dto.PersonDTO
import tech.cuda.woden.common.service.toLocalDateTime
import tech.cuda.woden.webserver.RestfulTestToolbox

/**
 * @author Jensen Qi
 * @since 0.1.0
 */
open class PersonControllerTest : RestfulTestToolbox("person") {

    @Test
    fun listing() {
        val validCount = 143
        with(postman.get("/api/person").shouldSuccess) {
            val persons = this.getList<PersonDTO>("persons")
            val count = this.get<Int>("count")
            persons.size shouldBe validCount
            count shouldBe validCount
        }

        val pageSize = 13
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/person", mapOf("page" to page, "pageSize" to pageSize)).shouldSuccess) {
                val persons = this.getList<PersonDTO>("persons")
                val count = this.get<Int>("count")
                persons.size shouldBe if (page == queryTimes) lastPageCount else pageSize
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
            with(postman.get("/api/person", mapOf("page" to page, "pageSize" to pageSize, "like" to null)).shouldSuccess) {
                val persons = this.getList<PersonDTO>("persons")
                val count = this.get<Int>("count")
                persons.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
            with(postman.get("/api/person", mapOf("page" to page, "pageSize" to pageSize, "like" to "  ")).shouldSuccess) {
                val persons = this.getList<PersonDTO>("persons")
                val count = this.get<Int>("count")
                persons.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 1 个相似词
        validCount = 43
        pageSize = 7
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/person", mapOf("page" to page, "pageSize" to pageSize, "like" to " a")).shouldSuccess) {
                val persons = this.getList<PersonDTO>("persons")
                val count = this.get<Int>("count")
                persons.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 2 个相似词
        validCount = 8
        pageSize = 3
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/person", mapOf("page" to page, "pageSize" to pageSize, "like" to " a b")).shouldSuccess) {
                val persons = this.getList<PersonDTO>("persons")
                val count = this.get<Int>("count")
                persons.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun currentPerson() {
        postman.login("root", "root")
        postman.get("/api/person/current").shouldSuccess.get<PersonDTO>("person").withExpect {
            it.teams shouldContainExactlyInAnyOrder setOf(1)
            it.name shouldBe "root"
            it.email shouldBe "root@woden.com"
            it.createTime shouldBe "2048-08-14 06:10:35".toLocalDateTime()
            it.updateTime shouldBe "2051-03-13 21:06:23".toLocalDateTime()
        }

        postman.login("guest", "guest")
        postman.get("/api/person/current").shouldSuccess.get<PersonDTO>("person").withExpect {
            it.teams shouldContainExactlyInAnyOrder setOf(6, 8, 1, 9, 7, 5, 2)
            it.name shouldBe "guest"
            it.email shouldBe "guest@woden.com"
            it.createTime shouldBe "2041-02-10 19:37:55".toLocalDateTime()
            it.updateTime shouldBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun find() {
        postman.get("/api/person/66").shouldSuccess.get<PersonDTO>("person").withExpect {
            it.teams shouldContainExactlyInAnyOrder setOf(8, 7, 3, 6, 2, 1)
            it.name shouldBe "WjWUMovObM"
            it.email shouldBe "WjWUMovObM@139.com"
            it.createTime shouldBe "2042-06-02 09:25:38".toLocalDateTime()
            it.updateTime shouldBe "2043-01-26 13:59:27".toLocalDateTime()
        }

        postman.get("/api/person/67").shouldFailed.withError("用户 67 不存在或已被删除")
    }

    @Test
    fun create() {
        // 禁止创建重名用户
        postman.post("/api/person", mapOf("name" to "root", "password" to "", "teamIds" to listOf(1), "email" to ""))
            .shouldFailed.withError("用户 root 已存在")

        val nextPersonId = 180
        val name = "test_create"
        val password = "test_password"
        val teamIds = setOf(131, 127)
        val email = "test_create@woden.com"

        postman.post("/api/person", mapOf("name" to name, "password" to password, "teamIds" to teamIds, "email" to email))
            .shouldSuccess.get<PersonDTO>("person").withExpect {
            it.id shouldBe nextPersonId
            it.name shouldBe name
            it.email shouldBe email
            it.teams shouldContainExactlyInAnyOrder teamIds
        }

        postman.get("/api/person/$nextPersonId").shouldSuccess.get<PersonDTO>("person").withExpect {
            it.teams shouldContainExactlyInAnyOrder teamIds
            it.name shouldBe name
            it.email shouldBe email
        }

        val token = postman.post("/api/login", mapOf("name" to name, "password" to password))
            .shouldSuccess.get<String>("token").toString()
        PersonService.getPersonByToken(token)?.name shouldBe name
    }

    @Test
    fun updateName() {
        val oldName = "root"
        val newName = "root_new_name"

        postman.put("/api/person/1", mapOf("name" to "guest")).shouldFailed.withError("用户 guest 已存在")

        postman.post("/api/login", mapOf("name" to oldName, "password" to "root")).shouldSuccess
        postman.put("/api/person/1", mapOf("name" to newName)).shouldSuccess.get<PersonDTO>("person").withExpect {
            it.name shouldBe newName
        }
        postman.post("/api/login", mapOf("name" to oldName, "password" to "root")).shouldFailed.withError("登录失败")
        val token = postman.post("/api/login", mapOf("name" to newName, "password" to "root"))
            .shouldSuccess.get<String>("token")
        PersonService.getPersonByToken(token)?.name shouldBe newName

    }

    @Test
    fun updatePassword() {
        val oldPassword = "root"
        val newPassword = "root_new_password"

        postman.post("/api/login", mapOf("name" to "root", "password" to oldPassword)).shouldSuccess
        postman.put("/api/person/1", mapOf("password" to newPassword)).shouldSuccess
        postman.post("/api/login", mapOf("name" to "root", "password" to oldPassword)).shouldFailed.withError("登录失败")

        val token = postman.post("/api/login", mapOf("name" to "root", "password" to newPassword))
            .shouldSuccess.get<String>("token")
        PersonService.getPersonByToken(token)?.name shouldBe "root"
    }

    @Test
    fun updateEmail() {
        val newEmail = "new_email@woden.com"
        postman.put("/api/person/2", mapOf("email" to newEmail)).shouldSuccess.get<PersonDTO>("person").withExpect {
            it.email shouldBe newEmail
        }
        postman.get("/api/person/2").shouldSuccess.get<PersonDTO>("person").withExpect {
            it.email shouldBe newEmail
            it.updateTime shouldNotBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun updateteamIds() {
        val newteamIds = setOf(137, 149)
        postman.put("/api/person/2", mapOf("teamIds" to newteamIds)).shouldSuccess.get<PersonDTO>("person").withExpect {
            it.teams shouldContainExactlyInAnyOrder newteamIds
        }
        postman.get("/api/person/2").shouldSuccess.get<PersonDTO>("person").withExpect {
            it.teams shouldContainExactlyInAnyOrder newteamIds
            it.updateTime shouldNotBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun updateAll() {
        val newName = "new_name"
        val newPassword = "new_password"
        val newEmail = "new_email@woden.com"
        val newteamIds = setOf(137, 149)
        postman.put("/api/person/2", mapOf("name" to newName, "password" to newPassword, "email" to newEmail, "teamIds" to newteamIds))
            .shouldSuccess.get<PersonDTO>("person").withExpect {
            it.name shouldBe newName
            it.email shouldBe newEmail
            it.teams shouldContainExactlyInAnyOrder newteamIds
        }

        val token = postman.post("/api/login", mapOf("name" to newName, "password" to newPassword))
            .shouldSuccess.get<String>("token").toString()
        PersonService.getPersonByToken(token)?.name shouldBe newName

        postman.get("/api/person/2").shouldSuccess.get<PersonDTO>("person").withExpect {
            it.teams shouldContainExactlyInAnyOrder newteamIds
            it.email shouldBe newEmail
            it.updateTime shouldNotBe "2042-03-23 08:54:17".toLocalDateTime()
        }
    }

    @Test
    fun updateInvalidPerson() {
        postman.put("/api/person/4", mapOf("name" to "person who has been remove")).shouldFailed.withError("用户 4 不存在或已被删除")
        postman.put("/api/person/180", mapOf("name" to "person not exists")).shouldFailed.withError("用户 180 不存在或已被删除")
    }

    @Test
    fun remove() {
        postman.get("/api/person/2").shouldSuccess
        postman.delete("/api/person/2").shouldSuccess.withMessage("用户 2 已被删除")
        postman.get("/api/person/2").shouldFailed.withError("用户 2 不存在或已被删除")
        postman.delete("/api/person/4").shouldFailed.withError("用户 4 不存在或已被删除")

        postman.delete("/api/person/180").shouldFailed.withError("用户 180 不存在或已被删除")
    }
}