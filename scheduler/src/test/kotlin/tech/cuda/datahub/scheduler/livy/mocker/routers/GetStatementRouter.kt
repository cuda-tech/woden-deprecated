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

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import tech.cuda.datahub.scheduler.exception.LivyException
import tech.cuda.datahub.scheduler.livy.mocker.LivyServer
import tech.cuda.datahub.scheduler.livy.mocker.StatementExample
import tech.cuda.datahub.scheduler.livy.session.SessionKind
import java.time.Duration
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class GetStatementRouter : Router() {
    override val pathPattern = Regex("/sessions/[0-9][0-9]*/statements/[0-9][0-9]*")
    override val method = Method.GET

    override fun execute(request: Request): Response {
        val sessionId = request.url.path.replace("/sessions/", "")
            .substringBefore("/").toInt()
        val statementId = request.url.path.substringAfterLast("/").toInt()
        val session = LivyServer.sessionStore[sessionId]
            ?: return response(request.url, 404, mapOf("msg" to "Session '$sessionId' not found.").toJsonString())
        val (statement, createTime) = LivyServer.statementStore[sessionId]?.get(statementId)
            ?: return response(request.url, 404, mapOf("msg" to "Statement not found").toJsonString())
        val example = StatementExample.findByCode(statement.code) ?: throw LivyException("statement example not found")
        val finish = Duration.between(createTime, LocalDateTime.now()).seconds > 1
        val ret = when {
            !finish -> mapOf(
                "id" to statement.id,
                "code" to statement.code,
                "state" to "running",
                "output" to null,
                "progress" to 0,
                "started" to 1599145031092,
                "completed" to 0
            )
            finish && example.errorName == null -> mapOf(
                "id" to statement.id,
                "code" to statement.code,
                "state" to "available",
                "output" to mapOf(
                    "status" to "ok",
                    "execution_count" to statement.id,
                    "data" to mapOf(
                        (if (session.first.kind == SessionKind.SQL) "application/json" else "text/plain")
                            to if (session.first.kind == SessionKind.SQL) mapper.readValue<Map<String, Any?>>(example.stdout!!) else example.stdout
                    )
                ),
                "progress" to 1,
                "started" to 1599145031092,
                "completed" to 1599145031092
            )
            finish && example.errorName != null -> mapOf(
                "id" to statement.id,
                "code" to statement.code,
                "state" to "available",
                "output" to mapOf(
                    "status" to "error",
                    "execution_count" to statement.id,
                    "ename" to example.errorName,
                    "evalue" to example.stderr,
                    "traceback" to listOf(
                        "mock\n",
                        "traceback"
                    )
                ),
                "progress" to 1,
                "started" to 1599145031092,
                "completed" to 1599145031092
            )
            else -> throw LivyException()
        }.toJsonString()

        return response(request.url, 200, ret)
    }

}