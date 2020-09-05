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
import io.kotest.matchers.shouldBe
import tech.cuda.datahub.scheduler.livy.mocker.routers.*
import tech.cuda.datahub.scheduler.livy.session.Session
import tech.cuda.datahub.scheduler.livy.statement.Statement
import tech.cuda.datahub.scheduler.util.MachineUtil
import java.time.LocalDateTime

/**
 * 用于单测的 Livy Service，当本地环境不存在 Livy 时则用这个来进行单测
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object LivyServer {
    private val runtime = Runtime.getRuntime()
    private val mockLivyServer = (System.getenv()["MOCK_LIVY"]
        ?: System.getProperty("MOCK_LIVY")
        ?: "true").toBoolean()
    private val livyServerExists = if (MachineUtil.systemInfo.isWindows) {
        runtime.exec("wsl bash -c \"source /etc/profile && livy-server status\"").waitFor() == 0
    } else {
        runtime.exec("bash -c \"source /etc/profile && livy-server status\"").waitFor() == 0
    }

    var nextId = -1
    var sessionStore: MutableMap<Int, Pair<Session, LocalDateTime>> = mutableMapOf()
    var statementStore: MutableMap<Int, MutableMap<Int, Pair<Statement, LocalDateTime>>> = mutableMapOf()
    private val defaultClient = FuelManager.instance.client
    private val routers: Router = CancelStatementRouter().also {
        it.setNext(CreateSessionRouter())
            .setNext(CreateStatementRouter())
            .setNext(DeleteSessionRouter())
            .setNext(GetSessionRouter())
            .setNext(GetStatementRouter())
            .setNext(ListingSessionRouter())
            .setNext(SessionLogRouter())
            .setNext(SessionStateRouter())
    }

    fun start() = if (livyServerExists && !mockLivyServer) {
        startRealServer()
    } else {
        startMockServer()
    }

    private fun startRealServer() {
        if (MachineUtil.systemInfo.isWindows) {
            runtime.exec("wsl bash -c \"source /etc/profile && livy-server start\"")
        } else {
            runtime.exec("bash -c \"source /etc/profile && livy-server start\"")
        }.waitFor() shouldBe 0
    }

    private fun startMockServer() {
        val client = object : Client {
            override fun executeRequest(request: Request): Response = routers.executeRequest(request)
        }
        FuelManager.instance.client = client
        sessionStore = mutableMapOf()
        statementStore = mutableMapOf()
        nextId = -1
    }

    fun stop() {
        if (livyServerExists && !mockLivyServer) {
            stopRealServer()
        } else {
            stopMockServer()
        }
    }

    private fun stopRealServer() {
        if (MachineUtil.systemInfo.isWindows) {
            runtime.exec("wsl bash -c \"source /etc/profile && livy-server stop\"")
        } else {
            runtime.exec("bash -c \"source /etc/profile && livy-server stop\"")
        }.waitFor() shouldBe 0
    }

    private fun stopMockServer() {
        FuelManager.instance.client = defaultClient
        sessionStore = mutableMapOf()
        statementStore = mutableMapOf()
        nextId = -1
    }
}