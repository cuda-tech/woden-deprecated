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
import tech.cuda.datahub.webserver.ResponseData
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.web.bind.annotation.*
import tech.cuda.datahub.i18n.I18N
import tech.cuda.datahub.service.MachineService
import java.lang.Exception

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/api/machine")
class MachineController {

    /**
     * @api {get} /api/machine 获取机器列表
     * @apiDescription 获取机器列表，支持分页查询和模糊查询
     * @apiGroup Machine
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} [page = 1] 分页ID
     * @apiParam {Number} [pageSize = 9999] 分页大小
     * @apiParam {String} like 机器 hostname 模糊匹配，多个词用空格分隔，null 字符串会被忽
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"machines":[{"id":1,"hostname":"new host name","mac":"1F-72-5B-F7-10-AB","ip":"107.116.90.29","cpuLoad":34,"memLoad":31,"diskUsage":63,"createTime":"2029-06-06 19:57:08","updateTime":"2020-05-23 01:40:25"},{"id":3,"hostname":"nknvleif","mac":"9E-EE-49-FA-00-F4","ip":"192.168.1.1","cpuLoad":98,"memLoad":48,"diskUsage":31,"createTime":"2035-11-05 14:17:43","updateTime":"2020-05-23 01:40:25"},{"id":5,"hostname":"anything","mac":"7D-75-70-DE-73-0E","ip":"192.168.1.2","cpuLoad":1,"memLoad":60,"diskUsage":59,"createTime":"2004-11-28 21:50:06","updateTime":"2020-05-23 01:40:25"}],"count":188}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @GetMapping
    fun listing(@RequestParam(required = false, defaultValue = "1") page: Int,
                @RequestParam(required = false, defaultValue = "9999") pageSize: Int,
                @RequestParam(required = false) like: String?): ResponseData {
        val (machines, count) = MachineService.listing(page, pageSize, like)
        return Response.Success.data("machines" to machines, "count" to count)
    }

    /**
     * @api {get} /api/machine/{id} 查询机器
     * @apiDescription 查询指定 ID 的机器详情
     * @apiGroup Machine
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"machine":{"id":1,"hostname":"new host name","mac":"1F-72-5B-F7-10-AB","ip":"107.116.90.29","cpuLoad":34,"memLoad":31,"diskUsage":63,"createTime":"2029-06-06 19:57:08","updateTime":"2020-05-23 01:40:25"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"调度服务器 2 不存在或已被删除"}
     */
    @GetMapping("{id}")
    fun find(@PathVariable id: Int): ResponseData {
        val machine = MachineService.findById(id)
        return if (machine == null) {
            Response.Failed.WithError("${I18N.machine} $id ${I18N.notExistsOrHasBeenRemove}")
        } else {
            Response.Success.data("machine" to MachineService.findById(id))
        }
    }

    /**
     * @api {post} /api/machine 新建机器
     * @apiDescription 新建一台调度服务器，并将新建后的信息返回
     * @apiGroup Machine
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {String} ip 服务器 IP
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"machine":{"id":247,"hostname":"","mac":"","ip":"192.168.1.20","cpuLoad":0,"memLoad":0,"diskUsage":0,"createTime":"2020-05-23 02:00:43","updateTime":"2020-05-23 02:00:43"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"IP 地址 192.168.1.1 已存在"}
     */
    @PostMapping
    fun create(@RequestParam(required = true) ip: String): ResponseData {
        return try {
            val machine = MachineService.create(ip)
            Response.Success.data("machine" to machine)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务错误")
        }
    }

    /**
     * @api {put} /api/machine/{id} 更新机器信息
     * @apiDescription 更新指定 ID 的机器信息，如果该指定机器已被删除或不存在则失败
     * @apiGroup Machine
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {String} [hostname = null] 机器 hostname，如果不提供则不更新
     * @apiParam {String} [ip = null] 机器 IP，如果不提供则不更新
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"machine":{"id":3,"hostname":"nknvleif","mac":"9E-EE-49-FA-00-F4","ip":"192.168.1.21","cpuLoad":98,"memLoad":48,"diskUsage":31,"createTime":"2035-11-05 14:17:43","updateTime":"2020-05-23 12:28:03"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"IP 地址 192.168.1.21 已存在"}
     */
    @PutMapping("{id}")
    fun update(@PathVariable id: Int,
               @RequestParam(required = false) hostname: String?,
               @RequestParam(required = false) ip: String?): ResponseData {
        return try {
            val machine = MachineService.update(id, hostname = hostname, ip = ip)
            Response.Success.data("machine" to machine)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务错误")
        }
    }

    /**
     * @api {delete} /api/machine/{id} 删除机器
     * @apiDescription 删除指定 ID 的机器，如果该指定机器已被删除或不存在则失败
     * @apiGroup Machine
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","message":"调度服务器 1 已被删除"}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"调度服务器 1 不存在或已被删除"}
     */
    @DeleteMapping("{id}")
    fun remove(@PathVariable id: Int): ResponseData {
        return try {
            MachineService.remove(id)
            Response.Success.message("${I18N.machine} $id ${I18N.hasBeenRemove}")
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务错误")
        }
    }

}