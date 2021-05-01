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

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.common.service.dto.FileContentDTO
import tech.cuda.woden.common.service.dto.FileDTO
import tech.cuda.woden.common.service.po.dtype.FileType
import tech.cuda.woden.common.service.toLocalDateTime
import tech.cuda.woden.webserver.RestfulTestToolbox

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
open class FileControllerTest : RestfulTestToolbox("person", "team", "file", "person_team_mapping") {

    @Test
    fun listing() {
        // 一级目录
        with(postman.get("/api/file", mapOf("parentId" to 1)).shouldSuccess) {
            val files = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            files.size shouldBe 5
            count shouldBe 5
            files.map { it.type } shouldContainExactly listOf(FileType.DIR, FileType.SPARK_SQL, FileType.SPARK_SQL, FileType.SPARK_SQL, FileType.SPARK_SHELL)
            files.map { it.name } shouldContainExactly listOf("zwgjydgn", "kniovyqn", "ladlehnr", "yoglnkyc", "jldwzlys")
            files.forEach {
                it.teamId shouldBe 1
                it.parentId shouldBe 1
            }
        }

        // 二级目录
        with(postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess) {
            val files = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            files.size shouldBe 3
            count shouldBe 3
            files.map { it.type } shouldContainExactly listOf(FileType.DIR, FileType.SPARK_SQL, FileType.SPARK_SHELL)
            files.map { it.name } shouldContainExactly listOf("zvdjsdhz", "yijlstlq", "yzhamcqc")
            files.forEach {
                it.teamId shouldBe 1
                it.parentId shouldBe 4
            }
        }
    }

    @Test
    fun findRootDir() {
        postman.get("/api/file/root", mapOf("teamId" to 6)).shouldSuccess.get<FileDTO>("file").withExpect {
            it.name shouldBe "hhkjnqwc"
            it.ownerId shouldBe 124
        }

        // 已删除的项目组
        postman.get("/api/file/root", mapOf("teamId" to 7)).shouldFailed.withError("项目组 7 根目录 不存在或已被删除")

        // 不存在的项目组
        postman.get("/api/file/root", mapOf("teamId" to 40)).shouldFailed.withError("项目组 40 根目录 不存在或已被删除")
    }

    @Test
    fun getParent() {
        // 三层父节点
        with(postman.get("/api/file/28/parent").shouldSuccess) {
            val parent = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            parent.size shouldBe count
            parent.map { it.name } shouldContainExactly listOf("root_project", "zwgjydgn", "zvdjsdhz")
        }

        // 两层父节点
        with(postman.get("/api/file/42/parent").shouldSuccess) {
            val parent = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            parent.size shouldBe count
            parent.map { it.name } shouldContainExactly listOf("root_project", "zwgjydgn")
        }
        with(postman.get("/api/file/27/parent").shouldSuccess) {
            val parent = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            parent.size shouldBe count
            parent.map { it.name } shouldContainExactly listOf("root_project", "zwgjydgn")
        }

        // 一层父节点
        with(postman.get("/api/file/6/parent").shouldSuccess) {
            val parent = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            parent.size shouldBe count
            parent.map { it.name } shouldContainExactly listOf("root_project")
        }
        with(postman.get("/api/file/4/parent").shouldSuccess) {
            val parent = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            parent.size shouldBe count
            parent.map { it.name } shouldContainExactly listOf("root_project")
        }

        // 根目录
        postman.get("/api/file/1/parent").shouldSuccess.getList<FileDTO>("files").size shouldBe 0

        // 已删除的文件
        postman.get("/api/file/5/parent").shouldFailed.withError("文件节点 5 不存在或已被删除")

        // 不存在的文件
        postman.get("/api/file/70/parent").shouldFailed.withError("文件节点 70 不存在或已被删除")
    }

    @Test
    fun getContent() {
        postman.get("/api/file/2/content").shouldSuccess.get<FileContentDTO>("content").content shouldBe
            "xwuocwyldfswbdwbnkpizvuhokfhhbwrmykqlgtpqkrzuatixnavciilmbkyxnuw"
        // 已删除的文件
        postman.get("/api/file/5/content").shouldFailed.withError("文件节点 5 不存在或已被删除")
        // 不存在的文件
        postman.get("/api/file/70/content").shouldFailed.withError("文件节点 70 不存在或已被删除")
        // 目录
        postman.get("/api/file/4/content").shouldFailed.withError("文件夹 不允许 获取 内容")
    }

    @Test
    fun search() {
        with(postman.get("/api/file/search", mapOf("like" to " a  b   c    ")).shouldSuccess) {
            val files = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            count shouldBe 2
            files.map { it.name } shouldContainExactly listOf("bcmawkte", "lwbaccod")
        }

        with(postman.get("/api/file/search", mapOf("like" to "hh")).shouldSuccess) {
            val files = this.getList<FileDTO>("files")
            val count = this.get<Int>("count")
            count shouldBe 2
            files.map { it.name } shouldContainExactly listOf("hhkjnqwc", "ghxwtphh")
        }
    }

