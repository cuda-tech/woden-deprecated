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

import tech.cuda.datahub.service.po.TaskPO
import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.*
import tech.cuda.datahub.annotation.mysql.*
import tech.cuda.datahub.service.po.dtype.ScheduleDependencyInfo
import tech.cuda.datahub.service.po.dtype.ScheduleFormat
import tech.cuda.datahub.service.po.dtype.SchedulePeriod
import tech.cuda.datahub.service.po.dtype.SchedulePriority

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@STORE_IN_MYSQL
internal object TaskDAO : Table<TaskPO>("tasks") {
    @BIGINT
    @COMMENT("任务 ID")
    @PRIMARY_KEY
    @AUTO_INCREMENT
    val id by int("id").primaryKey().bindTo { it.id }

    @BIGINT
    @COMMENT("镜像 ID")
    val mirrorId by int("mirror_id").bindTo { it.mirrorId }

    @BIGINT
    @COMMENT("项目组 ID")
    val groupId by int("group_id").bindTo { it.groupId }

    @VARCHAR(512)
    @COMMENT("任务名")
    val name by varchar("name").bindTo { it.name }

    @JSON
    @COMMENT("负责人")
    val owners by json("owners", typeRef<Set<Int>>()).bindTo { it.owners }

    @TEXT
    @COMMENT("执行参数")
    val args by json("args", typeRef<Map<String, Any>>()).bindTo { it.args }

    @BOOL
    @COMMENT("执行失败是否跳过")
    val isSoftFail by boolean("is_soft_fail").bindTo { it.isSoftFail }

    @VARCHAR(10)
    @COMMENT("调度周期")
    val period by enum("period", typeRef<SchedulePeriod>()).bindTo { it.period }

    @JSON
    @COMMENT("调度时间格式")
    val format by json("format", typeRef<ScheduleFormat>()).bindTo { it.format }

    @VARCHAR(32)
    @COMMENT("执行队列")
    val queue by varchar("queue").bindTo { it.queue }

    @VARCHAR(10)
    @COMMENT("调度优先级")
    val priority by enum("priority", typeRef<SchedulePriority>()).bindTo { it.priority }

    @INT
    @COMMENT("最大等待时间（分钟）")
    val pendingTimeout by int("pending_timeout").bindTo { it.pendingTimeout }

    @INT
    @COMMENT("最大执行时间（分钟）")
    val runningTimeout by int("running_timeout").bindTo { it.runningTimeout }

    @JSON
    @COMMENT("父任务列表")
    val parent by json("parent", typeRef<Map<Int, ScheduleDependencyInfo>>()).bindTo { it.parent }

    @JSON
    @COMMENT("子任务列表")
    val children by json("children", typeRef<Set<Int>>()).bindTo { it.children }

    @SMALLINT
    @COMMENT("重试次数")
    val retries by int("retries").bindTo { it.retries }

    @INT
    @COMMENT("重试间隔")
    val retryDelay by int("retry_delay").bindTo { it.retryDelay }

    @BOOL
    @COMMENT("调度是否生效")
    val isValid by boolean("is_valid").bindTo { it.isValid }

    @BOOL
    @COMMENT("逻辑删除")
    val isRemove by boolean("is_remove").bindTo { it.isRemove }

    @DATETIME
    @COMMENT("创建时间")
    val createTime by datetime("create_time").bindTo { it.createTime }

    @DATETIME
    @COMMENT("更新时间")
    val updateTime by datetime("update_time").bindTo { it.updateTime }
}