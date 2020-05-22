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
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import tech.cuda.datahub.i18n.I18N
import tech.cuda.datahub.service.FileService
import tech.cuda.datahub.service.UserService
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.po.dtype.FileType
import java.lang.Exception

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/api/file")
class FileController {

    /**
     * @api {get} /api/file 获取文件列表
     * @apiDescription 获取指定父节点的文件列表，或全局模糊查找
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} [parentId = null] 父节点 ID
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"files":[{"id":4,"groupId":1,"ownerId":26,"name":"zwgjydgn","type":"DIR","parentId":1,"createTime":"2002-05-14 08:16:08","updateTime":"2004-04-17 08:43:14"},{"id":43,"groupId":1,"ownerId":140,"name":"kniovyqn","type":"SQL","parentId":1,"createTime":"2009-09-17 00:47:55","updateTime":"2011-11-10 01:43:32"},{"id":6,"groupId":1,"ownerId":167,"name":"ladlehnr","type":"SQL","parentId":1,"createTime":"2003-09-09 05:14:44","updateTime":"2004-06-15 14:47:45"},{"id":2,"groupId":1,"ownerId":10,"name":"jldwzlys","type":"SPARK","parentId":1,"createTime":"2048-12-27 13:12:08","updateTime":"2049-01-24 17:09:09"}],"count":4}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @GetMapping
    fun listing(@RequestParam(required = false) parentId: Int): ResponseData {
        val (files, count) = FileService.listChildren(parentId)
        return Response.Success.data("files" to files, "count" to count)
    }

    /**
     * @api {get} /api/file/search 模糊查询
     * @apiDescription 全局模糊查询文件
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {String} [like = null] 文件名模糊匹配，多个词用空格分隔，null 字符串会被忽略
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"files":[{"id":7,"groupId":7,"ownerId":36,"name":"bamvjrno","type":"SQL","parentId":64,"createTime":"2045-08-02 02:39:46","updateTime":"2048-06-19 13:58:27"},{"id":18,"groupId":16,"ownerId":48,"name":"bcmawkte","type":"SQL","parentId":49,"createTime":"2002-05-01 00:41:43","updateTime":"2003-10-20 15:00:30"},{"id":60,"groupId":5,"ownerId":48,"name":"lwbaccod","type":"SQL","parentId":7,"createTime":"2007-02-12 03:45:03","updateTime":"2008-04-14 18:06:49"}],"count":3}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"错误信息"}
     */
    @GetMapping("/search")
    fun search(@RequestParam(required = false) like: String): ResponseData {
        val (files, count) = FileService.search(like)
        return Response.Success.data("files" to files, "count" to count)
    }

    /**
     * @api {get} /api/file/root 获取根目录
     * @apiDescription 获取指定项目组的根目录
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} groupId 项目组 ID
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"file":{"id":1,"groupId":1,"ownerId":1,"name":"root_project","type":"DIR","parentId":null,"createTime":"2037-05-20 14:58:39","updateTime":"2040-02-04 21:46:36"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"项目组 3 根目录 不存在或已被删除"}
     */
    @GetMapping("/root")
    fun findRoot(@RequestParam(required = true) groupId: Int): ResponseData {
        return try {
            val file = FileService.findRootByGroupId(groupId)
            Response.Success.data("file" to file)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }


    /**
     * @api {get} /api/file/{id}/parent 获取文件父节点
     * @apiDescription 获取指定文件的父节点，如果文件不存在或被删除则返回错误；如果文件的任何一个父节点不存在或被删除则返回错误；根节点返回空数组
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"files":[{"id":1,"groupId":1,"ownerId":1,"name":"root_project","type":"DIR","parentId":null,"createTime":"2037-05-20 14:58:39","updateTime":"2040-02-04 21:46:36"}],"count":1}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"文件节点 5 不存在或已被删除"}
     */
    @GetMapping("/{id}/parent")
    fun listingParent(@PathVariable id: Int): ResponseData {
        return try {
            val (parent, count) = FileService.listParent(id)
            Response.Success.data("files" to parent, "count" to count)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }


    /**
     * @api {get} /api/file/{id}/content 获取内容
     * @apiDescription 获取指定文件的内容，如果文件不存在或被删除，则返回错误；如果文件是目录，则返回错误
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"content":{"content":"jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"文件夹 不允许 获取 内容"}
     */
    @GetMapping("/{id}/content")
    fun getContent(@PathVariable id: Int): ResponseData {
        return try {
            val content = FileService.getContent(id)
            Response.Success.data("content" to content)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }

    /**
     * @api {post} /api/file 创建文件节点
     * @apiDescription 创建文件节点，并返回创建后的数据
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} groupId 归属的项目组 ID
     * @apiParam {String} name 节点名称
     * @apiParam {Enum} type 文件类型，可选 DIR、 SQL、 SPARK、 MR
     * @apiParam {Number} parentId 父节点 ID
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"file":{"id":70,"groupId":1,"ownerId":1,"name":"testCreate","type":"SQL","parentId":1,"createTime":"2020-05-23 12:49:53","updateTime":"2020-05-23 12:49:53"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"文件夹 1 存在 文件类型 SQL 文件节点 testCreate"}
     */
    @PostMapping
    fun create(@RequestParam(required = true) groupId: Int,
               @RequestParam(required = true) name: String,
               @RequestParam(required = true) type: FileType,
               @RequestParam(required = true) parentId: Int): ResponseData {
        val servlet = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = servlet.request
        return try {
            val file = FileService.create(
                groupId = groupId,
                name = name,
                type = type,
                parentId = parentId,
                user = UserService.getUserByToken(request.getHeader("TOKEN"))
                    ?: throw NotFoundException(I18N.user, I18N.notExistsOrHasBeenRemove)
            )
            Response.Success.data("file" to file)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }

    /**
     * @api {put} /api/file/{id} 更新文件信息
     * @apiDescription 更新指定 ID 的文件信息
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiParam {Number} [ownerId = null] 文件归属者 ID
     * @apiParam {String} [name = null] 文件名
     * @apiParam {Number} [version = null] 文件版本号
     * @apiParam {Number} [parentId = null] 父节点 ID
     * @apiSuccessExample 请求成功
     * {"status":"success","data":{"file":{"id":4,"groupId":1,"ownerId":26,"name":"testUpdate","type":"DIR","parentId":1,"createTime":"2002-05-14 08:16:08","updateTime":"2020-05-23 12:48:46"}}}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"根目录 禁止更新"}
     */
    @PutMapping("{id}")
    fun update(@PathVariable id: Int,
               @RequestParam(required = false) ownerId: Int?,
               @RequestParam(required = false) name: String?,
               @RequestParam(required = false) content: String?,
               @RequestParam(required = false) parentId: Int?): ResponseData {
        return try {
            val file = FileService.update(id, ownerId = ownerId, name = name, content = content, parentId = parentId)
            Response.Success.data("file" to file)
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }

    /**
     * @api {delete} /api/file/{id} 删除文件
     * @apiDescription 删除指定 ID 的文件节点，如果这是一个文件夹节点，则递归地删除其子节点
     * @apiGroup File
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {"status":"success","message":"文件节点 3 已被删除"}
     * @apiSuccessExample 请求失败
     * {"status":"failed","error":"根目录 禁止删除"}
     */
    @DeleteMapping("{id}")
    fun remove(@PathVariable id: Int): ResponseData {
        return try {
            FileService.remove(id)
            Response.Success.message("${I18N.file} $id ${I18N.hasBeenRemove}")
        } catch (e: Exception) {
            Response.Failed.WithError(e.message ?: "服务异常")
        }
    }

}