    @Test
    fun createDir() {
        postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess.get<Int>("count") shouldBe 3

        postman.post("/api/file", mapOf(
            "teamId" to 1,
            "name" to "test create",
            "type" to "DIR",
            "parentId" to 4)
        ).shouldSuccess.get<FileDTO>("file").withExpect {
            it.id shouldBe 70
            it.teamId shouldBe 1
            it.ownerId shouldBe 1
            it.name shouldBe "test create"
            it.type shouldBe FileType.DIR
            it.parentId shouldBe 4
        }

        with(postman.get("/api/file/search", mapOf("like" to "test create")).shouldSuccess) {
            val count = this.get<Int>("count")
            val files = this.getList<FileDTO>("files")
            count shouldBe 1
            files.size shouldBe 1
            files.first().withExpect {
                it.id shouldBe 70
                it.teamId shouldBe 1
                it.ownerId shouldBe 1
                it.name shouldBe "test create"
                it.type shouldBe FileType.DIR
                it.parentId shouldBe 4
            }
        }

        postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess.get<Int>("count") shouldBe 4

        // 创建同名文件夹
        postman.post("/api/file", mapOf(
            "teamId" to 1,
            "name" to "test create",
            "type" to "DIR",
            "parentId" to 4)
        ).shouldFailed.withError("文件夹 4 存在 文件类型 DIR 文件节点 test create")
    }

    @Test
    fun createFile() {
        postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess.get<Int>("count") shouldBe 3

        postman.post("/api/file", mapOf(
            "teamId" to 1,
            "name" to "test create",
            "type" to "SPARK_SQL",
            "parentId" to 4)
        ).shouldSuccess.get<FileDTO>("file").withExpect {
            it.id shouldBe 70
            it.teamId shouldBe 1
            it.ownerId shouldBe 1
            it.name shouldBe "test create"
            it.type shouldBe FileType.SPARK_SQL
            it.parentId shouldBe 4
        }

        postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess.get<Int>("count") shouldBe 4
        postman.get("/api/file/70/content").shouldSuccess.get<FileContentDTO>("content").content!!.length shouldBeGreaterThan 0

        with(postman.get("/api/file/search", mapOf("like" to "test create")).shouldSuccess) {
            val count = this.get<Int>("count")
            val files = this.getList<FileDTO>("files")
            count shouldBe 1
            files.size shouldBe 1
            files.first().withExpect {
                it.id shouldBe 70
                it.teamId shouldBe 1
                it.ownerId shouldBe 1
                it.name shouldBe "test create"
                it.type shouldBe FileType.SPARK_SQL
                it.parentId shouldBe 4
            }
        }

        postman.post("/api/file", mapOf(
            "teamId" to 1,
            "name" to "test create",
            "type" to "SPARK_SQL",
            "parentId" to 4)
        ).shouldFailed.withError("文件夹 4 存在 文件类型 SPARK_SQL 文件节点 test create")
    }

    @Test
    fun update() {
        // 更新名称
        postman.get("/api/file/search", mapOf("like" to "test update")).shouldSuccess.get<Int>("count") shouldBe 0
        postman.put("/api/file/28", mapOf("name" to "test update")).shouldSuccess.get<FileDTO>("file").withExpect {
            it.name shouldBe "test update"
            it.updateTime shouldNotBe "2046-07-28 08:28:16".toLocalDateTime()
        }
        postman.get("/api/file/search", mapOf("like" to "test update")).shouldSuccess.get<Int>("count") shouldBe 1

        // 更新归属者
        postman.get("/api/file/search", mapOf("like" to "test update")).shouldSuccess
            .getList<FileDTO>("files").first().ownerId shouldBe 91
        postman.put("/api/file/28", mapOf("ownerId" to 5)).shouldSuccess.get<FileDTO>("file").ownerId shouldBe 5
        postman.get("/api/file/search", mapOf("like" to "test update")).shouldSuccess
            .getList<FileDTO>("files").first().ownerId shouldBe 5


        // 更新父节点
        postman.get("/api/file/search", mapOf("like" to "yoglnkyc")).shouldSuccess
            .getList<FileDTO>("files").first().parentId shouldBe 1
        postman.put("/api/file/3", mapOf("parentId" to 4)).shouldSuccess.get<FileDTO>("file").parentId shouldBe 4
        postman.get("/api/file/search", mapOf("like" to "yoglnkyc")).shouldSuccess
            .getList<FileDTO>("files").first().parentId shouldBe 4

        // 更新异常
        postman.put("/api/file/11", mapOf("ownerId" to 1)).shouldFailed.withError("文件节点 11 不存在或已被删除")
        postman.put("/api/file/70", mapOf("ownerId" to 1)).shouldFailed.withError("文件节点 70 不存在或已被删除")
        postman.put("/api/file/56", mapOf("ownerId" to 2)).shouldFailed.withError("根目录 禁止更新")
    }

    @Test
    fun remove() {
        postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess.get<Int>("count") shouldBe 3
        postman.delete("/api/file/42").shouldSuccess.withMessage("文件节点 42 已被删除")
        postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess.get<Int>("count") shouldBe 2

        // 已删除
        postman.delete("/api/file/11").shouldFailed.withError("文件节点 11 不存在或已被删除")

        // 不存在
        postman.delete("/api/file/70").shouldFailed.withError("文件节点 70 不存在或已被删除")

        // 根目录
        postman.delete("/api/file/56").shouldFailed.withError("根目录 禁止删除")
    }

    @Test
    fun removeDir() {
        postman.delete("/api/file/4").shouldSuccess.withMessage("文件节点 4 已被删除")

        // 子节点应被删除
        postman.get("/api/file", mapOf("parentId" to 4)).shouldSuccess.get<Int>("count") shouldBe 0

        // 孙节点应被删除
        postman.get("/api/file", mapOf("parentId" to 27)).shouldSuccess.get<Int>("count") shouldBe 0

        // 兄弟节点不应被删除
        postman.get("/api/file", mapOf("parentId" to 1)).shouldSuccess.get<Int>("count") shouldBe 4
    }

}