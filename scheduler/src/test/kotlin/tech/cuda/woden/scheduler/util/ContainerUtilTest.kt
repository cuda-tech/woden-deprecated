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
package tech.cuda.woden.scheduler.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class ContainerUtilTest : StringSpec({
    "获取硬件信息" {
        val systemInfo = ContainerUtil.systemInfo
        systemInfo.hostname.length shouldBeGreaterThan 0

        Thread.sleep(1000L) // 为了统计 CPU 使用率，需要等待一会
        val loadInfo = ContainerUtil.loadInfo
        loadInfo.cpu shouldBeGreaterThan 0
        loadInfo.memory shouldBeGreaterThan 0
        loadInfo.diskUsage shouldBeGreaterThan 0
    }
})