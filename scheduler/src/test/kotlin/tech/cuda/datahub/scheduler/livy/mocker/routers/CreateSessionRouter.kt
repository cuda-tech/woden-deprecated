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
import tech.cuda.datahub.scheduler.exception.LivyException
import tech.cuda.datahub.scheduler.livy.mocker.LivyServer
import tech.cuda.datahub.scheduler.livy.session.Session
import tech.cuda.datahub.scheduler.livy.session.SessionKind
import tech.cuda.datahub.scheduler.livy.session.SessionState
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class CreateSessionRouter : Router() {
    override val pathPattern = Regex("/sessions")
    override val method = Method.POST

    override fun execute(request: Request): Response {
        val body = request.body.toMap()
        val (success, session) = createSession(body["kind"] as String, body["name"] as String)
        return if (success) {
            response(request.url, 200, session)
        } else {
            response(request.url, 400, session, msg = "Bad Request")
        }
    }

    private fun createSession(kind: String, name: String): Pair<Boolean, String> {
        LivyServer.nextId++
        val exists = LivyServer.sessionStore.filter { it.component2().first.name == name }.count() > 0
        if (exists) {
            return false to mapOf("msg" to "Duplicate session name: Some($name) for session ${LivyServer.nextId}").toJsonString()
        }
        val session = Session(
            id = LivyServer.nextId,
            name = name,
            appId = null,
            owner = null,
            proxyUser = null,
            _state = SessionState.STARTING,
            kind = when (kind) {
                "sql" -> SessionKind.SQL
                "pyspark" -> SessionKind.PY_SPARK
                "spark" -> SessionKind.SPARK
                "sparkr" -> SessionKind.SPARK_R
                else -> throw LivyException("unknown session kind")
            },
            appInfo = mapOf("driverLogUrl" to null, "driverLogUrl" to null),
            _log = listOf("stdout: ", "\\nstderr: ")
        )
        LivyServer.sessionStore[LivyServer.nextId] = session to LocalDateTime.now()
        return true to session.toJsonString()
    }
}