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
package tech.cuda.woden.common.service

import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.asc
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.global.add
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.dao.FileMirrorDAO
import tech.cuda.woden.common.service.dto.FileMirrorDTO
import tech.cuda.woden.common.service.dto.toFileMirrorDTO
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.exception.OperationNotAllowException
import tech.cuda.woden.common.service.po.FileMirrorPO
import tech.cuda.woden.common.service.po.dtype.FileType
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object FileMirrorService : Service(FileMirrorDAO) {

    /**
     * 批量返回指定文件[fileId]的镜像，支持模糊查询
     */
    fun listing(fileId: Int, page: Int, pageSize: Int, pattern: String? = null): Pair<List<FileMirrorDTO>, Int> {
        val (mirrors, count) = batch<FileMirrorPO>(
            pageId = page,
            pageSize = pageSize,
            filter = FileMirrorDAO.isRemove eq false and (FileMirrorDAO.fileId eq fileId),
            like = FileMirrorDAO.message.match(pattern),
            orderBy = FileMirrorDAO.id.asc()
        )
        return mirrors.map { it.toFileMirrorDTO() } to count
    }


    /**
     * 查找指定[id]的镜像
     * 如果不存在或已被删除，则返回 null
     */
    fun findById(id: Int) = find<FileMirrorPO>(
        where = FileMirrorDAO.isRemove eq false and (FileMirrorDAO.id eq id)
    )?.toFileMirrorDTO()

    /**
     * 将文件节点[fileId]打包镜像
     * 如果给定的节点[fileId]不存在或已被删除，则抛出 NotFoundException
     * 如果给定的节点是文件夹类型，则抛出 IllegalArgumentException
     */
    fun create(fileId: Int, message: String): FileMirrorDTO {
        if (FileService.findById(fileId)?.type == FileType.DIR) {
            throw OperationNotAllowException(I18N.dir, I18N.createMirrorNotAllow)
        }
        val content = FileService.getContent(fileId).content!!
        val mirror = FileMirrorPO {
            this.fileId = fileId
            this.content = content
            this.message = message
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
        }
        FileMirrorDAO.add(mirror)
        return mirror.toFileMirrorDTO()
    }

    /**
     * 删除指定[id]的文件镜像
     * 如果指定的镜像不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) {
        val mirror = find<FileMirrorPO>(
            where = FileMirrorDAO.isRemove eq false and (FileMirrorDAO.id eq id)
        ) ?: throw NotFoundException(I18N.fileMirror, id, I18N.notExistsOrHasBeenRemove)
        mirror.isRemove = true
        mirror.updateTime = LocalDateTime.now()
        mirror.flushChanges()
    }

}