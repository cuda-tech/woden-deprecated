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
import tech.cuda.datahub.service.FileMirrorService
import java.lang.Exception

/**
 * 文件镜像接口，只允许查询、创建、删除(不建议)，不允许修改
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/api/file/{fileId}/mirror")
class FileMirrorController {

    /**
     * @api {get} /api/file/{fileId}/mirror 获取文件镜像列表
     * @apiDescription 获取指定文件的镜像列表，支持分页查询和按镜像注释模糊查询
     * @apiGroup FileMirror
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} [page = 1] 分页ID
     * @apiParam {Number} [pageSize = 9999] 分页大小
     * @apiParam {String} like 注释模糊匹配，多个词用空格分隔，null 字符串会被忽略
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"mirrors":[{"id":40,"fileId":1,"content":"qyeosvfgnborxpqfmqyagyheklugcbiylczhxxtaedyeerkkpmajgskpgntohyyl","message":"krvpcwcg","createTime":"2020-05-04 03:18:07","updateTime":"2023-05-06 00:32:21"},{"id":56,"fileId":1,"content":"wrjmwtrbeoqmkunhifbkybchxhzxrlfcoelzuoobwukvsavhymdtjqlitblzfyxb","message":"eneasves","createTime":"2025-09-07 13:38:33","updateTime":"2025-09-10 09:58:15"},{"id":135,"fileId":1,"content":"iyzzxvkqvjgbhjiewbfnepenadmchhusgrejvjmbgfbbcqhsiijiumghsziaomrm","message":"lirtmdot","createTime":"2042-05-04 19:25:08","updateTime":"2043-10-15 14:11:59"}],"count":9}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @GetMapping
    fun listing(@PathVariable fileId: Int,
                @RequestParam(required = false, defaultValue = "1") page: Int,
                @RequestParam(required = false, defaultValue = "9999") pageSize: Int,
                @RequestParam(required = false) like: String?): ResponseData {
        val (mirrors, count) = FileMirrorService.listing(fileId, page, pageSize, like)
        return Response.Success.data("mirrors" to mirrors, "count" to count)
    }


    /**
     * @api {get} /api/file/{fileId}/mirror/{id} 获取指定文件镜像
     * @apiDescription 获取指定文件的指定镜像，如果镜像不存在或被删除，则返回错误
     * @apiGroup FileMirror
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"mirror":{"id":40,"fileId":1,"content":"qyeosvfgnborxpqfmqyagyheklugcbiylczhxxtaedyeerkkpmajgskpgntohyyl","message":"krvpcwcg","createTime":"2020-05-04 03:18:07","updateTime":"2023-05-06 00:32:21"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"文件镜像 5 不存在或已被删除"}
     */
    @GetMapping("{id}")
    fun find(@PathVariable fileId: Int, @PathVariable id: Int): ResponseData {
        val mirror = FileMirrorService.findById(id)
        return if (mirror == null) {
            Response.Failed.WithError("${I18N.fileMirror} $id ${I18N.notExistsOrHasBeenRemove}")
        } else {
            Response.Success.data("mirror" to mirror)
        }
    }

    /**
     * @api {post} /api/file/{fileId}/mirror 创建镜像
     * @apiDescription 对指定文件文件创建当前状态的一个镜像，并返回创建后的镜像
     * @apiGroup FileMirror
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {String} message 镜像注释
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"mirror":{"id":301,"fileId":3,"content":"jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont","message":"testCreate","createTime":"2020-05-23 12:56:20","updateTime":"2020-05-23 12:56:20"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"文件夹 禁止创建镜像"}
     */
    @PostMapping
    fun create(@PathVariable fileId: Int, @RequestParam(required = true) message: String): ResponseData {
        return try {
            val mirror = FileMirrorService.create(fileId, message)
            Response.Success.data("mirror" to mirror)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }

    /**
     * @api {delete} /api/file/{fileId}/mirror/{id} 删除镜像
     * @apiDescription 删除指定文件的指定镜像，如果镜像已经被删除，或者文件 ID 跟镜像 ID 不匹配，则返回错误。不建议使用这个接口，应该尽量镜像只增不减
     * @apiGroup FileMirror
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","message":"文件镜像 1 已被删除"}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"文件镜像 1 不存在或已被删除"}
     */
    @DeleteMapping("{id}")
    fun remove(@PathVariable fileId: Int, @PathVariable id: Int): ResponseData {
        return try {
            FileMirrorService.remove(id)
            Response.Success.message("${I18N.fileMirror} $id ${I18N.hasBeenRemove}")
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }

}