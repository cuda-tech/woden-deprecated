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
package tech.cuda.woden.scheduler.tracker

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import tech.cuda.woden.common.service.toLocalDateTime
import tech.cuda.woden.common.utils.SystemUtil
import tech.cuda.woden.scheduler.TestWithDistribution

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class ContainerTrackerTest : TestWithDistribution("container") {

    @Test
    fun testStart() {
        mockkObject(SystemUtil)
        every { SystemUtil.hostName } returns "nknvleif"
        every { SystemUtil.isWindows } returns false
        val containerTracker = ContainerTracker(afterStarted = {
            it.container.id shouldBe 3
            it.container.hostname shouldBe "nknvleif"
            it.container.cpuLoad shouldBeGreaterThan 0
            it.container.memLoad shouldBeGreaterThan 0
            it.container.diskUsage shouldBeGreaterThan 0
            it.container.updateTime shouldNotBe "2036-03-31 18:40:59".toLocalDateTime()
            it.cancel()
        })
        containerTracker.start()
        containerTracker.join()
        unmockkObject(SystemUtil)
    }

    @Test
    fun testStartWhenNotRegister() {
        mockkObject(SystemUtil)
        every { SystemUtil.hostName } returns "HOSTNAME"
        every { SystemUtil.isWindows } returns false
        val containerTracker = ContainerTracker(afterStarted = {
            it.container.id shouldBe 247
            it.container.hostname shouldBe "HOSTNAME"
            it.container.cpuLoad shouldBeGreaterThan 0
            it.container.memLoad shouldBeGreaterThan 0
            it.container.diskUsage shouldBeGreaterThan 0
            it.cancel()
        })
        containerTracker.start()
        containerTracker.join()
        unmockkObject(SystemUtil)
    }

}
