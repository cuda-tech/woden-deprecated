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
package tech.cuda.datahub.scheduler.livy.mocker.routers

import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import tech.cuda.datahub.scheduler.livy.mocker.LivyServer
import tech.cuda.datahub.scheduler.livy.session.Session
import tech.cuda.datahub.scheduler.livy.statement.Statement
import tech.cuda.datahub.scheduler.livy.statement.StatementState
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class CreateStatementRouter : Router() {
    override val pathPattern = Regex("/sessions/[0-9][0-9]*/statements")
    override val method = Method.POST

    override fun execute(request: Request): Response {
        val sessionId = request.url.path.replace("/sessions/", "")
            .replace("/statements", "").toInt()
        val session = LivyServer.sessionStore[sessionId]
        return if (session == null) {
            response(request.url, 404, mapOf("msg" to "Session '$sessionId' not found.").toJsonString())
        } else {
            val body = request.body.toMap()
            val statement = createStatement(session.first, body["code"] as String)
            response(request.url, 200, statement.toJsonString())
        }
    }

    private fun createStatement(session: Session, code: String): Map<String, Any?> {
        LivyServer.statementStore.putIfAbsent(session.id, mutableMapOf())
        val statementStore = LivyServer.statementStore[session.id]!!
        val nextId = statementStore.size
        val statement = Statement(
            id = nextId,
            code = code,
            state = StatementState.WAITING,
            progress = 0.0,
            _startTime = 0,
            _finishTime = 0,
            _output = null
        )
        statementStore[nextId] = statement to LocalDateTime.now()
        return mapOf(
            "id" to nextId,
            "code" to code,
            "state" to "waiting",
            "output" to null,
            "progress" to 0,
            "started" to 0,
            "completed" to 0
        )
    }
}