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
class SessionLogRouter : Router() {
    override val pathPattern = Regex("/sessions/[0-9][0-9]*/log")
    override val method = Method.GET

    override fun execute(request: Request): Response {
        val sessionId = request.url.path.replace("/sessions/", "")
            .replace("/log", "").toInt()
        val session = LivyServer.sessionStore[sessionId]
        return if (session != null) {
            val secondPass = Duration.between(session.second, LocalDateTime.now()).seconds
            val state = if (secondPass > 1) "idle" else "starting" // todo: statement 判断
            response(request.url, 200, mapOf(
                "id" to sessionId,
                "from" to 32,
                "total" to 132,
                "log" to listOf(
                    "2020-09-01 22:33:05 INFO  ContextHandler:781 - Started o.s.j.s.ServletContextHandler@5604df93{/jobs,null,AVAILABLE,@Spark}",
                    "2020-09-01 22:33:05 INFO  ContextHandler:781 - Started o.s.j.s.ServletContextHandler@7b0a030b{/jobs/json,null,AVAILABLE,@Spark}",
                    "2020-09-01 22:33:05 INFO  ContextHandler:781 - Started o.s.j.s.ServletContextHandler@7745e9b6{/jobs/job,null,AVAILABLE,@Spark}",
                    "2020-09-01 22:33:05 INFO  ContextHandler:781 - Started o.s.j.s.ServletContextHandler@1bed3dc4{/jobs/job/json,null,AVAILABLE,@Spark}",
                    "2020-09-01 22:33:05 INFO  ContextHandler:781 - Started o.s.j.s.ServletContextHandler@30d467e9{/stages,null,AVAILABLE,@Spark}"
                )
            ).toJsonString())
        } else {
            response(request.url, 404, mapOf("msg" to "Session '$sessionId' not found.").toJsonString())
        }
    }


}