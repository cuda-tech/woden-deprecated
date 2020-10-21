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

import tech.cuda.datahub.adhoc.*
import tech.cuda.datahub.service.*
import tech.cuda.datahub.service.dto.MachineDTO
import tech.cuda.datahub.service.exception.OperationNotAllowException
import tech.cuda.datahub.service.po.dtype.FileType
import tech.cuda.datahub.service.po.dtype.InstanceStatus
import tech.cuda.datahub.service.po.dtype.JobStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * 实例 Tracker，轮询当前有没有 ready 的实例，如果有，则启动实例，并跟踪实例状态
 * 其中 [afterStarted] 是启动后的回调，一般只用于单测
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class InstanceTracker(
    private val machine: MachineDTO,
    private val afterStarted: () -> Unit = {}
) : Tracker() {

    // 需要使用线程安全的容器，避免 forEach 的时候因 remove 而产生 ConcurrentModificationException
    private val adhocQueue = ConcurrentHashMap<Int, Adhoc>() // (InstanceId, (JobId, Adhoc))

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
            val (jobs, total) = JobService.listing(batch, batchSize, status = JobStatus.READY, machineId = machine.id)
            jobs.filter { !runningJob.values.contains(it.id) }
                .forEach { job ->
                    val task = TaskService.findById(job.taskId)
                        .logIfNull("task ${job.id} defined in job ${job.id} does not exist")
                        ?: return@forEach

                    val mirror = FileMirrorService.findById(task.mirrorId)
                        .logIfNull("mirror ${task.mirrorId} defined in task ${task.id} and job ${job.id} does not exists")
                        ?: return@forEach

                    val file = FileService.findById(mirror.fileId)
                        .logIfNull("file ${mirror.fileId} defined in mirror ${mirror.id}, task ${task.id} and job ${job.id} does not exists")
                        ?: return@forEach

                    val adhoc = when (file.type) {
                        FileType.SPARK_SQL -> SparkSQLAdhoc(code = mirror.content)
                        FileType.ANACONDA -> AnacondaAdhoc(code = mirror.content)
                        FileType.PY_SPARK -> PySparkAdhoc(code = mirror.content)
                        FileType.BASH -> BashAdhoc(code = mirror.content)
                        else -> throw OperationNotAllowException()
                    }
                    adhoc.start()
                    val instance = InstanceService.create(job)
                    adhocQueue[instance.id] = adhoc
                    runningJob[instance.id] = job.id
                }
            jobs.size over total
        }
    }

    private fun checkStatusForRunningInstance() {
        adhocQueue.forEach { (instanceId, adhoc) ->
            val adhocStatus = adhoc.status

            if (adhocStatus.isFinish) {
                InstanceService.update(
                    id = instanceId,
                    status = when (adhocStatus) {
                        AdhocStatus.FAILED -> InstanceStatus.FAILED
                        AdhocStatus.KILLED -> InstanceStatus.KILLED
                        AdhocStatus.SUCCESS -> InstanceStatus.SUCCESS
                        else -> throw Exception("Wrong adhoc status $adhocStatus, expect isFinish = true") // 这个分支逻辑上不可达
                    },
                    log = adhoc.output
                )
                adhocQueue.remove(instanceId)
                runningJob.remove(instanceId)
            } else {
                InstanceService.update(id = instanceId, log = adhoc.output)
            }
        }
    }

    override fun onStarted() {
        onHeartBeat()
        afterStarted()
    }

    override fun onDestroyed() {
        adhocQueue.values.forEach { adhoc ->
            adhoc.join()
        }
        checkStatusForRunningInstance()
    }

    override fun onHeartBeat() {
        createInstanceForReadyJob()
        checkStatusForRunningInstance()
    }

}