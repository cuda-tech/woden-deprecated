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

import io.kotest.matchers.shouldBe
import tech.cuda.datahub.scheduler.TestWithDistribution
import tech.cuda.datahub.scheduler.livy.mocker.LivyServer
import tech.cuda.datahub.service.InstanceService
import tech.cuda.datahub.service.po.dtype.InstanceStatus

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class InstanceTrackerTest : TestWithDistribution("machines", "jobs", "tasks", "files", "file_mirrors") {

    @Test
    fun testCreateInstanceForReadyBashJob() = supposeImMachine(1) {
        // (Job Id = 4, task ID = 35, mirror ID = 235, File ID = 13)
        val machineTracker = MachineTracker(afterStarted = {
            val instanceTracker = InstanceTracker(it, afterStarted = {
                val (instances, count) = InstanceService.listing(pageId = 1, pageSize = 1000, jobId = 4, status = InstanceStatus.RUNNING)
                count shouldBe 1
                val instance = instances.first()
                instance.id shouldBe 1
            })
            instanceTracker.start()
            Thread.sleep(1000)
            instanceTracker.cancelAndAwait()
            val instance = InstanceService.findById(1)!!
            instance.status shouldBe InstanceStatus.SUCCESS
            instance.log shouldBe "hello bash instance\n"
        })
        machineTracker.start()
        machineTracker.cancelAndAwait()
    }

    @Test
    fun testCreateInstanceForReadyPythonJob() = supposeImMachine(3) {
        // (Job Id = 6, task ID = 59, mirror ID = 253, File ID = 15)
        val machineTracker = MachineTracker(afterStarted = {
            val instanceTracker = InstanceTracker(it, devMode = true, afterStarted = {
                val (instances, count) = InstanceService.listing(pageId = 1, pageSize = 1000, jobId = 6, status = InstanceStatus.RUNNING)
                count shouldBe 1
                val instance = instances.first()
                instance.id shouldBe 1
            })
            instanceTracker.start()
            Thread.sleep(1000)
            instanceTracker.cancelAndAwait()
            val instance = InstanceService.findById(1)!!
            instance.status shouldBe InstanceStatus.SUCCESS
            instance.log shouldBe "hello python instance\n"
        })
        machineTracker.start()
        machineTracker.cancelAndAwait()
    }

    @Test
    fun testCreateInstanceForReadySparkSqlJob() = supposeImMachine(10) {
        // (Job Id = 16, task ID = 49, mirror ID = 290, File ID = 7)
        LivyServer.start()
        val machineTracker = MachineTracker(afterStarted = {
            val instanceTracker = InstanceTracker(it, afterStarted = {
                val (instances, count) = InstanceService.listing(pageId = 1, pageSize = 1000, jobId = 16, status = InstanceStatus.RUNNING)
                count shouldBe 1
                val instance = instances.first()
                instance.id shouldBe 1
            })
            instanceTracker.start()
            Thread.sleep(5000)
            instanceTracker.cancelAndAwait()
            val instance = InstanceService.findById(1)!!
            instance.status shouldBe InstanceStatus.SUCCESS
            instance.log shouldBe """
                {"schema":{"type":"struct","fields":[{"name":"hello world","type":"string","nullable":false,"metadata":{}}]},"data":[["hello world"]]}
            """.trimIndent()
        })
        machineTracker.start()
        machineTracker.cancelAndAwait()
        LivyServer.start()
    }

}