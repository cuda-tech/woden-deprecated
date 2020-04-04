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

import datahub.models.User
import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.*


/**
 * @author Jensen Qi
 * @since 1.0.0
 */
@ColumnsDef("""
    id              int unsigned    comment '用户 ID' auto_increment primary key,
    groups          json            comment '项目组 ID',
    name            varchar(256)    comment '用户名',
    email           varchar(256)    comment '登录名',
    password        varchar(256)    comment '登录密码密文',
    is_remove       bool            comment '逻辑删除',
    create_time     datetime        comment '创建时间',
    update_time     datetime        comment '更新时间',
    key idx_name(is_remove, name)
""")
object Users : Table<User>("users") {
    val id by int("id").primaryKey().bindTo { it.id }
    val groups by json("groups", typeRef<Set<Int>>()).bindTo { it.groups }
    val name by varchar("name").bindTo { it.name }
    val email by varchar("email").bindTo { it.email }
    val password by varchar("password").bindTo { it.password }
    val isRemove by boolean("is_remove").bindTo { it.isRemove }
    val createTime by datetime("create_time").bindTo { it.createTime }
    val updateTime by datetime("update_time").bindTo { it.updateTime }
}
