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
package datahub.api.controller

import com.google.common.collect.Lists
import datahub.api.Response
import datahub.api.ResponseData
import datahub.api.utils.Page
import datahub.dao.FileMirrors
import datahub.dao.Files
import datahub.models.FileMirror
import datahub.models.dtype.FileType
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.findById
import me.liuwj.ktorm.schema.ColumnDeclaring
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * 文件镜像接口，只允许查询、创建、删除(不建议)，不允许修改
 * @author Jensen Qi 2020/02/14
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
     * {}
     * @apiSuccessExample 请求失败
     * {}
     */
    @GetMapping
    fun listing(@PathVariable fileId: Int,
                @RequestParam(required = false, defaultValue = "1") page: Int,
                @RequestParam(required = false, defaultValue = "9999") pageSize: Int,
                @RequestParam(required = false) like: String?): ResponseData {
        val fileMirrors = FileMirrors.select().where {
            val conditions = Lists.newArrayList<ColumnDeclaring<Boolean>>(
                FileMirrors.isRemove eq false,
                FileMirrors.fileId eq fileId
            )
            if (like != null && like.isNotBlank() && like.trim().toUpperCase() != "NULL") {
                like.split("\\s+".toRegex()).forEach {
                    conditions.add(FileMirrors.message.like("%$it%"))
                }
            }
            conditions.reduce { a, b -> a and b }
        }
        val count = fileMirrors.totalRecords
        return Response.Success.WithData(mapOf(
            "count" to count,
            "fileMirrors" to fileMirrors.orderBy(FileMirrors.id.asc()).limit(Page.offset(page, pageSize), pageSize).map {
                FileMirrors.createEntity(it)
            }
        ))
    }


    /**
     * @api {delete} /api/file/{fileId}/mirror/{id} 获取指定文件镜像
     * @apiDescription 获取指定文件的指定镜像，如果镜像不存在或被删除，则返回错误
     * @apiGroup FileMirror
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {}
     * @apiSuccessExample 请求失败
     * {}
     */
    @GetMapping("{id}")
    fun find(@PathVariable fileId: Int, @PathVariable id: Int): ResponseData {
        val fileMirror = FileMirrors.select().where {
            FileMirrors.fileId eq fileId and (FileMirrors.id eq id) and (FileMirrors.isRemove eq false)
        }.map { FileMirrors.createEntity(it) }.firstOrNull()
        return if (fileMirror == null) {
            Response.Failed.DataNotFound("file mirror $id")
        } else {
            Response.Success.WithData(mapOf("fileMirror" to fileMirror))
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
     * {}
     * @apiSuccessExample 请求失败
     * {}
     */
    @PostMapping
    fun create(@PathVariable fileId: Int, @RequestParam(required = true) message: String): ResponseData {
        val file = Files.findById(fileId)
        return if (file == null || file.isRemove) {
            Response.Failed.DataNotFound("file $fileId")
        } else if (file.type == FileType.DIR) {
            Response.Failed.IllegalArgument("目录不允许创建镜像")
        } else {
            val fileMirror = FileMirror {
                this.fileId = fileId
                this.content = file.content ?: ""
                this.message = message
                this.isRemove = false
                this.createTime = LocalDateTime.now()
                this.updateTime = LocalDateTime.now()
            }
            FileMirrors.add(fileMirror)
            return Response.Success.WithData(mapOf("fileMirror" to fileMirror))
        }
    }

    /**
     * @api {delete} /api/file/{fileId}/mirror/{id} 删除镜像
     * @apiDescription 删除指定文件的指定镜像，如果镜像已经被删除，或者文件 ID 跟镜像 ID 不匹配，则返回错误。不建议使用这个接口，应该尽量镜像只增不减
     * @apiGroup FileMirror
     * @apiVersion 0.1.0
     * @apiHeader {String} token 用户授权 token
     * @apiSuccessExample 请求成功
     * {}
     * @apiSuccessExample 请求失败
     * {}
     */
    @DeleteMapping("{id}")
    fun remove(@PathVariable fileId: Int, @PathVariable id: Int): ResponseData {
        val fileMirror = FileMirrors.select().where {
            FileMirrors.fileId eq fileId and (FileMirrors.id eq id) and (FileMirrors.isRemove eq false)
        }.map { FileMirrors.createEntity(it) }.firstOrNull()
        return if (fileMirror == null) {
            Response.Failed.DataNotFound("file mirror $id")
        } else {
            fileMirror.isRemove = true
            fileMirror.updateTime = LocalDateTime.now()
            fileMirror.flushChanges()
            Response.Success.Remove("file mirror $id")
        }
    }

}