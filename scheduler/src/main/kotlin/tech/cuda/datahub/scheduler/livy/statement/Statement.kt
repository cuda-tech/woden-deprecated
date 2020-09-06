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
package tech.cuda.datahub.scheduler.livy.statement

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import org.apache.log4j.Logger
import tech.cuda.datahub.config.Datahub
import tech.cuda.datahub.scheduler.exception.LivyException
import tech.cuda.datahub.scheduler.livy.MessageResponse
import tech.cuda.datahub.scheduler.livy.session.SessionKind
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.properties.Delegates

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class Statement(
    val id: Int,
    val code: String,
    @JsonProperty("state") private var _state: StatementState,
    @JsonProperty("progress") private var _progress: Double,
    @JsonProperty("started") private var _startTime: Long,
    @JsonProperty("completed") private var _finishTime: Long,
    @JsonProperty("output") private var _output: Map<String, Any>?
) {
    private val logger: Logger = Logger.getLogger(this.javaClass)

    var sessionId by Delegates.notNull<Int>()
    lateinit var sessionKind: SessionKind

    val startTime: LocalDateTime
        get() = LocalDateTime.ofInstant(Instant.ofEpochMilli(_startTime), ZoneId.systemDefault())

    val finishTime: LocalDateTime
        get() = LocalDateTime.ofInstant(Instant.ofEpochMilli(_finishTime), ZoneId.systemDefault())

    val output: StatementOutput
        get() {
            this.refresh()
            return when (sessionKind) {
                SessionKind.SQL -> StatementOutput("application/json", _output ?: mapOf())
                else -> StatementOutput("text/plain", _output ?: mapOf())
            }
        }

    val state: StatementState
        get() {
            this.refresh()
            return _state
        }

    val progress: Double
        get() {
            this.refresh()
            return _progress
        }

    private var lastRefreshTime = LocalDateTime.now()

    /**
     * 更新 statement 数据
     */
    private fun refresh() {
        val shouldRefresh = Duration.between(lastRefreshTime, LocalDateTime.now()).seconds > 1
        if (shouldRefresh) {
            val (response, error) = "${Datahub.livy.baseUrl}/sessions/$sessionId/statements/$id"
                .httpGet().responseObject<Statement>().third
            if (response != null) {
                this._state = response.state
                this._output = response._output
                this._progress = response.progress
                this._startTime = response._startTime
                this._finishTime = response._finishTime
            } else {
                logger.error("refresh statement $id in session $sessionId failed, ${error?.exception}")
            }
            lastRefreshTime = LocalDateTime.now()
        }
    }

    /**
     * 取消正在执行的 Statement
     * 由于 Livy 的 bug 还没有修复，暂时禁止取消任务
     */
    fun cancel() = if (true) {
        throw LivyException("因为 Livy 的 Bug，无法取消 Spark 相关的 Statement，详见 https://issues.apache.org/jira/browse/LIVY-786")
    } else {
        Fuel.post("${Datahub.livy.baseUrl}/sessions/$sessionId/statements/$id/cancel")
            .responseObject<MessageResponse>().third.component2() == null
    }


    /**
     * 等待 Statement 执行完毕，并返回状态
     */
    fun waitFinishedAndReturnState(): StatementState {
        val terminalState = listOf(
            StatementState.AVAILABLE,
            StatementState.ERROR,
            StatementState.CANCELLING,
            StatementState.CANCELLED
        )
        do {
            Thread.sleep(1000)
            refresh()
        } while (state !in terminalState)
        return state
    }

    override fun toString(): String {
        return mapOf(
            "id" to id,
            "session_id" to sessionId,
            "code" to "\n$code\n",
            "state" to state,
            "output" to output,
            "progress" to progress,
            "startTime" to startTime,
            "finishTime" to finishTime
        ).toString()
    }
}
