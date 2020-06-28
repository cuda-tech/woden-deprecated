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
package tech.cuda.datahub.scheduler.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class MachineUtilTest : StringSpec({
    "获取硬件信息" {
        val systemInfo = MachineUtil.systemInfo
        systemInfo.hostname.length shouldBeGreaterThan 0
        systemInfo.mac shouldContain Regex("([A-F0-9]{2}-){5}[A-F0-9]{2}")
        systemInfo.ip.split(".").map { it.toInt() }.map {
            it shouldBeGreaterThan 0
            it shouldBeLessThan 256
            it
        }.size shouldBe 4

        Thread.sleep(1000L) // 为了统计 CPU 使用率，需要等待一会
        val loadInfo = MachineUtil.loadInfo
        loadInfo.cpu shouldBeGreaterThan 0
        loadInfo.memory shouldBeGreaterThan 0
        loadInfo.diskUsage shouldBeGreaterThan 0
    }
})