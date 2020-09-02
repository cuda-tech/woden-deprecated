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
package tech.cuda.datahub.scheduler.livy.session

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.fuel.jackson.responseObject
import org.apache.log4j.Logger
import tech.cuda.datahub.config.Datahub
import tech.cuda.datahub.scheduler.livy.SessionLogResponse
import tech.cuda.datahub.scheduler.livy.SessionStateResponse
import tech.cuda.datahub.scheduler.livy.statement.Statement
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
data class Session(
    val id: Int,
    val name: String,
    val appId: String?,
    val owner: String?,
    val proxyUser: String?,
    val kind: SessionKind,
    val appInfo: Map<String, Any?>,
    @JsonProperty("state") private val _state: SessionState,
    @JsonProperty("log") private val _log: List<String>
) {
    private val logger: Logger = Logger.getLogger(this.javaClass)

    /**
     * 获取 Session 的当前状态
     */
    val state: SessionState
        get() {
            val (response, _) = "${Datahub.livy.baseUrl}/sessions/$id/state"
                .httpGet().responseObject<SessionStateResponse>().third
            return response?.state ?: SessionState.UNKNOWN
        }

    /**
     * 获取所有的 log 输出
     */
    val log: List<String>
        get() {
            val (response, _) = "${Datahub.livy.baseUrl}/sessions/$id/log"
                .httpGet(listOf("from" to 0, "size" to Int.MAX_VALUE))
                .responseObject<SessionLogResponse>().third
            return response?.log ?: listOf()
        }

    /**
     * 等待 Session 进入 IDLE 状态
     * 如果成功进入，则返回 true
     * 如果因为超时或异常导致无法进入 IDLE，则返回 false
     */
    fun waitIDLE(): Boolean {
        val timeout = 200L
        val startTime = LocalDateTime.now()
        do {
            val currentState = state
            if (currentState == SessionState.IDLE) {
                return true
            }
            if (currentState != SessionState.NOT_STARTED && currentState != SessionState.STARTING) {
                logger.error("waiting IDLE failed, current state $currentState")
                return false
            }
            if (LocalDateTime.now().minusSeconds(timeout).isAfter(startTime)) {
                logger.error("waiting IDLE timeout")
                return false
            }
            Thread.sleep(1000)
        } while (currentState != SessionState.IDLE)
        return false
    }

    /**
     * 删除 session，并返回是否删除成功
     */
    fun kill() = "${Datahub.livy.baseUrl}/sessions/$id".httpDelete().response().second.statusCode == 200

    /**
     * 获取 Session 下的所有 statements
     * 如果出现异常，则返回空数组
     */
    fun listingStatements(): List<Statement> {
        val (response, _) = "${Datahub.livy.baseUrl}/sessions/$id/statements"
            .httpGet().responseObject<List<Statement>>().third
        return response ?: listOf()
    }

    /**
     * 获取指定[statementId]的 statement
     * 如果出现异常，则返回 null
     */
    fun getStatement(statementId: Int): Statement? {
        val (response, _) = "${Datahub.livy.baseUrl}/sessions/$id/statements/$statementId"
            .httpGet().responseObject<Statement>().third
        return response
    }

    /**
     * 创建一个执行[code]的 Statement
     */
    fun createStatement(code: String): Statement? {
        val (statement, _) = Fuel.post("${Datahub.livy.baseUrl}/sessions/$id/statements")
            .header("Content-Type", "application/json")
            .objectBody(mapOf("code" to code))
            .responseObject<Statement>().third
        return statement?.also {
            it.sessionId = this.id
            it.sessionKind = this.kind
        }
    }

    override fun toString(): String {
        return mapOf(
            "id" to id,
            "name" to name,
            "appId" to appId,
            "owner" to owner,
            "proxyUser" to proxyUser,
            "state" to state,
            "kind" to kind,
            "log" to log,
            "appInfo" to appInfo
        ).toString()
    }

}
