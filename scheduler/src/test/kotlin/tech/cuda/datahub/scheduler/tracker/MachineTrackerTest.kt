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
package tech.cuda.datahub.scheduler.tracker

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import tech.cuda.datahub.scheduler.TestWithDistribution
import tech.cuda.datahub.scheduler.util.MachineUtil
import tech.cuda.datahub.toLocalDateTime

class MachineTrackerTest : TestWithDistribution("machines") {

    @Test
    fun testStart() = runBlocking {
        mockkObject(MachineUtil)
        every { MachineUtil.systemInfo } returns MachineUtil.SystemInfo("17.212.169.100", "9E-EE-49-FA-00-F4", "nknvleif")
        val machineTracker = MachineTracker(afterStarted = {
            it.id shouldBe 3
            it.ip shouldBe "17.212.169.100"
            it.mac shouldBe "9E-EE-49-FA-00-F4"
            it.hostname shouldBe "nknvleif"
            it.cpuLoad shouldBeGreaterThan 0
            it.memLoad shouldBeGreaterThan 0
            it.diskUsage shouldBeGreaterThan 0
            it.updateTime shouldNotBe "2036-03-31 18:40:59".toLocalDateTime()
        })
        machineTracker.start()
        machineTracker.cancelAndAwait()
    }

    @Test
    fun testStartWhenIpAndHostChange() = runBlocking {
        mockkObject(MachineUtil)
        every { MachineUtil.systemInfo } returns MachineUtil.SystemInfo("192.168.1.1", "9E-EE-49-FA-00-F4", "HOSTNAME")
        val machineTracker = MachineTracker(afterStarted = {
            it.id shouldBe 3
            it.ip shouldBe "192.168.1.1"
            it.mac shouldBe "9E-EE-49-FA-00-F4"
            it.hostname shouldBe "HOSTNAME"
            it.cpuLoad shouldBeGreaterThan 0
            it.memLoad shouldBeGreaterThan 0
            it.diskUsage shouldBeGreaterThan 0
            it.updateTime shouldNotBe "2036-03-31 18:40:59".toLocalDateTime()
        })
        machineTracker.start()
        machineTracker.cancelAndAwait()
    }

    @Test
    fun testStartWhenNotRegister() = runBlocking {
        mockkObject(MachineUtil)
        every { MachineUtil.systemInfo } returns MachineUtil.SystemInfo("192.168.1.1", "01-23-45-67-89-AB", "HOSTNAME")
        val machineTracker = MachineTracker(afterStarted = {
            it.id shouldBe 247
            it.ip shouldBe "192.168.1.1"
            it.mac shouldBe "01-23-45-67-89-AB"
            it.hostname shouldBe "HOSTNAME"
            it.cpuLoad shouldBeGreaterThan 0
            it.memLoad shouldBeGreaterThan 0
            it.diskUsage shouldBeGreaterThan 0
        })
        machineTracker.start()
        machineTracker.cancelAndAwait()
    }

}
