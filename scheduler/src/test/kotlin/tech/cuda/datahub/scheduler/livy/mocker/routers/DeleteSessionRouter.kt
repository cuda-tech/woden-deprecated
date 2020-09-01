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

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class DeleteSessionRouter : Router() {
    override val pathPattern = Regex("/sessions/[0-9][0-9]*")
    override val method = Method.DELETE

    override fun execute(request: Request): Response {
        val sessionId = request.url.path.replace("/sessions/", "").toInt()
        val session = LivyServer.sessionStore[sessionId]
        return if (session != null) {
            LivyServer.sessionStore.remove(sessionId)
            response(request.url, 200, mapOf("msg" to "deleted").toJsonString())
        } else {
            response(request.url, 404, mapOf("msg" to "Session '$sessionId' not found.").toJsonString())
        }
    }

}