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
import tech.cuda.woden.common.service.dto.FileMirrorDTO
import tech.cuda.woden.common.service.toLocalDateTime
import tech.cuda.woden.webserver.RestfulTestToolbox

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
open class FileMirrorControllerTest : RestfulTestToolbox("users", "file_mirrors", "files") {

    @Test
    fun listing() {
        val validCount = 13
        with(postman.get("/api/file/3/mirror").shouldSuccess) {
            val mirrors = this.getList<FileMirrorDTO>("mirrors")
            val count = this.get<Int>("count")
            mirrors.size shouldBe validCount
            count shouldBe validCount
        }

        val pageSize = 5
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/file/3/mirror", mapOf("page" to page, "pageSize" to pageSize)).shouldSuccess) {
                val mirrors = this.getList<FileMirrorDTO>("mirrors")
                val count = this.get<Int>("count")
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
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
            with(postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to null)).shouldSuccess) {
                val mirrors = this.getList<FileMirrorDTO>("mirrors")
                val count = this.get<Int>("count")
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to "  ")).shouldSuccess) {
                val mirrors = this.getList<FileMirrorDTO>("mirrors")
                val count = this.get<Int>("count")
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 1 个相似词
        validCount = 9
        pageSize = 7
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to " a")).shouldSuccess) {
                val mirrors = this.getList<FileMirrorDTO>("mirrors")
                val count = this.get<Int>("count")
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 2 个相似词
        validCount = 2
        pageSize = 1
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/file/16/mirror", mapOf("page" to page, "pageSize" to pageSize, "like" to " a b")).shouldSuccess) {
                val mirrors = this.getList<FileMirrorDTO>("mirrors")
                val count = this.get<Int>("count")
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun find() {
        postman.get("/api/file/14/mirror/4").shouldSuccess.get<FileMirrorDTO>("mirror").withExpect {
            it.content shouldBe "cmkiuyvxlipatzvvhdfieajehrxrhdruaxbgjimzhqqugmbsfualqezdpuqqvlmq"
            it.message shouldBe "kkidxecg"
            it.createTime shouldBe "2047-04-30 22:24:15".toLocalDateTime()
            it.updateTime shouldBe "2048-08-28 08:22:55".toLocalDateTime()
        }

        postman.get("/api/file/14/mirror/5").shouldFailed.withError("文件镜像 5 不存在或已被删除")
    }

    @Test
    fun create() {
        val nextId = 301
        postman.post("/api/file/2/mirror", mapOf("message" to "create mirror")).shouldSuccess.get<FileMirrorDTO>("mirror").withExpect {
            it.id shouldBe nextId
            it.fileId shouldBe 2
            it.content shouldBe "xwuocwyldfswbdwbnkpizvuhokfhhbwrmykqlgtpqkrzuatixnavciilmbkyxnuw"
            it.message shouldBe "create mirror"
        }
        postman.get("/api/file/2/mirror/301").shouldSuccess.get<FileMirrorDTO>("mirror").withExpect {
            it.content shouldBe "xwuocwyldfswbdwbnkpizvuhokfhhbwrmykqlgtpqkrzuatixnavciilmbkyxnuw"
            it.message shouldBe "create mirror"
        }

        // 已删除的文件
        postman.post("/api/file/5/mirror", mapOf("message" to "create mirror")).shouldFailed.withError("文件节点 5 不存在或已被删除")

        // 不存在的文件
        postman.post("/api/file/70/mirror", mapOf("message" to "create mirror")).shouldFailed.withError("文件节点 70 不存在或已被删除")

        // 目录
        postman.post("/api/file/56/mirror", mapOf("message" to "create mirror")).shouldFailed.withError("文件夹 禁止创建镜像")
    }

    @Test
    fun remove() {
        postman.get("/api/file/14/mirror/4").shouldSuccess
        postman.delete("/api/file/14/mirror/4").shouldSuccess.withMessage("文件镜像 4 已被删除")
        postman.get("/api/file/14/mirror/4").shouldFailed.withError("文件镜像 4 不存在或已被删除")

        // 已删除的镜像
        postman.delete("/api/file/18/mirror/5").shouldFailed.withError("文件镜像 5 不存在或已被删除")

        // 不存在的镜像
        postman.delete("/api/file/14/mirror/301").shouldFailed.withError("文件镜像 301 不存在或已被删除")
    }
}