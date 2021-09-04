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
package tech.cuda.woden.common.service.dao

import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.*
import tech.cuda.woden.annotation.mysql.*
import tech.cuda.woden.common.service.po.TaskPO
import tech.cuda.woden.common.service.po.dtype.ScheduleDependencyInfo
import tech.cuda.woden.common.service.po.dtype.ScheduleFormat
import tech.cuda.woden.common.service.po.dtype.SchedulePeriod
import tech.cuda.woden.common.service.po.dtype.SchedulePriority

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object TaskDAO : Table<TaskPO>("task") {
    @BIGINT
    @COMMENT("任务 ID")
    @PRIMARY_KEY
    @AUTO_INCREMENT
    val id = int("id").primaryKey().bindTo { it.id }

    @VARCHAR(512)
    @COMMENT("任务名")
    val name = varchar("name").bindTo { it.name }

    @VARCHAR(256)
    @COMMENT("文件路径")
    val filePath = varchar("file_path").bindTo { it.filePath }

    @BIGINT
    @COMMENT("负责人")
    val ownerId = int("owner_id").bindTo { it.ownerId }

    @TEXT
    @COMMENT("执行参数")
    val args = json<Map<String, Any>>("args").bindTo { it.args }

    @BOOL
    @COMMENT("执行失败是否跳过")
    val isSoftFail = boolean("is_soft_fail").bindTo { it.isSoftFail }

    @VARCHAR(10)
    @COMMENT("调度周期")
    val period = enum<SchedulePeriod>("period").bindTo { it.period }

    @JSON
    @COMMENT("调度时间格式")
    val format = json<ScheduleFormat>("format").bindTo { it.format }

    @VARCHAR(32)
    @COMMENT("执行队列")
    val queue = varchar("queue").bindTo { it.queue }

    @VARCHAR(10)
    @COMMENT("调度优先级")
    val priority = enum<SchedulePriority>("priority").bindTo { it.priority }

    @INT
    @COMMENT("最大等待时间（分钟）")
    val pendingTimeout = int("pending_timeout").bindTo { it.pendingTimeout }

    @INT
    @COMMENT("最大执行时间（分钟）")
    val runningTimeout = int("running_timeout").bindTo { it.runningTimeout }

    @SMALLINT
    @COMMENT("重试次数")
    val retries = int("retries").bindTo { it.retries }

    @INT
    @COMMENT("重试间隔")
    val retryDelay = int("retry_delay").bindTo { it.retryDelay }

    @BOOL
    @COMMENT("调度是否生效")
    val isValid = boolean("is_valid").bindTo { it.isValid }

    @BOOL
    @COMMENT("逻辑删除")
    val isRemove = boolean("is_remove").bindTo { it.isRemove }

    @DATETIME
    @COMMENT("创建时间")
    val createTime = datetime("create_time").bindTo { it.createTime }

    @DATETIME
    @COMMENT("更新时间")
    val updateTime = datetime("update_time").bindTo { it.updateTime }
}