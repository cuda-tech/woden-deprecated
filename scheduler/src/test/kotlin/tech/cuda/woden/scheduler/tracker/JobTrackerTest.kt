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
import tech.cuda.woden.scheduler.TestWithDistribution
import tech.cuda.woden.service.JobService
import tech.cuda.woden.service.po.dtype.JobStatus
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class JobTrackerTest : TestWithDistribution("jobs", "tasks") {

    /**
     * 周调度：1:6, 2:9, 3:7, 4:5, 5:10, 6:6, 7:6
     * 天调度：73
     * 小时调度: 47
     *
     * 2010-08-06: 周五(10) + MONTH(1) + DAY(73) + HOUR(47*24) = 1212
     * 2010-08-07: 周六(6) + MONTH(2) + DAY(73) + HOUR(47*24) = 1209
     * 2010-08-08: 周日(6) + YEAR(1) + MONTH(1) + DAY(73) + HOUR(47*24) = 1209
     * 2010-08-09: 周一(6) + ONCE(1) + MONTH(1) + DAY(73) + HOUR(47*24) = 1209
     * 2010-08-10: 周二(9) + ONCE(1) + MONTH(4) + DAY(73) + HOUR(47*24) = 1215
     * 2010-08-11: 周三(7) + MONTH(2) + DAY(73) + HOUR(47*24) = 1210
     * 2010-08-12: 周四(5) + MONTH(1) + DAY(73) + HOUR(47*24) = 1207
     */
    @Test
    fun testGenerateJob() {
        val expectCount = listOf(1212, 1209, 1209, 1209, 1215, 1210, 1207)
        (6..12).forEachIndexed { index, day ->
            supposeNowIs(2010, 8, day, 0, 1) {
                val now = LocalDateTime.now()
                JobService.listing(1, 10, status = JobStatus.INIT, after = now, before = now).second shouldBe 0
                val jobTracker = JobTracker(afterStarted = {
                    JobService.listing(1, 10, status = JobStatus.INIT, after = now, before = now).second shouldBe expectCount[index]
                })
                jobTracker.start()
                jobTracker.cancelAndAwait()
            }
        }
    }

}