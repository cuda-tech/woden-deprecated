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

import me.liuwj.ktorm.schema.*
import tech.cuda.woden.annotation.mysql.*
import tech.cuda.woden.common.service.po.JobPO
import tech.cuda.woden.common.service.po.dtype.JobStatus

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object JobDAO : Table<JobPO>("jobs") {
    @BIGINT
    @UNSIGNED
    @AUTO_INCREMENT
    @PRIMARY_KEY
    @COMMENT("作业 ID")
    val id = int("id").primaryKey().bindTo { it.id }

    @BIGINT
    @UNSIGNED
    @COMMENT("任务 ID")
    val taskId = int("task_id").bindTo { it.taskId }

    @BIGINT
    @UNSIGNED
    @COMMENT("执行容器 ID")
    val containerId = int("container_id").bindTo { it.containerId }

    @VARCHAR(10)
    @COMMENT("作业状态")
    val status = enum("status", typeRef<JobStatus>()).bindTo { it.status }

    @TINYINT
    @COMMENT("执行时间（小时)")
    val hour = int("hour").bindTo { it.hour }

    @TINYINT
    @COMMENT("执行时间（分钟)")
    val minute = int("minute").bindTo { it.minute }

    @INT
    @COMMENT("执行次数")
    val runCount = int("run_count").bindTo { it.runCount }

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