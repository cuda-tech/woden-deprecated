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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DefaultBody
import tech.cuda.datahub.scheduler.exception.LivyException
import java.net.URL

/**
 * 任务链模式的基类 Router
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
abstract class Router {
    private var nextRouter: Router? = null
    protected val mapper = ObjectMapper().registerKotlinModule()
    abstract val pathPattern: Regex
    abstract val method: Method

    protected abstract fun execute(request: Request): Response

    protected fun Body.toMap() = mapper.readValue<Map<String, Any?>>(this.asString("application/json"))
    protected fun Any.toJsonString(): String = mapper.writeValueAsString(this)

    protected fun response(url: URL, code: Int, body: String, msg: String = ""): Response {
        return Response(url, code, body = DefaultBody(openStream = { body.byteInputStream() }), responseMessage = msg)
    }

    fun setNext(router: Router): Router {
        nextRouter = router
        return router
    }

    fun executeRequest(request: Request): Response = when {
        request.url.path.matches(pathPattern) && request.method == method -> execute(request)
        nextRouter != null -> nextRouter!!.executeRequest(request) // 任务链创建后不要修改为 null，否则这里强转非空可能抛异常
        else -> throw LivyException()
    }


}