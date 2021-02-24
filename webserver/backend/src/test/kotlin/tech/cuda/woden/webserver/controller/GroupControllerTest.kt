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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.service.dto.GroupDTO
import tech.cuda.woden.service.toLocalDateTime
import tech.cuda.woden.webserver.RestfulTestToolbox

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
open class GroupControllerTest : RestfulTestToolbox("users", "groups") {

    @Test
    fun listing() {
        val validCount = 32
        with(postman.get("/api/group").shouldSuccess) {
            val groups = this.getList<GroupDTO>("groups")
            val count = this.get<Int>("count")
            groups.size shouldBe validCount
            count shouldBe validCount
        }

        val pageSize = 7
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/group", mapOf("page" to page, "pageSize" to pageSize)).shouldSuccess) {
                val groups = this.getList<GroupDTO>("groups")
                val count = this.get<Int>("count")
                groups.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun search() {
        // 提供空或 null 的相似词
        var validCount = 32
        var pageSize = 5
        var queryTimes = validCount / pageSize + 1
        var lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/group", mapOf("page" to page, "pageSize" to pageSize, "like" to null)).shouldSuccess) {
                val groups = this.getList<GroupDTO>("groups")
                val count = this.get<Int>("count")
                groups.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(postman.get("/api/group", mapOf("page" to page, "pageSize" to pageSize, "like" to "null")).shouldSuccess) {
                val groups = this.getList<GroupDTO>("groups")
                val count = this.get<Int>("count")
                groups.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供一个相似词
        validCount = 12
        pageSize = 5
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/group", mapOf("page" to page, "pageSize" to pageSize, "like" to " f")).shouldSuccess) {
                val groups = this.getList<GroupDTO>("groups")
                val count = this.get<Int>("count")
                groups.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供两个相似词
        validCount = 5
        pageSize = 2
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/group", mapOf("page" to page, "pageSize" to pageSize, "like" to " f r ")).shouldSuccess) {
                val groups = this.getList<GroupDTO>("groups")
                val count = this.get<Int>("count")
                groups.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun find() {
        postman.get("/api/group/23").shouldSuccess.get<GroupDTO>("group").withExpect {
            it.name shouldBe "kcwhynhd"
            it.createTime shouldBe "2044-11-11 15:27:26".toLocalDateTime()
            it.updateTime shouldBe "2047-07-02 20:28:57".toLocalDateTime()
        }
        postman.get("/api/group/39").shouldFailed.withError("项目组 39 不存在或已被删除")
        postman.get("/api/group/40").shouldFailed.withError("项目组 40 不存在或已被删除")
    }

    @Test
    fun create() {
        postman.login("guest", "guest")
        val nextGroupId = 40
        val name = "test_create"
        postman.post("/api/group", mapOf("name" to name)).shouldSuccess.get<GroupDTO>("group").withExpect {
            it.id shouldBe nextGroupId
            it.name shouldBe name
        }
        postman.get("/api/group/$nextGroupId").shouldSuccess.get<GroupDTO>("group").withExpect {
            it.id shouldBe nextGroupId
            it.name shouldBe name
        }
    }

    @Test
    fun update() {
        val newName = "new_name"
        postman.put("/api/group/25", mapOf("name" to newName)).shouldSuccess.get<GroupDTO>("group").withExpect {
            it.name shouldBe newName
            it.updateTime shouldNotBe "2051-07-10 20:16:48".toLocalDateTime()
        }
        postman.get("/api/group/25").shouldSuccess.get<GroupDTO>("group").withExpect {
            it.name shouldBe newName
            it.updateTime shouldNotBe "2051-07-10 20:16:48".toLocalDateTime()
        }
    }

    @Test
    fun remove() {
        postman.get("/api/group/15").shouldSuccess
        postman.delete("/api/group/15").shouldSuccess.withMessage("项目组 15 已被删除")
        postman.get("/api/group/15").shouldFailed.withError("项目组 15 不存在或已被删除")

        postman.delete("/api/group/18").shouldFailed.withError("项目组 18 不存在或已被删除")

        postman.delete("/api/group/40").shouldFailed.withError("项目组 40 不存在或已被删除")

    }
}