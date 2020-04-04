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

import tech.cuda.datahub.service.model.Task
import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.*
import tech.cuda.datahub.service.model.dtype.SchedulePeriod

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object Tasks : Table<Task>("tasks") {
    val id by int("id").primaryKey().bindTo { it.id }
    val mirrorId by int("mirror_id").bindTo { it.mirrorId }
    val name by varchar("name").bindTo { it.name }
    val owners by json("owners", typeRef<Set<Int>>()).bindTo { it.owners }
    val args by text("args").bindTo { it.args }
    val softFail by boolean("soft_fail").bindTo { it.softFail }
    val period by enum("type", typeRef<SchedulePeriod>()).bindTo { it.period }
    val queue by varchar("queue").bindTo { it.queue }
    val priority by int("priority").bindTo { it.priority }
    val pendingTimeout by int("pending_timeout").bindTo { it.pendingTimeout }
    val runningTimeout by int("running_timeout").bindTo { it.runningTimeout }
    val parent by json("parent", typeRef<Map<Int, Map<String, String>>>()).bindTo { it.parent }
    val children by json("children", typeRef<Map<Int, Map<String, String>>>()).bindTo { it.children }
    val retries by int("retries").bindTo { it.retries }
    val retryDelay by int("retry_delay").bindTo { it.retryDelay }
    val valid by boolean("valid").bindTo { it.valid }
    val isRemove by boolean("is_remove").bindTo { it.isRemove }
    val createTime by datetime("create_time").bindTo { it.createTime }
    val updateTime by datetime("update_time").bindTo { it.updateTime }
}