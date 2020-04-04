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
package tech.cuda.datahub.service.model

import tech.cuda.datahub.service.model.dtype.FileType
import me.liuwj.ktorm.entity.Entity
import tech.cuda.datahub.annotation.mysql.*
import java.time.LocalDateTime

/**
 * 数据开发中创建的文件或文件夹
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@STORE_IN_MYSQL
interface File : Entity<File> {
    companion object : Entity.Factory<File>()

    @BIGINT
    @UNSIGNED
    @AUTO_INCREMENT
    @PRIMARY_KEY
    @COMMENT("文件 ID")
    val id: Int

    @INT
    @UNSIGNED
    @COMMENT("项目组 ID")
    var groupId: Int

    @INT
    @UNSIGNED
    @COMMENT("创建者 ID")
    var ownerId: Int

    @VARCHAR(128)
    @COMMENT("文件名")
    var name: String

    @VARCHAR(32)
    @COMMENT("文件类型")
    var type: FileType

    @BIGINT
    @UNSIGNED
    @COMMENT("父节点 ID")
    var parentId: Int?

    @TEXT
    @COMMENT("文件内容")
    var content: String?

    @BOOL
    @COMMENT("逻辑删除")
    var isRemove: Boolean

    @DATETIME
    @COMMENT("创建时间")
    var createTime: LocalDateTime

    @DATETIME
    @COMMENT("更新时间")
    var updateTime: LocalDateTime

}