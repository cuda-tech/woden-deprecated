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

import datahub.dao.Users.bindTo
import datahub.models.FileMirror
import me.liuwj.ktorm.schema.*

/**
 * @author Jensen Qi
 * @since 1.0.0
 */
@ColumnsDef("""
    id              bigint unsigned comment '文件镜像 ID' auto_increment primary key,
    file_id         bigint unsigned comment '文件 ID',
    content         text            comment '文件内容',
    message         varchar(512)    comment '镜像注释',
    is_remove       bool            comment '是否逻辑删除',
    create_time     datetime        comment '创建事件',
    update_time     datetime        comment '更新事件',
    key idx_file_id(is_remove, file_id)
""")
object FileMirrors : Table<FileMirror>("file_mirrors") {
    val id by int("id").primaryKey().bindTo { it.id }
    val fileId by int("file_id").bindTo { it.fileId }
    val content by text("content").bindTo { it.content }
    val message by varchar("message").bindTo { it.message }
    val isRemove by boolean("is_remove").bindTo { it.isRemove }
    val createTime by datetime("create_time").bindTo { it.createTime }
    val updateTime by datetime("update_time").bindTo { it.updateTime }
}