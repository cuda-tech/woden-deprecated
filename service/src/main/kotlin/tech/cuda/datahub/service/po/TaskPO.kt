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
package tech.cuda.datahub.service.po

import me.liuwj.ktorm.entity.Entity
import tech.cuda.datahub.service.po.dtype.ScheduleDependencyInfo
import tech.cuda.datahub.service.po.dtype.ScheduleFormat
import tech.cuda.datahub.service.po.dtype.SchedulePriority
import tech.cuda.datahub.service.po.dtype.SchedulePeriod
import java.time.LocalDateTime

/**
 * 调度任务
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
internal interface TaskPO : Entity<TaskPO> {
    companion object : Entity.Factory<TaskPO>()

    val id: Int
    var mirrorId: Int
    var groupId: Int
    var name: String
    var owners: Set<Int>
    var args: Map<String, Any>
    var isSoftFail: Boolean
    var period: SchedulePeriod
    var format: ScheduleFormat
    var queue: String
    var priority: SchedulePriority
    var pendingTimeout: Int
    var runningTimeout: Int
    var parent: Map<Int, ScheduleDependencyInfo>
    var children: Set<Int>
    var retries: Int
    var retryDelay: Int
    var isValid: Boolean
    var isRemove: Boolean
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}


