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
package tech.cuda.datahub.webserver.controller

import tech.cuda.datahub.webserver.Response
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import tech.cuda.datahub.i18n.I18N
import tech.cuda.datahub.service.UserService
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.webserver.ResponseData

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
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
     * {"status":"success","data":{"count":143,"users":[{"id":177,"groupIds":[1,2],"name":"yUEtMsgswR","email":"yUEtMsgswR@aliyun.com","createTime":"2010-06-0518:23:58","updateTime":"2011-09-2300:47:13"},{"id":178,"groupIds":[1,2,3],"name":"snaspGzKcI","email":"snaspGzKcI@outlook.com","createTime":"2032-11-1808:54:43","updateTime":"2035-04-0704:26:21"}]}}
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
     * {"status":"success","data":{"user":{"id":1,"groupIds":[1],"name":"root","email":"root@datahub.com","createTime":"2048-08-14 06:10:35","updateTime":"2051-03-13 21:06:23"}}}
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
     * {"status":"success","data":{"user":{"id":3,"groupIds":[2,3,4,6,8],"name":"OHzXwnDAAd","email":"OHzXwnDAAd@189.com","createTime":"2041-11-20 12:44:46","updateTime":"2044-05-12 14:09:07"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"user 2 not found"}
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
     * {"status":"success","data":{"user":{"id":180,"groupIds":[2,3],"name":"rootaa","email":"aaaaa","createTime":"2020-03-07 22:58:26","updateTime":"2020-03-07 22:58:26"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
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
     * {"status":"success","data":{"user":{"id":10,"groupIds":[3,6],"name":"IinOzxLt","email":"IinOzxLtGL@139.com","createTime":"2018-03-21 03:59:24","updateTime":"2020-03-07 23:00:34"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
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
     * {"status":"success","message":"user 2 has been removed"}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @DeleteMapping("{id}")
    fun remove(@PathVariable id: Int) = try {
        UserService.remove(id)
        Response.Success.message("用户 $id 已被删除")
    } catch (e: NotFoundException) {
        Response.Failed.WithError(e.message ?: "系统异常")
    }


}
