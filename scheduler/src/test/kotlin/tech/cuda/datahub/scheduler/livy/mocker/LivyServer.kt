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
package tech.cuda.datahub.scheduler.livy.mocker

import com.github.kittinunf.fuel.core.*
import tech.cuda.datahub.scheduler.livy.mocker.routers.*
import tech.cuda.datahub.scheduler.livy.session.Session
import java.time.LocalDateTime

/**
 * 用于单测的 Livy Service，当本地环境不存在 Livy 时则用这个来进行单测
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object LivyServer {
    var nextId = -1
    var sessionStore = mutableMapOf<Int, Pair<Session, LocalDateTime>>()
    private val defaultClient = FuelManager.instance.client
    private val routers: Router = CancelStatementRouter().also {
        it.setNext(CreateSessionRouter())
            .setNext(CreateStatementRouter())
            .setNext(DeleteSessionRouter())
            .setNext(GetSessionRouter())
            .setNext(GetStatementRouter())
            .setNext(ListingSessionRouter())
            .setNext(ListingStatementRouter())
            .setNext(SessionLogRouter())
            .setNext(SessionStateRouter())
    }

    fun start() {
        val client = object : Client {
            override fun executeRequest(request: Request): Response = routers.executeRequest(request)
        }
        FuelManager.instance.client = client
        sessionStore = mutableMapOf()
        nextId = -1
    }

    fun stop() {
        FuelManager.instance.client = defaultClient
        sessionStore = mutableMapOf()
        nextId = -1
    }
}