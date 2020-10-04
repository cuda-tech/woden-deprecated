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

import com.google.common.base.Charsets
import com.google.common.io.Resources
import org.apache.spark.launcher.SparkAppHandle
import org.apache.spark.launcher.SparkLauncher
import tech.cuda.datahub.config.Datahub
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
abstract class AbstractSparkJob : Job {

    abstract val mainClass: String
    open val jar: String = "${Datahub.scheduler.sparkHome}${File.separator}jars${File.separator}spark-sql*.jar"
    open val appArgs: Map<String, String> = mapOf()
    open val sparkConf: Map<String, String> = mapOf()

    private val logFile = File.createTempFile("__adhoc__", ".log")
    private lateinit var handler: SparkAppHandle
    override val output: String
        get() {
            return Resources.readLines(logFile.toURI().toURL(), Charsets.UTF_8).joinToString("\n")
        }

    override val status: JobStatus
        get() = if (!this::handler.isInitialized) {
            JobStatus.NOT_START
        } else {
            when (handler.state) {
                SparkAppHandle.State.CONNECTED -> JobStatus.RUNNING
                SparkAppHandle.State.SUBMITTED -> JobStatus.RUNNING
                SparkAppHandle.State.RUNNING -> JobStatus.RUNNING
                SparkAppHandle.State.FINISHED -> {
                    // FINISHED 仅代表 Spark Context 正确地启动 & 停止，并不代表作业成功
                    // 因此需要判断一下子线程的返回值，由于子线程是 private 的，因此需要反射设置 accessible 后读取
                    val proc = handler::class.java.getDeclaredField("childProc")
                        .also { it.isAccessible = true }
                        .get(handler) as Process
                    val exit = proc.waitFor(60, TimeUnit.SECONDS)
                    when {
                        !exit -> JobStatus.UNKNOWN
                        proc.exitValue() == 0 -> JobStatus.SUCCESS
                        else -> JobStatus.FAILED
                    }
                }
                SparkAppHandle.State.FAILED -> JobStatus.FAILED
                SparkAppHandle.State.KILLED -> JobStatus.KILLED
                SparkAppHandle.State.LOST -> JobStatus.FAILED
                else -> JobStatus.UNKNOWN
            }
        }


    override fun start() {
        if (!this::handler.isInitialized) {
            beforeStart()
            handler = SparkLauncher()
                .redirectOutput(logFile)
                .redirectError(logFile)
                .setMainClass(mainClass)
                .setAppResource(jar)
                .also {
                    appArgs.forEach { (k, v) -> it.addAppArgs(k, v) }
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