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
package tech.cuda.datahub.scheduler.livy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.fuel.jackson.responseObject
import org.apache.log4j.Logger
import tech.cuda.datahub.config.Datahub
import tech.cuda.datahub.scheduler.exception.LivyException
import tech.cuda.datahub.scheduler.livy.session.Session
import tech.cuda.datahub.scheduler.livy.session.SessionKind
import java.util.*

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object LivyClient {

    private val logger: Logger = Logger.getLogger(this.javaClass)
    private val mapper = ObjectMapper().registerKotlinModule()
    private const val protocol = "http"

    private fun FuelError.toLivyException() = LivyException(
        this.response.statusCode,
        this.response.responseMessage,
        mapper.readValue(this.response.body().toByteArray().toString(Charsets.UTF_8), Map::class.java)["msg"]
    )

    /**
     * 返回指定[sessionId]的 session
     * 如果出现异常，则返回 null
     */
    fun getSession(sessionId: Int): Session? {
        val (sessions, error) = "${Datahub.livy.baseUrl}/sessions/$sessionId"
            .httpGet()
            .responseObject<Session>().third
        if (error != null) {
            return null
        }
        return sessions
    }

    /**
     * 返回从[fromId]开始往后的[size]个 sessions
     * 如果出现异常，则返回空数组
     */
    fun listSessions(fromId: Int = 0, size: Int = 10): List<Session> {
        val (response, error) = "${Datahub.livy.baseUrl}/sessions"
            .httpGet(listOf("from" to fromId, "size" to size))
            .responseObject<ListingSessionResponse>().third
        if (error != null) {
            return listOf()
        }
        return response?.sessions ?: listOf()
    }


    /**
     * 在队列 [queue] 上创建一个类型为 [kind]，名称为 [name] 的 Session
     * 如果依赖于某些资源文件，可通过 [jars]、[pyFiles]、[files]、[archives] 进行指定
     * 如果需要指定某些 spark 配置，可通过 [conf] 进行指定
     * 如果需要指定资源分配，可设置 [driverMemory]、[driverCores]、[executorMemory]、[executorCores]、[numExecutors]
     * 当闲置超过 [heartbeatTimeoutInSecond] 秒后则回收该 Session
     */
    fun createSession(
        kind: SessionKind,
        jars: List<String> = listOf(),
        pyFiles: List<String> = listOf(),
        files: List<String> = listOf(),
        archives: List<String> = listOf(),
        driverMemory: String = "512M",
        driverCores: Int = 1,
        executorMemory: String = "2G",
        executorCores: Int = 2,
        numExecutors: Int = 2,
        queue: String = "default",
        name: String? = null,
        conf: Map<String, Any> = mapOf(),
        heartbeatTimeoutInSecond: Int = 600
    ): Session {
        val (session, error) = Fuel.post("${Datahub.livy.baseUrl}/sessions")
            .header("Content-Type", "application/json")
            .objectBody(mapOf(
                "kind" to kind,
                "jars" to jars,
                "pyFiles" to pyFiles,
                "files" to files,
                "archives" to archives,
                "driverMemory" to driverMemory,
                "driverCores" to driverCores,
                "executorMemory" to executorMemory,
                "executorCores" to executorCores,
                "numExecutors" to numExecutors,
                "queue" to queue,
                "name" to (name ?: "Livy-Job${UUID.randomUUID().toString().replace("-", "").toLowerCase()}"),
                "conf" to conf,
                "heartbeatTimeoutInSecond" to heartbeatTimeoutInSecond
            )).responseObject<Session>().third

        if (error != null) {
            logger.error(error.message)
            throw error.toLivyException()
        }
        return session ?: throw LivyException("Unknown Error")
    }

}