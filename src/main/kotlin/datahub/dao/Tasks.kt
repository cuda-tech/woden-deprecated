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

import datahub.models.Task
import datahub.models.dtype.SchedulePeriod
import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.*


/**
 * @author Jensen Qi
 * @since 1.0.0
 */
@ColumnsDef("""
    id                  bigint unsigned     comment '任务 ID' auto_increment primary key,
    mirror_id           bigint unsigned     comment '镜像 ID',
    name                varchar(512)        comment '任务名',
    owners              text                comment '负责人 ID 列表',
    args                text                comment '执行参数',
    soft_fail           bool                comment '执行失败是否跳过',
    period              varchar(10)         comment '调度周期',
    queue               varchar(32)         comment '执行队列',
    priority            smallint unsigned   comment '优先级',
    pending_timeout     int                 comment '最大等待时间（分钟）',
    running_timeout     int                 comment '最大执行时间（分钟）',
    parent              json                comment '父任务列表',
    children            json                comment '子任务列表',
    retries             smallint unsigned   comment '重试次数',
    retry_delay         int                 comment '重试间隔（分钟）',
    valid               bool                comment '是否生效',
    is_remove           bool                comment '逻辑删除',
    create_time         datetime            comment '创建时间',
    update_time         datetime            comment '更新时间'
""")
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