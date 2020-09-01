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
import java.time.Duration
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class SessionStateRouter : Router() {
    override val pathPattern = Regex("/sessions/[0-9][0-9]*/state")
    override val method = Method.GET

    override fun execute(request: Request): Response {
        val sessionId = request.url.path.replace("/sessions/", "")
            .replace("/state", "").toInt()
        val session = LivyServer.sessionStore[sessionId]
        return if (session != null) {
            val secondPass = Duration.between(session.second, LocalDateTime.now()).seconds
            val state = if (secondPass > 1) "idle" else "starting" // todo: statement 判断
            response(request.url, 200, mapOf("id" to sessionId, "state" to state).toJsonString())
        } else {
            response(request.url, 404, mapOf("msg" to "Session '$sessionId' not found.").toJsonString())
        }
    }


}