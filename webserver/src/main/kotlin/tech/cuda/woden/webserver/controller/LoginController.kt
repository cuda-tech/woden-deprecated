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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.web.bind.annotation.*
import tech.cuda.woden.service.UserService
import tech.cuda.woden.webserver.Response
import javax.validation.constraints.NotBlank

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/api/login")
class LoginController {

    /**
     * @api {post} /api/login 用户登录
     * @apiDescription 用户登录，若账号密码正确，则返回 token，后续的其他请求都需要设置这个 token
     * @apiGroup Login
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {String} username 登录名
     * @apiParam {String} password 登录密码
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1ODM2ODI0MTQsInVzZXJuYW1lIjoicm9vdCJ9.LQuBenBb9UymkBfmzYkxCvrq5ecVL2YGqAyAtSU2IjY"}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"登录失败"}
     */
    @PostMapping
    fun login(@NotBlank(message = "{required}") username: String,
              @NotBlank(message = "{required}") password: String): Map<String, Any> {
        return try {
            val token = UserService.sign(username, password)
            if (token != null) {
                Response.Success.data("token" to token)
            } else {
                Response.Failed.WithError("登录失败")
            }
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }
}