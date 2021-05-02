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

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.datetime
import me.liuwj.ktorm.schema.int
import tech.cuda.woden.annotation.mysql.*
import tech.cuda.woden.common.service.po.TaskDependencyPO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object TaskDependencyDAO : Table<TaskDependencyPO>("task_dependency") {

    @BIGINT
    @COMMENT("父任务 ID")
    val parentId = int("parent_id").bindTo { it.parentId }

    @BIGINT
    @COMMENT("子任务 ID")
    val childId = int("child_id").bindTo { it.childId }

    @INT
    @COMMENT("弱依赖等待时长")
    val waitTimeout = int("wait_timeout").bindTo { it.waitTimeout }

    @INT
    @COMMENT("偏移天数")
    val offsetDay = int("offset_day").bindTo { it.offsetDay }

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