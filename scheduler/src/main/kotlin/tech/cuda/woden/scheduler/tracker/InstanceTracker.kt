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

import tech.cuda.woden.common.service.dto.ContainerDTO
import tech.cuda.woden.common.service.exception.OperationNotAllowException
import tech.cuda.woden.common.service.*
import tech.cuda.woden.common.service.enum.TaskType
import tech.cuda.woden.common.service.po.dtype.FileType
import tech.cuda.woden.common.service.po.dtype.InstanceStatus
import tech.cuda.woden.common.service.po.dtype.JobStatus
import tech.cuda.woden.scheduler.runner.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 实例 Tracker，轮询当前有没有 ready 的实例，如果有，则启动实例，并跟踪实例状态
 * 其中 [afterStarted] 是启动后的回调，一般只用于单测
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class InstanceTracker(
    private val container: ContainerDTO,
    private val afterStarted: (InstanceTracker) -> Unit = {}
) : Tracker() {

    // 需要使用线程安全的容器，避免 forEach 的时候因 remove 而产生 ConcurrentModificationException
    private val runnerQueue = ConcurrentHashMap<Int, Runner>() // (InstanceId, (JobId, runner))

    // JobTracker 更新 Job 状态可能会存在延迟，因此在 InstanceTracker 维护一个执行中作业的 Map(InstanceId, JobId)
    private val runningJob = mutableMapOf<Int, Int>()

    private fun <T> T?.logIfNull(message: String): T? {
        if (this == null) {
            logger.error(message)
        }
        return this
    }

    private fun createInstanceForReadyJob() {
        batchExecute { batch, batchSize ->
            val (jobs, total) = JobService.listing(
                batch,
                batchSize,
                status = JobStatus.READY,
                containerId = container.id
            )
            jobs.filter { !runningJob.values.contains(it.id) }
                .forEach { job ->
                    val task = TaskService.findById(job.taskId)
                        .logIfNull("task ${job.id} defined in job ${job.id} does not exist")
                        ?: return@forEach
                    val content = GitService.readFile(task.filePath)
                    val runner = when (TaskService.getTaskTypeByFilePath(task.filePath)) {
                        TaskType.SPARK_SQL -> SparkSQLRunner(code = content)
                        TaskType.SPARK_SHELL -> SparkShellRunner(code = content)
                        TaskType.PY_SPARK -> PySparkRunner(code = content)
                        TaskType.MAP_REDUCE -> BashRunner(code = content)
                        TaskType.ANACONDA -> AnacondaRunner(code = content)
                        TaskType.BASH -> BashRunner(code = content)
                        else -> throw OperationNotAllowException()
                    }
                    runner.start()
                    val instance = InstanceService.create(job)
                    runnerQueue[instance.id] = runner
                    runningJob[instance.id] = job.id
                }
            jobs.size over total
        }
    }

    private fun checkStatusForRunningInstance() {
        runnerQueue.forEach { (instanceId, runner) ->
            val runnerStatus = runner.status

            if (runnerStatus.isFinish) {
                InstanceService.update(
                    id = instanceId,
                    status = when (runnerStatus) {
                        RunnerStatus.FAILED -> InstanceStatus.FAILED
                        RunnerStatus.KILLED -> InstanceStatus.KILLED
                        RunnerStatus.SUCCESS -> InstanceStatus.SUCCESS
                        else -> throw Exception("Wrong runner status $runnerStatus, expect isFinish = true") // 这个分支逻辑上不可达
                    },
                    log = runner.output
                )
                runnerQueue.remove(instanceId)
                runningJob.remove(instanceId)
            } else {
                InstanceService.update(id = instanceId, log = runner.output)
            }
        }
    }

    override fun onStarted() {
        onHeartBeat()
        afterStarted(this)
    }

    override fun onDestroyed() {
        runnerQueue.values.forEach { runner ->
            runner.join()
        }
        checkStatusForRunningInstance()
    }

    override fun onHeartBeat() {
        createInstanceForReadyJob()
        checkStatusForRunningInstance()
    }

}