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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import tech.cuda.woden.scheduler.TestWithDistribution
import tech.cuda.woden.common.service.InstanceService
import tech.cuda.woden.common.service.po.dtype.InstanceStatus
import tech.cuda.woden.scheduler.runner.EnvSetter

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class InstanceTrackerTest : TestWithDistribution("containers", "jobs", "tasks", "files", "file_mirrors") {

    @Test
    fun testCreateInstanceForReadyBashJob() = supposeImContainer(1) {
        EnvSetter.autoConvertPathFromWindows2WSL {
            // (Job Id = 4, task ID = 35, mirror ID = 235, File ID = 13)
            val containerTracker = ContainerTracker(afterStarted = {
                val instanceTracker = InstanceTracker(it.container, afterStarted = { self ->
                    val (instances, count) = InstanceService.listing(pageId = 1, pageSize = 1000, jobId = 4, status = InstanceStatus.RUNNING)
                    count shouldBe 1
                    val instance = instances.first()
                    instance.id shouldBe 1
                    self.cancel()
                })
                instanceTracker.start()
                Thread.sleep(1000)
                instanceTracker.join()
                val instance = InstanceService.findById(1)!!
                instance.status shouldBe InstanceStatus.SUCCESS
                instance.log shouldBe "hello bash instance\n"
                it.cancel()
            })
            containerTracker.start()
            containerTracker.join()
        }
    }

    @Test
    fun testCreateInstanceForReadyPythonJob() = supposeImContainer(3) {
        // (Job Id = 6, task ID = 59, mirror ID = 253, File ID = 15)
        val containerTracker = ContainerTracker(afterStarted = {
            val instanceTracker = InstanceTracker(it.container, afterStarted = { self ->
                val (instances, count) = InstanceService.listing(pageId = 1, pageSize = 1000, jobId = 6, status = InstanceStatus.RUNNING)
                count shouldBe 1
                val instance = instances.first()
                instance.id shouldBe 1
                self.cancel()
            })
            instanceTracker.start()
            Thread.sleep(1000)
            instanceTracker.join()
            val instance = InstanceService.findById(1)!!
            instance.status shouldBe InstanceStatus.SUCCESS
            instance.log shouldBe "hello python instance\n"
            it.cancel()
        })
        containerTracker.start()
        containerTracker.join()
    }

    @Test
    fun testCreateInstanceForReadySparkSqlJob() = supposeImContainer(10) {
        EnvSetter.autoSetLocalAndDerbyDir {
            // (Job Id = 16, task ID = 49, mirror ID = 290, File ID = 7)
            val containerTracker = ContainerTracker(afterStarted = { it ->
                val instanceTracker = InstanceTracker(it.container, afterStarted = { self ->
                    val (instances, count) = InstanceService.listing(pageId = 1, pageSize = 1000, jobId = 16, status = InstanceStatus.RUNNING)
                    count shouldBe 1
                    val instance = instances.first()
                    instance.id shouldBe 1
                    self.cancel()
                })
                instanceTracker.start()
                Thread.sleep(5000)
                instanceTracker.join()
                val instance = InstanceService.findById(1)!!
                instance.status shouldBe InstanceStatus.SUCCESS
                instance.log shouldContain "string_column\nhello world"
                it.cancel()
            })
            containerTracker.start()
            containerTracker.join()
        }
    }

}