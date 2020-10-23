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

import tech.cuda.woden.service.dto.JobDTO
import tech.cuda.woden.service.exception.NotFoundException
import tech.cuda.woden.service.InstanceService
import tech.cuda.woden.service.JobService
import tech.cuda.woden.service.MachineService
import tech.cuda.woden.service.TaskService
import tech.cuda.woden.service.po.dtype.InstanceStatus
import tech.cuda.woden.service.po.dtype.JobStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * 作业 Tracker，每当跨天的时候，生成当天应该调度的作业
 * 其中 [afterStarted] 是启动后的回调，一般只用于单测
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class JobTracker(private val afterStarted: () -> Unit = {}) : Tracker() {

    private val readyJobs = HashSet<JobDTO>()
    private val runningJobs = ConcurrentHashMap<Int, Int>() // (jobId, instanceId)

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

    /**
     * 检查 init 状态的作业其上游任务是否都执行成功或状态为 PASS
     * 如果是，则为其分配执行机器，并将状态置为 Ready
     */
    private fun makeReadyForInitedJob() {
        batchExecute { batch, batchSize ->
            val (jobs, total) = JobService.listing(batch, batchSize, status = JobStatus.READY)
            jobs.forEach { job ->
                if (JobService.isReady(job)) {
                    val worker = MachineService.findSlackMachine()
                    JobService.update(job.id, JobStatus.READY, machineId = worker.id)
                    readyJobs.add(job)
                }
            }
            jobs.size over total
        }
    }

    /**
     * 检查 ready 状态的作业是否生成了实例
     * 如果是，则将装状态置为 running
     */
    private fun makeRunningForReadyJob() {
        readyJobs.forEach { job ->
            val (instances, count) = InstanceService.listing(
                pageId = 1,
                pageSize = 1000,
                jobId = job.id,
                status = InstanceStatus.RUNNING
            )
            if (count > 1) throw Exception("job ${job.id} has more than 1 instance: ${instances.map { it.id }}") // todo: 当节点失效时可能会出现一个 job 多个节点执行的情况
            if (count == 1) {
                JobService.update(job.id, JobStatus.RUNNING)
                runningJobs[job.id] = instances.first().id
            }
        }
        readyJobs.removeIf { runningJobs.containsKey(it.id) }
    }

    /**
     * 检查 running 状态的作业的实例状态
     * 如果已经结束，则更新作业状态
     */
    private fun checkInstanceOfRunningJob() {
        runningJobs.forEach { (jobId, instanceId) ->
            val instance = InstanceService.findById(instanceId) ?: throw NotFoundException()
            if (instance.status != InstanceStatus.RUNNING) {
                JobService.update(jobId, when (instance.status) {
                    InstanceStatus.SUCCESS -> JobStatus.SUCCESS
                    InstanceStatus.FAILED -> JobStatus.FAILED
                    InstanceStatus.KILLED -> JobStatus.KILLED
                    else -> throw Exception() // 这里肯定不会到达，所以直接抛异常就好了
                })
                runningJobs.remove(jobId)
            }
        }
    }

    /**
     * 对于可重试的作业将状态设置为 ready
     */
    private fun retryForFailedJob() {
        batchExecute { batch, batchSize ->
            val (jobs, total) = JobService.listing(batch, batchSize, status = JobStatus.FAILED)
            jobs.forEach { job ->
                if (JobService.canRetry(job.id)) {
                    val worker = MachineService.findSlackMachine()
                    JobService.update(job.id, JobStatus.READY, machineId = worker.id)
                    readyJobs.add(job)
                }
            }
            jobs.size over total
        }
    }


    override fun onDateChange() = generateTodayJob()

    override fun onHeartBeat() {
        makeReadyForInitedJob()
        makeRunningForReadyJob()
        checkInstanceOfRunningJob()
        retryForFailedJob()
    }

}