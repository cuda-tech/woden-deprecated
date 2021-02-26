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
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.UserService
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.webserver.Response
import tech.cuda.woden.webserver.ResponseData

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/api/user")
class UserController {

    /**
     * @api {get} /api/user 获取用户列表
     * @apiDescription 获取用户列表，支持分页和模糊查询
     * @apiGroup User
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} [page = 1] 分页ID
     * @apiParam {Number} [pageSize = 9999] 分页大小
     * @apiParam {String} like 用户名模糊匹配，多个词用空格分隔，null 字符串会被忽略
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"count":144,"users":[{"id":1,"groups":[1],"name":"root","email":"root@woden.com","createTime":"2048-08-14 06:10:35","updateTime":"2051-03-13 21:06:23"},{"id":2,"groups":[1,2,5,6,7,8,9],"name":"guest","email":"guest@woden.com","createTime":"2041-02-10 19:37:55","updateTime":"2042-03-23 08:54:17"},{"id":3,"groups":[2,3,4,6,8],"name":"OHzXwnDAAd","email":"OHzXwnDAAd@189.com","createTime":"2041-11-20 12:44:46","updateTime":"2044-05-12 14:09:07"}]}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @GetMapping
    fun listing(@RequestParam(required = false, defaultValue = "1") page: Int,
                @RequestParam(required = false, defaultValue = "9999") pageSize: Int,
                @RequestParam(required = false) like: String?): Map<String, Any> {
        val (users, count) = UserService.listing(page, pageSize, like)
        return Response.Success.data("count" to count, "users" to users)
    }

    /**
     * @api {get} /api/user/current 获取当前登录用户
     * @apiGroup User
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"user":{"id":1,"groups":[1],"name":"root","email":"root@woden.com","createTime":"2048-08-14 06:10:35","updateTime":"2051-03-13 21:06:23"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @GetMapping("/current")
    fun currentUser(): Map<String, Any> {
        val servlet = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = servlet.request
        val user = UserService.getUserByToken(request.getHeader("TOKEN")) ?: return Response.Failed.DataNotFound("")
        return Response.Success.data("user" to user)
    }


    /**
     * @api {get} /api/user/{id} 查找用户
     * @apiDescription 查找指定 ID 的用户，查找已删除的或不不存在的用户将返回错误
     * @apiGroup User
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"user":{"id":10,"groups":[1,3,4,5,6,8,9],"name":"IinOzxLtGL","email":"IinOzxLtGL@139.com","createTime":"2018-03-21 03:59:24","updateTime":"2019-02-28 12:26:06"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"用户 11 不存在或已被删除"}
     */
    @GetMapping("{id}")
    fun find(@PathVariable id: Int): ResponseData {
        val user = UserService.findById(id)
        return if (user == null) {
            Response.Failed.WithError("${I18N.user} $id ${I18N.notExistsOrHasBeenRemove}")
        } else {
            Response.Success.data("user" to UserService.findById(id))
        }
    }


    /**
     * @api {post} /api/user 创建用户
     * @apiDescription 创建用户，并返回创建后的数据
     * @apiGroup User
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {String} name 用户登录名
     * @apiParam {String} password 用户登录密码
     * @apiParam {Array} groupIds 用户归属项目组 ID
     * @apiParam {String} email 用户邮箱
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"user":{"id":180,"groups":[1,2,3],"name":"testName","email":"testEmail","createTime":"2020-05-22 01:43:10","updateTime":"2020-05-22 01:43:10"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"用户 testName 已存在"}
     */
    @PostMapping
    fun create(@RequestParam(required = true) name: String,
               @RequestParam(required = true) password: String,
               @RequestParam(required = true) groupIds: ArrayList<Int>,
               @RequestParam(required = true) email: String) = try {
        Response.Success.data("user" to UserService.create(name, password, groupIds.toSet(), email))
    } catch (e: DuplicateException) {
        Response.Failed.WithError(e.message ?: "系统异常")
    }

    /**
     * @api {put} /api/user/{id} 更新用户信息
     * @apiDescription 更新指定 ID 的用户信息，并返回更新后的数据
     * @apiGroup User
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {String} [name = null] 用户登录名，不指定则不更新
     * @apiParam {String} [password = null] 用户登录密码，不指定则不更新
     * @apiParam {Array} [groupIds = null] 用户归属项目组，不指定则不更新
     * @apiParam {String} [email = null] 用户邮箱，不指定则不更新
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"user":{"id":180,"groups":[1,2,3],"name":"testName","email":"testEmail","createTime":"2020-05-22 01:43:11","updateTime":"2020-05-22 01:49:55"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"用户 root 已存在"}
     */
    @PutMapping("{id}")
    fun update(@PathVariable id: Int,
               @RequestParam(required = false) name: String?,
               @RequestParam(required = false) password: String?,
               @RequestParam(required = false) groupIds: ArrayList<Int>?,
               @RequestParam(required = false) email: String?) = try {
        Response.Success.data("user" to UserService.update(id, name, password, groupIds?.toSet(), email))
    } catch (e: Exception) {
        Response.Failed.WithError(e.message ?: "系统异常")
    }


    /**
     * @api {delete} /api/user/{id} 删除用户
     * @apiDescription 删除指定 ID 的用户
     * @apiGroup User
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","message":"用户 10 已被删除"}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"用户 9 不存在或已被删除"}
     */
    @DeleteMapping("{id}")
    fun remove(@PathVariable id: Int) = try {
        UserService.remove(id)
        Response.Success.message("${I18N.user} $id ${I18N.hasBeenRemove}")
    } catch (e: NotFoundException) {
        Response.Failed.WithError(e.message ?: "系统异常")
    }


}
