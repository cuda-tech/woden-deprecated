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
import tech.cuda.woden.scheduler.TestWithDistribution
import tech.cuda.woden.scheduler.util.MachineUtil

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class MachineTrackerTest : TestWithDistribution("machines") {

    @Test
    fun testStart() {
        mockkObject(MachineUtil)
        every { MachineUtil.systemInfo } returns MachineUtil.SystemInfo("17.212.169.100", "9E-EE-49-FA-00-F4", "nknvleif", false)
        val machineTracker = MachineTracker(afterStarted = {
            it.machine.id shouldBe 3
            it.machine.ip shouldBe "17.212.169.100"
            it.machine.mac shouldBe "9E-EE-49-FA-00-F4"
            it.machine.hostname shouldBe "nknvleif"
            it.machine.cpuLoad shouldBeGreaterThan 0
            it.machine.memLoad shouldBeGreaterThan 0
            it.machine.diskUsage shouldBeGreaterThan 0
            it.machine.updateTime shouldNotBe "2036-03-31 18:40:59".toLocalDateTime()
            it.cancel()
        })
        machineTracker.start()
        machineTracker.join()
        unmockkObject(MachineUtil)
    }

    @Test
    fun testStartWhenIpAndHostChange() {
        mockkObject(MachineUtil)
        every { MachineUtil.systemInfo } returns MachineUtil.SystemInfo("192.168.1.1", "9E-EE-49-FA-00-F4", "HOSTNAME", false)
        val machineTracker = MachineTracker(afterStarted = {
            it.machine.id shouldBe 3
            it.machine.ip shouldBe "192.168.1.1"
            it.machine.mac shouldBe "9E-EE-49-FA-00-F4"
            it.machine.hostname shouldBe "HOSTNAME"
            it.machine.cpuLoad shouldBeGreaterThan 0
            it.machine.memLoad shouldBeGreaterThan 0
            it.machine.diskUsage shouldBeGreaterThan 0
            it.machine.updateTime shouldNotBe "2036-03-31 18:40:59".toLocalDateTime()
            it.cancel()
        })
        machineTracker.start()
        machineTracker.join()
        unmockkObject(MachineUtil)
    }

    @Test
    fun testStartWhenNotRegister() {
        mockkObject(MachineUtil)
        every { MachineUtil.systemInfo } returns MachineUtil.SystemInfo("192.168.1.1", "01-23-45-67-89-AB", "HOSTNAME", false)
        val machineTracker = MachineTracker(afterStarted = {
            it.machine.id shouldBe 247
            it.machine.ip shouldBe "192.168.1.1"
            it.machine.mac shouldBe "01-23-45-67-89-AB"
            it.machine.hostname shouldBe "HOSTNAME"
            it.machine.cpuLoad shouldBeGreaterThan 0
            it.machine.memLoad shouldBeGreaterThan 0
            it.machine.diskUsage shouldBeGreaterThan 0
            it.cancel()
        })
        machineTracker.start()
        machineTracker.join()
        unmockkObject(MachineUtil)
    }

}
