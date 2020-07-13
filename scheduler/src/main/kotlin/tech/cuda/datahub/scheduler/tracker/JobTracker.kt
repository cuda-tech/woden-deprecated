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

import tech.cuda.datahub.service.JobService
import tech.cuda.datahub.service.TaskService

/**
 * 作业 Tracker，每当跨天的时候，生成当天应该调度的作业
 * 其中 [afterStarted] 是启动后的回调，一般只用于单测
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class JobTracker(private val afterStarted: () -> Unit = {}) : Tracker() {

    /**
     * 对当前生效的任务，分批地生成调度作业
     * 如果当天不应该调度，或作业已经生成，则不会生成作业（这个逻辑由 JobService 控制）
     */
    private fun generateTodayJob() {
        logger.info("generate today's jobs")
        batchExecute { batch, batchSize ->
            val (tasks, total) = TaskService.listing(batch, batchSize, isValid = true)
            tasks.forEach { JobService.create(it) }
            tasks.size over total
        }
        logger.info("generate today's jobs done")
    }

    override fun onStarted() {
        generateTodayJob()
        afterStarted()
    }

    override fun onDestroyed() {}

    override fun onDateChange() = generateTodayJob()

    override fun onHourChange() {}

    override fun onHeartBeat() {}


}