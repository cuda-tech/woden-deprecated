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
package tech.cuda.datahub.adhoc

import org.apache.commons.exec.*
import org.apache.commons.exec.environment.EnvironmentUtils
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
abstract class AbstractBashAdhoc(
    private val executorPath: String,
    private val code: String,
    private val arguments: List<String> = listOf(),
    private val kvArguments: Map<String, String> = mapOf()
) : Adhoc {
    private val expectedExitValue = 0
    private val resultHandler = DefaultExecuteResultHandler()
    private val logStream = ByteArrayOutputStream()
    private var started = false
    private var killed = false

    private val executor = DefaultExecutor().also {
        it.setExitValue(expectedExitValue)
        it.streamHandler = PumpStreamHandler(logStream, logStream)
        it.processDestroyer = ShutdownHookProcessDestroyer()
    }

    override val output: String
        get() = if (resultHandler.hasResult() && resultHandler.exception != null) {
            logStream.toString() + "\n" + resultHandler.exception.toString()
        } else {
            logStream.toString()
        }

    override val status: AdhocStatus
        get() {
            return when {
                !started -> AdhocStatus.NOT_START
                !this.resultHandler.hasResult() -> AdhocStatus.RUNNING
                this.resultHandler.exitValue == expectedExitValue -> AdhocStatus.SUCCESS
                this.resultHandler.exitValue != expectedExitValue -> if (killed) AdhocStatus.KILLED else AdhocStatus.FAILED
                else -> AdhocStatus.UNKNOWN
            }
        }

    override fun start() {
        if (started) {
            return
        }
        beforeStart()
        val command = CommandLine(executorPath).also {
            val tempFile = File.createTempFile("__adhoc__", ".temp").also { file ->
                val writer = BufferedWriter(FileWriter(file))
                writer.write(code)
                writer.flush()
                writer.close()
                file.deleteOnExit()
            }
            it.addArgument(tempFile.absolutePath)
            arguments.forEach { arg -> it.addArgument(arg) }
            kvArguments.forEach { (name, value) ->
                it.addArgument(name)
                it.addArgument(value)
            }
        }
        executor.execute(command, EnvironmentUtils.getProcEnvironment(), resultHandler)
        started = true
        afterStart()
    }

    override fun kill() {
        if (killed) {
            return
        }
        beforeKill()
        (this.executor.processDestroyer as ShutdownHookProcessDestroyer).run()
        killed = true
        afterKill()
    }

    override fun close() {
        beforeClose()
        afterClose()
    }

    override fun join() {
        beforeJoin()
        resultHandler.waitFor()
        afterJoin()
    }

}