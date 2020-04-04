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
package tech.cuda.datahub.webserver.controller

import tech.cuda.datahub.service.SchemaUtils
import tech.cuda.datahub.webserver.tools.Postman
import tech.cuda.datahub.webserver.tools.RestfulTestToolbox
import org.junit.jupiter.api.*
import tech.cuda.datahub.webserver.tools.TablesMocker

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class FileMirrorControllerTest : RestfulTestToolbox() {

    @BeforeEach
    fun rebuildDB() {
        SchemaUtils.rebuildDB()
        TablesMocker.mock(listOf("users", "files", "groups", "file_mirrors"))
        this.postman = Postman(template)
        this.postman.login()
    }

    @Test
    fun listing() {
        val validCount = 13
        postman.get("/api/file/3/mirror").shouldSuccess.thenGetData.andCheckCount(validCount)
            .thenGetListOf("fileMirrors").andCheckSize(validCount)

        val pageSize = 5
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            postman.get("/api/file/3/mirror", mapOf("page" to page, "pageSize" to pageSize)).shouldSuccess
                .thenGetData.andCheckCount(validCount)
                .thenGetListOf("fileMirrors").andCheckSize(if (page == queryTimes) lastPageCount else pageSize)
        }
    }

    @Test
    fun search() {
        // 提供空或 null 的相似词
        var validCount = 24
        var pageSize = 5
        var queryTimes = validCount / pageSize + 1
        var lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to null)).shouldSuccess
                .thenGetData.andCheckCount(validCount)
                .thenGetListOf("fileMirrors").andCheckSize(if (page == queryTimes) lastPageCount else pageSize)

            postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to "  ")).shouldSuccess
                .thenGetData.andCheckCount(validCount)
                .thenGetListOf("fileMirrors").andCheckSize(if (page == queryTimes) lastPageCount else pageSize)
        }

        // 提供 1 个相似词
        validCount = 9
        pageSize = 7
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to " a")).shouldSuccess
                .thenGetData.andCheckCount(validCount)
                .thenGetListOf("fileMirrors").andCheckSize(if (page == queryTimes) lastPageCount else pageSize)
        }

        // 提供 2 个相似词
        validCount = 2
        pageSize = 1
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to " a b")).shouldSuccess
                .thenGetData.andCheckCount(validCount)
                .thenGetListOf("fileMirrors").andCheckSize(if (page == queryTimes) lastPageCount else pageSize)
        }
    }

    @Test
    fun find() {
        postman.get("/api/file/14/mirror/4").shouldSuccess.thenGetData.thenGetItem("fileMirror").withExpect {
            it["content"] shouldBe "cmkiuyvxlipatzvvhdfieajehrxrhdruaxbgjimzhqqugmbsfualqezdpuqqvlmq"
            it["message"] shouldBe "kkidxecg"
            it["createTime"] shouldBe "2047-04-30 22:24:15"
            it["updateTime"] shouldBe "2048-08-28 08:22:55"
        }

        postman.get("/api/file/14/mirror/5").shouldFailed.withNotFoundError("file mirror 5")
    }

    @Test
    fun create() {
        val nextId = 301
        postman.post("/api/file/2/mirror", mapOf("message" to "create mirror"))
            .shouldSuccess.thenGetData.thenGetItem("fileMirror").withExpect {
            it["id"] shouldBe nextId
            it["fileId"] shouldBe 2
            it["content"] shouldBe "xwuocwyldfswbdwbnkpizvuhokfhhbwrmykqlgtpqkrzuatixnavciilmbkyxnuw"
            it["message"] shouldBe "create mirror"
        }
        postman.get("/api/file/2/mirror/301").shouldSuccess.thenGetData.thenGetItem("fileMirror").withExpect {
            it["content"] shouldBe "xwuocwyldfswbdwbnkpizvuhokfhhbwrmykqlgtpqkrzuatixnavciilmbkyxnuw"
            it["message"] shouldBe "create mirror"
        }

        // 已删除的文件
        postman.post("/api/file/5/mirror", mapOf("message" to "create mirror")).shouldFailed.withNotFoundError("file 5")

        // 不存在的文件
        postman.post("/api/file/70/mirror", mapOf("message" to "create mirror")).shouldFailed.withNotFoundError("file 70")

        // 目录
        postman.post("/api/file/56/mirror", mapOf("message" to "create mirror")).shouldFailed.withIllegalArgumentError("目录不允许创建镜像")
    }

    @Test
    fun remove() {
        postman.get("/api/file/14/mirror/4").shouldSuccess
        postman.delete("/api/file/14/mirror/4").shouldSuccess.withMessage("file mirror 4 has been removed")
        postman.get("/api/file/14/mirror/4").shouldFailed.withNotFoundError("file mirror 4")

        // 已删除的镜像
        postman.delete("/api/file/18/mirror/5").shouldFailed.withNotFoundError("file mirror 5")

        // 不匹配的镜像
        postman.delete("/api/file/14/mirror/5").shouldFailed.withNotFoundError("file mirror 5")

        // 不存在的镜像
        postman.delete("/api/file/14/mirror/301").shouldFailed.withNotFoundError("file mirror 301")

    }
}