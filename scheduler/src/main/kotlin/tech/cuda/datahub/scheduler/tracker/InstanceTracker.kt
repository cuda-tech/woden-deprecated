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

import tech.cuda.datahub.scheduler.ops.*
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
 * [devMode] 标识当前是否处于开发环境
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class InstanceTracker(
    private val machine: MachineDTO,
    private val afterStarted: () -> Unit = {},
    private val devMode: Boolean = false
) : Tracker() {

    // 需要使用线程安全的容器，避免 forEach 的时候因 remove 而产生 ConcurrentModificationException
    private val operatorQueue = ConcurrentHashMap<Int, Operator>()

    private fun createInstanceForReadyJob() {
        batchExecute { batch, batchSize ->
            val (jobs, total) = JobService.listing(batch, batchSize, status = JobStatus.READY, machineId = machine.id)
            for (job in jobs) {
                val task = TaskService.findById(job.taskId) ?: continue
                val mirror = FileMirrorService.findById(task.mirrorId) ?: continue
                val file = FileService.findById(mirror.fileId) ?: continue
                val op = when (file.type) {
                    FileType.SQL -> SparkSqlOperator(task)
                    FileType.PYTHON -> if (devMode) PythonOperator(task, type = "python3") else PythonOperator(task)
                    FileType.BASH -> BashOperator(task)
                    else -> throw OperationNotAllowException()
                }
                op.start()
                val instance = InstanceService.create(job)
                operatorQueue[instance.id] = op
            }
            jobs.size over total
        }
    }

    private fun checkStatusForRunningInstance() {
        operatorQueue.forEach { (id, operator) ->
            if (operator.isFinish) {
                InstanceService.update(
                    id = id,
                    status = if (operator.isSuccess) InstanceStatus.SUCCESS else InstanceStatus.FAILED,
                    log = operator.output
                )
                operatorQueue.remove(id)
            } else {
                InstanceService.update(id, log = operator.output)
            }
        }
    }

    override fun onStarted() {
        createInstanceForReadyJob()
        checkStatusForRunningInstance()
        afterStarted()
    }

    override fun onDestroyed() {}

    override fun onDateChange() {}

    override fun onHourChange() {}

    override fun onHeartBeat() {
        createInstanceForReadyJob()
        checkStatusForRunningInstance()
    }

}