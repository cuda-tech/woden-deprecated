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
package tech.cuda.datahub.service.dao

import tech.cuda.datahub.service.po.FilePO
import tech.cuda.datahub.service.po.dtype.FileType
import me.liuwj.ktorm.schema.*
import tech.cuda.datahub.annotation.mysql.*

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@STORE_IN_MYSQL
internal object FileDAO : Table<FilePO>("files") {
    @BIGINT
    @UNSIGNED
    @AUTO_INCREMENT
    @PRIMARY_KEY
    @NOT_NULL
    @COMMENT("文件 ID")
    val id by int("id").primaryKey().bindTo { it.id }


    @INT
    @UNSIGNED
    @NOT_NULL
    @COMMENT("项目组 ID")
    val groupId by int("group_id").bindTo { it.groupId }

    @INT
    @UNSIGNED
    @NOT_NULL
    @COMMENT("创建者 ID")
    val ownerId by int("owner_id").bindTo { it.ownerId }

    @VARCHAR(128)
    @COMMENT("文件名")
    val name by varchar("name").bindTo { it.name }

    @VARCHAR(32)
    @COMMENT("文件类型")
    val type by enum("type", typeRef<FileType>()).bindTo { it.type }

    @BIGINT
    @UNSIGNED
    @COMMENT("父节点 ID")
    val parentId by int("parent_id").bindTo { it.parentId }

    @TEXT
    @COMMENT("文件内容")
    val content by text("content").bindTo { it.content }

    @BOOL
    @NOT_NULL
    @COMMENT("逻辑删除")
    val isRemove by boolean("is_remove").bindTo { it.isRemove }

    @DATETIME
    @NOT_NULL
    @COMMENT("创建时间")
    val createTime by datetime("create_time").bindTo { it.createTime }

    @DATETIME
    @NOT_NULL
    @COMMENT("更新时间")
    val updateTime by datetime("update_time").bindTo { it.updateTime }
}
