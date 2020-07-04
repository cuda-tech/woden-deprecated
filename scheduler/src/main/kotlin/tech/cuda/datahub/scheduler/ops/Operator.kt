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
package tech.cuda.datahub.scheduler.ops

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.apache.log4j.Logger
import tech.cuda.datahub.i18n.I18N
import tech.cuda.datahub.service.FileMirrorService
import tech.cuda.datahub.service.dto.TaskDTO
import tech.cuda.datahub.service.exception.NotFoundException

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
abstract class Operator(task: TaskDTO) {

    protected val logger: Logger = Logger.getLogger(this.javaClass)
    protected val mirror = FileMirrorService.findById(task.mirrorId)
        ?: throw NotFoundException(I18N.task, task.id, I18N.fileMirror, task.mirrorId, I18N.notExistsOrHasBeenRemove)
    protected lateinit var job: Deferred<Unit>

    abstract val isFinish: Boolean
    abstract val isSuccess: Boolean
    abstract val output: String

    /**
     * 异步地启动作业
     */
    abstract fun start()

    /**
     * 异步地 kill 作业
     */
    abstract fun kill()

    /**
     * 同步地等待作业执行完毕，一般只用于单测，所以这里会向上层抛出异常
     */
    fun join() = runBlocking {
        if (this@Operator::job.isInitialized) {
            job.await()
        }
    }

    /**
     * 将 Windows 文件路径转为 WSL 文件路径
     */
    protected fun String.toWSL() = this.replace("\\", "/").replace("C:", "/mnt/c")

}