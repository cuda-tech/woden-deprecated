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
import tech.cuda.woden.common.service.dto.ContainerDTO
import tech.cuda.woden.webserver.RestfulTestToolbox

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
open class ContainerControllerTest : RestfulTestToolbox("users", "containers") {

    @Test
    fun listing() {
        val validCount = 188
        with(postman.get("/api/container").shouldSuccess) {
            val containers = this.getList<ContainerDTO>("containers")
            val count = this.get<Int>("count")
            containers.size shouldBe validCount
            count shouldBe validCount
        }

        val pageSize = 13
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/container", mapOf("page" to page, "pageSize" to pageSize)).shouldSuccess) {
                val containers = this.getList<ContainerDTO>("containers")
                val count = this.get<Int>("count")
                containers.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun search() {
        // 提供空或 null 的相似词
        var validCount = 188
        var pageSize = 13
        var queryTimes = validCount / pageSize + 1
        var lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/container", mapOf("page" to page, "pageSize" to pageSize, "like" to null)).shouldSuccess) {
                val containers = this.getList<ContainerDTO>("containers")
                val count = this.get<Int>("count")
                containers.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(postman.get("/api/container", mapOf("page" to page, "pageSize" to pageSize, "like" to "  ")).shouldSuccess) {
                val containers = this.getList<ContainerDTO>("containers")
                val count = this.get<Int>("count")
                containers.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 1 个相似词
        validCount = 58
        pageSize = 13
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/container", mapOf("page" to page, "pageSize" to pageSize, "like" to " a ")).shouldSuccess) {
                val containers = this.getList<ContainerDTO>("containers")
                val count = this.get<Int>("count")
                containers.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 2 个相似词
        validCount = 15
        pageSize = 4
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/container", mapOf("page" to page, "pageSize" to pageSize, "like" to " a b")).shouldSuccess) {
                val containers = this.getList<ContainerDTO>("containers")
                val count = this.get<Int>("count")
                containers.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }
}