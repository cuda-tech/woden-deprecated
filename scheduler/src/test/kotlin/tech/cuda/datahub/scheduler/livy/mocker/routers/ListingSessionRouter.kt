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
class ListingSessionRouter : Router() {
    override val pathPattern = Regex("/sessions")
    override val method = Method.GET

    override fun execute(request: Request): Response {
        var from = 0
        var size = Integer.MAX_VALUE
        val url = request.url.toString()
        if (url.contains("?")) {
            val arguments = url.substringAfter("?").split("&").map {
                val (k, v) = it.split("=")
                if (k == "from") {
                    from = v.toInt()
                }
                if (k == "size") {
                    size = v.toInt()
                }
            }
        }
        return response(request.url, 200, mapOf(
            "from" to from,
            "total" to LivyServer.sessionStore.size,
            "sessions" to LivyServer.sessionStore
                .filter { it.component1() >= from }
                .map { it.component2().first }
                .sortedBy { it.id }
                .take(size)
        ).toJsonString())
    }
}