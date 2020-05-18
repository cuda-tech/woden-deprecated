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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.datahub.service.dto.MachineDTO
import tech.cuda.datahub.toLocalDateTime
import tech.cuda.datahub.webserver.RestfulTestToolbox


/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
open class MachineControllerTest : RestfulTestToolbox("users", "machines") {

    @Test
    fun listing() {
        val validCount = 188
        with(postman.get("/api/machine").shouldSuccess) {
            val machines = this.getList<MachineDTO>("machines")
            val count = this.get<Int>("count")
            machines.size shouldBe validCount
            count shouldBe validCount
        }

        val pageSize = 13
        val queryTimes = validCount / pageSize + 1
        val lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/machine", mapOf("page" to page, "pageSize" to pageSize)).shouldSuccess) {
                val machines = this.getList<MachineDTO>("machines")
                val count = this.get<Int>("count")
                machines.size shouldBe if (page == queryTimes) lastPageCount else pageSize
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
            with(postman.get("/api/machine", mapOf("page" to page, "pageSize" to pageSize, "like" to null)).shouldSuccess) {
                val machines = this.getList<MachineDTO>("machines")
                val count = this.get<Int>("count")
                machines.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(postman.get("/api/machine", mapOf("page" to page, "pageSize" to pageSize, "like" to "  ")).shouldSuccess) {
                val machines = this.getList<MachineDTO>("machines")
                val count = this.get<Int>("count")
                machines.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 1 个相似词
        validCount = 58
        pageSize = 13
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/machine", mapOf("page" to page, "pageSize" to pageSize, "like" to " a ")).shouldSuccess) {
                val machines = this.getList<MachineDTO>("machines")
                val count = this.get<Int>("count")
                machines.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 2 个相似词
        validCount = 15
        pageSize = 4
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(postman.get("/api/machine", mapOf("page" to page, "pageSize" to pageSize, "like" to " a b")).shouldSuccess) {
                val machines = this.getList<MachineDTO>("machines")
                val count = this.get<Int>("count")
                machines.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }
    }

    @Test
    fun create() {
        val newIP = "192.168.1.1"
        val nextId = 247

        postman.post("/api/machine", mapOf("ip" to newIP)).shouldSuccess.get<MachineDTO>("machine").withExpect {
            it.id shouldBe nextId
            it.hostname shouldBe ""
            it.ip shouldBe newIP
            it.mac shouldBe ""
            it.cpuLoad shouldBe 0
            it.memLoad shouldBe 0
            it.diskUsage shouldBe 0
        }
    }

    @Test
    fun update() {
        // 只更新 hostname
        postman.put("/api/machine/1", mapOf("hostname" to "new host name")).shouldSuccess.get<MachineDTO>("machine").withExpect {
            it.hostname shouldBe "new host name"
            it.ip shouldBe "107.116.90.29"
            it.updateTime shouldNotBe "2032-06-08 19:36:03".toLocalDateTime()
        }

        // 只更新 IP
        postman.put("/api/machine/3", mapOf("ip" to "192.168.1.1")).shouldSuccess.get<MachineDTO>("machine").withExpect {
            it.hostname shouldBe "nknvleif"
            it.ip shouldBe "192.168.1.1"
            it.updateTime shouldNotBe "2036-03-31 18:40:59".toLocalDateTime()
        }

        // 更新 hostname, IP
        postman.put("/api/machine/5", mapOf("hostname" to "anything", "ip" to "192.168.1.2")).shouldSuccess.get<MachineDTO>("machine").withExpect {
            it.hostname shouldBe "anything"
            it.ip shouldBe "192.168.1.2"
            it.updateTime shouldNotBe "2006-05-14 03:39:26".toLocalDateTime()
        }

        // 更新已删除的机器信息
        postman.put("/api/machine/2", mapOf("hostname" to "nothing"))
            .shouldFailed.withError("调度服务器 2 不存在或已被删除")

        // 更新不存在的机器信息
        postman.put("/api/machine/247", mapOf("hostname" to "nothing"))
            .shouldFailed.withError("调度服务器 247 不存在或已被删除")
    }

    @Test
    fun remove() {
        postman.delete("/api/machine/11").shouldSuccess.withMessage("调度服务器 11 已被删除")
        postman.delete("/api/machine/12").shouldFailed.withError("调度服务器 12 不存在或已被删除")
        postman.delete("/api/machine/247").shouldFailed.withError("调度服务器 247 不存在或已被删除")
    }
}