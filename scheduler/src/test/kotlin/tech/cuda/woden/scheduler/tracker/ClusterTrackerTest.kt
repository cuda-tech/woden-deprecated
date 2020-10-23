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

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import tech.cuda.woden.scheduler.TestWithDistribution
import tech.cuda.woden.service.MachineService
import tech.cuda.woden.service.po.dtype.MachineRole

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class ClusterTrackerTest : TestWithDistribution("machines") {

    @Test
    fun testEnsureOnlyOneMaster() {
        MachineService.findById(1)!!.role shouldBe MachineRole.MASTER
        MachineService.listingActiveMaster().second shouldBe 3
        MachineService.listingActiveMaster().first.map { it.id } shouldContainExactlyInAnyOrder listOf(1, 3, 5)
        val tracker = ClusterTracker(MachineService.findById(1)!!, afterStarted = {
            val (masters, count) = MachineService.listingActiveMaster()
            count shouldBe 1
            masters.first() shouldBe MachineService.findById(58)!!
        })
        tracker.start()
        tracker.cancelAndAwait()
        MachineService.findById(1)!!.role shouldBe MachineRole.SLAVE
    }

    @Test
    fun testCheckMasterAlive() = supposeNowIs(2020, 1, 1, 0, 0, 30) {
        MachineService.listingActiveMaster().second shouldBe 3
        val tracker = ClusterTracker(MachineService.findById(1)!!, afterStarted = {
            MachineService.listingActiveMaster().second shouldBe 1
        })
        tracker.start()
        tracker.cancelAndAwait()
    }

    @Test
    fun testCheckSlave() = supposeNowIs(2020, 1, 1, 0, 0, 30) {
        MachineService.listingActiveMaster().second shouldBe 3
        MachineService.listingActiveSlave().second shouldBe 156
        val tracker = ClusterTracker(MachineService.findById(58)!!, afterStarted = {
            MachineService.listingActiveSlave().second shouldBe 102 + 3 // 原有的 102 个活跃 slave + 3 个降级的 master
        })
        tracker.start()
        tracker.cancelAndAwait()
    }
}