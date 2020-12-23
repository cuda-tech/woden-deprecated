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
import io.mockk.every
import io.mockk.mockkObject
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.global.global
import tech.cuda.woden.config.Woden
import tech.cuda.woden.scheduler.TestWithDistribution
import tech.cuda.woden.service.JobService
import tech.cuda.woden.service.po.dtype.JobStatus
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class JobTrackerTest : TestWithDistribution("jobs", "tasks", "machines") {

    /**
     * 各调度格式的任务数
     * 周调度：1:6个, 2:9个, 3:7个, 4:5个, 5:10个, 6:6个, 7:6个
     * 天调度：73个
     * 小时调度: 48个
     *
     * 2010-08-06: 周五(10) + MONTH(1) + DAY(73) + HOUR(48*24) = 1236
     * 2010-08-07: 周六(6) + MONTH(2) + DAY(73) + HOUR(48*24) = 1233
     * 2010-08-08: 周日(6) + YEAR(1) + MONTH(1) + DAY(73) + HOUR(48*24) = 1233
     * 2010-08-09: 周一(6) + ONCE(1) + MONTH(1) + DAY(73) + HOUR(48*24) = 1233
     * 2010-08-10: 周二(9) + ONCE(1) + MONTH(4) + DAY(73) + HOUR(48*24) = 1239
     * 2010-08-11: 周三(7) + MONTH(2) + DAY(73) + HOUR(48*24) = 1234
     * 2010-08-12: 周四(5) + MONTH(1) + DAY(73) + HOUR(48*24) = 1231
     */
    @Test
    @Ignore
    fun testGenerateJob() {
        val expectCount = listOf(1236, 1233, 1233, 1233, 1239, 1234, 1231)
        (6..12).forEachIndexed { index, day ->
            supposeNowIs(2010, 8, day, 0, 1) {
                val now = LocalDateTime.now()
                JobService.listing(1, 10, status = JobStatus.INIT, after = now, before = now)
                    .second shouldBe 0
                val jobTracker = JobTracker(afterJobGenerated = {
                    JobService.listing(1, 10, status = JobStatus.INIT, after = now, before = now)
                        .second shouldBe expectCount[index]
                    it.cancel()
                })
                jobTracker.start()
                jobTracker.join()
            }
        }
    }

    /**
     * Init: 周五(10) + MONTH(1) + DAY(73) + HOUR(48*24) = 1236
     * Ready: 2010-08-06: 周五(10) + MONTH(1) + DAY(73) + HOUR(48*1) = 1236
     */
    @Test
    fun testMakeReadyForInitedJob() = supposeNowIs(2010, 8, 6, 0, 1) {
        val now = LocalDateTime.now()
        println(now)
        JobService.listing(1, 10, status = JobStatus.INIT, after = now, before = now).second shouldBe 0
        JobService.listing(1, 10, status = JobStatus.READY, after = now, before = now).second shouldBe 0
        val jobTracker = JobTracker(
            afterJobGenerated = {
                val jobs = JobService.listing(1, 10000, status = JobStatus.INIT, after = now, before = now).first
                jobs.size shouldBe 1236
                jobs.forEach { it.machineId shouldBe null }
            },
            afterMakeReady = { tracker ->
//                JobService.listing(1, 10, status = JobStatus.INIT, after = now, before = now)
//                    .second shouldBe 0
                val jobs = JobService.listing(1, 10000, status = JobStatus.READY, after = now, before = now).first
                jobs.size shouldBe 1236
                println(jobs.size)
                jobs.forEach { it.machineId shouldBe null }
                tracker.cancel()
            }
        )
        jobTracker.start()
        jobTracker.join()
    }

    @Test
    @Ignore
    fun testMakeRunningForReadyJob() {
        val jobTracker = JobTracker(afterMakeRunning = {
            it.cancel()
        })
        jobTracker.start()
        jobTracker.join()
    }

    @Test
    @Ignore
    fun testCheckInstanceOfRunningJob() {
        val jobTracker = JobTracker(afterInstanceCheck = {
            it.cancel()
        })
        jobTracker.start()
        jobTracker.join()
    }

    @Test
    @Ignore
    fun testRetryForFailedJob() {
        val jobTracker = JobTracker(afterRetry = {
            it.cancel()
        })
        jobTracker.start()
        jobTracker.join()
    }

}