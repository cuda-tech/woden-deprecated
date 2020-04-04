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
package tech.cuda.datahub.service.model

import me.liuwj.ktorm.entity.Entity
import tech.cuda.datahub.annotation.mysql.*
import tech.cuda.datahub.service.model.dtype.SchedulePeriod
import java.time.LocalDateTime

/**
 * 调度任务
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@STORE_IN_MYSQL
interface Task : Entity<Task> {
    companion object : Entity.Factory<Task>()

    @BIGINT
    @COMMENT("任务 ID")
    @PRIMARY_KEY
    @AUTO_INCREMENT
    val id: Int

    @BIGINT
    @COMMENT("镜像 ID")
    var mirrorId: Int

    @VARCHAR(512)
    @COMMENT("任务名")
    var name: String

    @JSON
    @COMMENT("负责人")
    var owners: Set<Int>

    @TEXT
    @COMMENT("执行参数")
    var args: String

    @BOOL
    @COMMENT("执行失败是否跳过")
    var softFail: Boolean

    @VARCHAR(10)
    @COMMENT("调度周期")
    var period: SchedulePeriod

    @VARCHAR(32)
    @COMMENT("执行队列")
    var queue: String

    @SMALLINT
    @COMMENT("优先级")
    var priority: Int

    @INT
    @COMMENT("最大等待时间（分钟）")
    var pendingTimeout: Int

    @INT
    @COMMENT("最大执行时间（分钟）")
    var runningTimeout: Int

    @JSON
    @COMMENT("父任务列表")
    var parent: Map<Int, Map<String, String>>

    @JSON
    @COMMENT("子任务列表")
    var children: Map<Int, Map<String, String>>

    @SMALLINT
    @COMMENT("重试次数")
    var retries: Int

    @INT
    @COMMENT("重试间隔")
    var retryDelay: Int

    @BOOL
    @COMMENT("调度是否生效")
    var valid: Boolean

    @BOOL
    @COMMENT("逻辑删除")
    var isRemove: Boolean

    @DATETIME
    @COMMENT("创建时间")
    var createTime: LocalDateTime

    @DATETIME
    @COMMENT("更新时间")
    var updateTime: LocalDateTime
}


