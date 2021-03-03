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
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.ContainerService
import tech.cuda.woden.webserver.Response
import tech.cuda.woden.webserver.ResponseData

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/api/container")
class ContainerController {

    /**
     * @api {get} /api/container 获取容器列表
     * @apiDescription 获取容器列表，支持分页查询和模糊查询
     * @apiGroup Container
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} [page = 1] 分页ID
     * @apiParam {Number} [pageSize = 9999] 分页大小
     * @apiParam {String} like 容器 hostname 模糊匹配，多个词用空格分隔，null 字符串会被忽
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"containers":[{"id":1,"hostname":"new host name""cpuLoad":34,"memLoad":31,"diskUsage":63,"createTime":"2029-06-06 19:57:08","updateTime":"2020-05-23 01:40:25"},{"id":3,"hostname":"nknvleif","cpuLoad":98,"memLoad":48,"diskUsage":31,"createTime":"2035-11-05 14:17:43","updateTime":"2020-05-23 01:40:25"},{"id":5,"hostname":"anything","cpuLoad":1,"memLoad":60,"diskUsage":59,"createTime":"2004-11-28 21:50:06","updateTime":"2020-05-23 01:40:25"}],"count":188}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @GetMapping
    fun listing(@RequestParam(required = false, defaultValue = "1") page: Int,
                @RequestParam(required = false, defaultValue = "9999") pageSize: Int,
                @RequestParam(required = false) like: String?): ResponseData {
        val (containers, count) = ContainerService.listing(page, pageSize, like)
        return Response.Success.data("containers" to containers, "count" to count)
    }

    /**
     * @api {get} /api/container/{id} 查询容器
     * @apiDescription 查询指定 ID 的容器详情
     * @apiGroup Container
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"container":{"id":1,"hostname":"new host name","cpuLoad":34,"memLoad":31,"diskUsage":63,"createTime":"2029-06-06 19:57:08","updateTime":"2020-05-23 01:40:25"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"调度容器 2 不存在或已被删除"}
     */
    @GetMapping("{id}")
    fun find(@PathVariable id: Int): ResponseData {
        val container = ContainerService.findById(id)
        return if (container == null) {
            Response.Failed.WithError("${I18N.container} $id ${I18N.notExistsOrHasBeenRemove}")
        } else {
            Response.Success.data("container" to ContainerService.findById(id))
        }
    }

}