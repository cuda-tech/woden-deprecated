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
package datahub.dao

import datahub.models.File
import datahub.models.dtype.FileType
import me.liuwj.ktorm.schema.*

/**
 * @author Jensen Qi
 * @since 1.0.0
 */
@ColumnsDef("""
    id              bigint unsigned comment '文件 ID' auto_increment primary key,
    group_id        int unsigned    comment '项目组 ID',
    owner_id        int unsigned    comment '创建者 ID',
    name            varchar(128)    comment '文件名',
    type            varchar(32)     comment '文件类型',
    parent_id       bigint unsigned comment '父节点 ID，如果为根目录则为 null',
    content         text            comment '文件目录',
    is_remove       bool            comment '是否逻辑删除',
    create_time     datetime        comment '创建时间',
    update_time     datetime        comment '更新时间',
    key idx_group(is_remove, group_id, type),
    key idx_parent(is_remove, parent_id)
""")
object Files : Table<File>("files") {
    val id by int("id").primaryKey().bindTo { it.id }
    val groupId by int("group_id").bindTo { it.groupId }
    val ownerId by int("owner_id").bindTo { it.ownerId }
    val name by varchar("name").bindTo { it.name }
    val type by enum("type", typeRef<FileType>()).bindTo { it.type }
    val parentId by int("parent_id").bindTo { it.parentId }
    val content by text("content").bindTo { it.content }
    val isRemove by boolean("is_remove").bindTo { it.isRemove }
    val createTime by datetime("create_time").bindTo { it.createTime }
    val updateTime by datetime("update_time").bindTo { it.updateTime }
}