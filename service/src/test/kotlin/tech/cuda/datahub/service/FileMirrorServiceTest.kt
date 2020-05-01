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
import tech.cuda.datahub.service.dao.FileDAO
import tech.cuda.datahub.service.dao.FileMirrorDAO
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.toLocalDateTime
import java.lang.IllegalArgumentException

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class FileMirrorServiceTest : TestWithMaria({
    "根据 ID 查找" {
        val mirror = FileMirrorService.findById(4)
        mirror shouldNotBe null
        mirror!!
        mirror.content shouldBe "cmkiuyvxlipatzvvhdfieajehrxrhdruaxbgjimzhqqugmbsfualqezdpuqqvlmq"
        mirror.message shouldBe "kkidxecg"
        mirror.createTime shouldBe "2047-04-30 22:24:15".toLocalDateTime()
        mirror.updateTime shouldBe "2048-08-28 08:22:55".toLocalDateTime()

        // 已删除的
        FileMirrorService.findById(5) shouldBe null

        // 不存在的
        FileMirrorService.findById(301) shouldBe null
    }

    "分页查询" {
        val validCount = 13
        val pageSize = 5
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(FileMirrorService.listing(3, page, pageSize)) {
                val (mirrors, count) = this
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe 13
            }
        }
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validCount = 24
        var pageSize = 5
        var queryTimes = validCount / pageSize + 1
        var lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(FileMirrorService.listing(16, page, pageSize, " ")) {
                val (mirrors, count) = this
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
            with(FileMirrorService.listing(16, page, pageSize, " NULL ")) {
                val (mirrors, count) = this
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
            with(FileMirrorService.listing(16, page, pageSize, " a")) {
                val (mirrors, count) = this
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
            with(FileMirrorService.listing(16, page, pageSize, " a null")) {
                val (mirrors, count) = this
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
            with(FileMirrorService.listing(16, page, pageSize, "a b")) {
                val (mirrors, count) = this
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
            with(FileMirrorService.listing(16, page, pageSize, "a null b")) {
                val (mirrors, count) = this
                mirrors.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    "创建镜像" {
        val mirror = FileMirrorService.create(2, "create mirror")
        mirror.id shouldBe 301
        mirror.message shouldBe "create mirror"
        mirror.content shouldBe "xwuocwyldfswbdwbnkpizvuhokfhhbwrmykqlgtpqkrzuatixnavciilmbkyxnuw"

        shouldThrow<NotFoundException> {
            FileMirrorService.create(5, "nothing")
        }.message shouldBe "文件节点 5 不存在或已被删除"

        shouldThrow<NotFoundException> {
            FileMirrorService.create(70, "nothing")
        }.message shouldBe "文件节点 70 不存在或已被删除"

        shouldThrow<IllegalArgumentException> {
            FileMirrorService.create(1, "nothing")
        }.message shouldBe "文件夹无法获取 content"
    }

    "删除镜像" {
        FileMirrorService.findById(4) shouldNotBe null
        FileMirrorService.remove(4)
        FileMirrorService.findById(4) shouldBe null

        shouldThrow<NotFoundException> {
            FileMirrorService.remove(5)
        }.message shouldBe "镜像 5 不存在或已被删除"

        shouldThrow<NotFoundException> {
            FileMirrorService.remove(301)
        }.message shouldBe "镜像 301 不存在或已被删除"
    }

}, FileMirrorDAO, FileDAO)