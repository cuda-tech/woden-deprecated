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

import tech.cuda.datahub.service.model.User
import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.*

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
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
