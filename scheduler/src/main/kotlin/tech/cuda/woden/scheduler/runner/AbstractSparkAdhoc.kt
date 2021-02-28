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
package tech.cuda.woden.scheduler.runner

import com.google.common.base.Charsets
import com.google.common.io.Resources
import org.apache.spark.launcher.SparkAppHandle
import org.apache.spark.launcher.SparkLauncher
import tech.cuda.woden.common.configuration.Woden
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
abstract class AbstractSparkAdhoc : Adhoc {

    abstract val mainClass: String
    open val jar: String = "${Woden.scheduler.sparkHome}${File.separator}jars${File.separator}spark-sql*.jar"
    open val appArgs: List<String> = listOf()
    open val sparkConf: Map<String, String> = mapOf()

    private val logFile = File.createTempFile("__adhoc__", ".log")
    private lateinit var handler: SparkAppHandle
    override val output: String
        get() {
            return Resources.readLines(logFile.toURI().toURL(), Charsets.UTF_8).joinToString("\n")
        }

    override val status: AdhocStatus
        get() = if (!this::handler.isInitialized) {
            AdhocStatus.NOT_START
        } else {
            when (handler.state!!) {
                SparkAppHandle.State.UNKNOWN -> AdhocStatus.NOT_START
                SparkAppHandle.State.CONNECTED -> AdhocStatus.NOT_START
                SparkAppHandle.State.SUBMITTED -> AdhocStatus.NOT_START
                SparkAppHandle.State.RUNNING -> AdhocStatus.RUNNING
                SparkAppHandle.State.FINISHED -> {
                    // FINISHED 仅代表 Spark Context 正确地启动 & 停止，并不代表作业成功
                    // 因此需要判断一下子线程的返回值，由于子线程是 private 的，因此需要反射设置 accessible 后读取
                    val proc = handler::class.java.getDeclaredField("childProc")
                        .also { it.isAccessible = true }
                        .get(handler) as Process
                    val exit = proc.waitFor(60, TimeUnit.SECONDS)
                    when {
                        !exit -> throw Exception()
                        proc.exitValue() == 0 -> AdhocStatus.SUCCESS
                        else -> AdhocStatus.FAILED
                    }
                }
                SparkAppHandle.State.FAILED -> AdhocStatus.FAILED
                SparkAppHandle.State.KILLED -> AdhocStatus.KILLED
                SparkAppHandle.State.LOST -> AdhocStatus.FAILED
            }
        }


    override fun start() {
        if (!this::handler.isInitialized) {
            beforeStart()
            handler = SparkLauncher()
                .redirectOutput(logFile)
                .redirectError(logFile)
                .setSparkHome(Woden.scheduler.sparkHome)
                .setMainClass(mainClass)
                .setAppResource(jar)
                .also {
                    appArgs.forEach { arg -> it.addAppArgs(arg) }
                    sparkConf.forEach { (k, v) -> it.setConf(k, v) }
                }
                .startApplication()
            afterStart()
        }
    }

    override fun close() {
        beforeClose()
        if (logFile.exists()) {
            logFile.delete()
        }
        afterClose()
    }

    override fun join() {
        beforeJoin()
        if (!this::handler.isInitialized) {
            return
        }
        while (!handler.state.isFinal) {
            Thread.sleep(1000)
        }
        afterJoin()
    }

    override fun kill() {
        beforeKill()
        if (this::handler.isInitialized && this.handler.state == SparkAppHandle.State.RUNNING) {
            handler.stop()
        }
        afterKill()
    }
}