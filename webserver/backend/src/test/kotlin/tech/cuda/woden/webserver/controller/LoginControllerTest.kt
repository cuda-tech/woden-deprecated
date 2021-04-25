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
package tech.cuda.woden.webserver.controller

import io.kotest.matchers.shouldBe
import tech.cuda.woden.common.service.PersonService
import tech.cuda.woden.webserver.RestfulTestToolbox

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
open class LoginControllerTest : RestfulTestToolbox("person") {

    @Test
    fun login() {
        val token = postman.post("/api/login", mapOf("name" to "root", "password" to "root")).shouldSuccess
            .get<String>("token")
        PersonService.getPersonByToken(token)?.name shouldBe "root"

        postman.post("/api/login", mapOf("name" to "root", "password" to "wrong password"))
            .shouldFailed.withError("登录失败")

        postman.post("/api/login", mapOf("name" to "wrong username", "password" to "root"))
            .shouldFailed.withError("登录失败")
    }
}