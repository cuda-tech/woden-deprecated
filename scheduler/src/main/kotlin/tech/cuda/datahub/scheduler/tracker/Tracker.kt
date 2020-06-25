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

import kotlinx.coroutines.*
import org.apache.log4j.Logger
import tech.cuda.datahub.scheduler.listener.ClockListener
import java.lang.AssertionError
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tracker 抽象类
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
abstract class Tracker : ClockListener {
    protected val logger: Logger = Logger.getLogger(this::class.java)
    private val className: String get() = this.javaClass.simpleName
    private val heartbeat = 500L
    private var started: Boolean = false
    private lateinit var job: Deferred<Unit>

    private var datetimeSnapshot = LocalDateTime.of(1970, 1, 1, 0, 0, 0)
    private fun dateChange() = LocalDate.now().isAfter(datetimeSnapshot.toLocalDate())
    private fun hourChange() = LocalDate.now().isAfter(datetimeSnapshot.toLocalDate())


    fun start() {
        if (this::job.isInitialized) {
            logger.error("try to start $className duplicate")
            return
        }
        this.job = GlobalScope.async {
            while (true) {
                try {
                    if (dateChange()) {
                        onDateChange()
                        logger.info("day change")
                    }
                    if (hourChange()) {
                        onHourChange()
                        logger.info("hour change")
                    }
                    onHeartBeat()
                    delay(heartbeat)
                    datetimeSnapshot = LocalDateTime.now()
                    logger.debug("$className alive")
                } catch (exception: Throwable) {
                    when (exception) {
                        // 不捕获由协程取消引起的 CancellationException 和单测引起的 AssertionError
                        is CancellationException, is AssertionError -> throw exception
                        else -> {
                            logger.error(exception.message)
                            exception.printStackTrace()
                        }
                    }
                }
            }
        }
        logger.info("$className started").also { this.started = true }
    }

    suspend fun await() {
        if (this::job.isInitialized) {
            this.job.await()
        } else {
            logger.error("try to join a not started $className")
        }
    }

    suspend fun cancelAndAwait() {
        if (this::job.isInitialized) {
            try {
                this.job.cancel()
                this.job.await()
            } catch (e: CancellationException) {
                logger.error("cancel $className")
            }
        } else {
            logger.error("try to cancel a not started $className")
        }
    }

}