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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.exec.*
import org.apache.commons.exec.environment.EnvironmentUtils
import tech.cuda.datahub.scheduler.util.MachineUtil
import tech.cuda.datahub.service.dto.TaskDTO
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter

/**
 * 基于 Bash 的任务，只支持 Linux 环境（如果是 windows，则执行在 WSL 下，你总不会把 WSL 作为生产环境吧...）
 * 执行逻辑：先将要执行的命令写入到临时文件，然后再执行这个临时文件
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@Suppress("BlockingMethodInNonBlockingContext")
abstract class BashBaseOperator(taskDTO: TaskDTO, private val type: String) : Operator(taskDTO) {
    private val exitValue = 0
    private val std = ByteArrayOutputStream()
    private val resultHandler = DefaultExecuteResultHandler()
    private val killer = ShutdownHookProcessDestroyer()
    private val executor = DefaultExecutor().also {
        it.setExitValue(exitValue)
        it.streamHandler = PumpStreamHandler(std, std)
        it.processDestroyer = killer
    }
    protected open val commands: String get() = mirror?.content ?: ""

    override val output: String
        get() = this.std.toString()

    override val isFinish: Boolean
        get() = this.resultHandler.hasResult()

    override val isSuccess: Boolean
        get() = try {
            this.isFinish && this.resultHandler.exitValue == exitValue
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }

    override fun start() {
        // 先将命令写入到临时文件
        val tempFile = File.createTempFile(type + '_', ".temp").also {
            val writer = BufferedWriter(FileWriter(it))
            writer.write(commands)
            writer.flush()
            writer.close()
        }

        this.job = GlobalScope.async {
            try {
                // 根据不同的操作系统生成 commandLine
                val commandLine = if (MachineUtil.systemInfo.isWindows) {
                    CommandLine("wsl").also {
                        it.addArguments(type)
                        it.addArgument(tempFile.absolutePath.toWSL())
                    }
                } else {
                    CommandLine(type).also { it.addArgument(tempFile.absolutePath) }
                }

                // 然后执行它并等待执行完毕
                executor.execute(commandLine, EnvironmentUtils.getProcEnvironment(), resultHandler)
                resultHandler.waitFor()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                tempFile.delete()
            }
        }
    }

    /**
     * kill 之前先等 1 秒避免刚 start 就 kill，此时 process 还未启动，killer 没有接收 process，无法 kill 掉 process
     * 我知道用 sleep 来控制很迷，但是 apache exec 没有暴露出 process 是否已启动的接口我也很绝望啊...
     * 所以这个 kill 方法并不保证一定能 kill 成功，你需要通过 isFinish = true && isSuccess = false 来判断是否已经 kill 成功
     */
    override fun kill() {
        GlobalScope.launch {
            delay(1000)
            killer.run()
        }
    }
}