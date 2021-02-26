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
package tech.cuda.woden.common.service.dto

import tech.cuda.woden.annotation.pojo.DTO
import tech.cuda.woden.common.service.po.TaskPO
import tech.cuda.woden.common.service.po.dtype.ScheduleDependencyInfo
import tech.cuda.woden.common.service.po.dtype.ScheduleFormat
import tech.cuda.woden.common.service.po.dtype.SchedulePeriod
import tech.cuda.woden.common.service.po.dtype.SchedulePriority
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@DTO(TaskPO::class)
data class TaskDTO(
    val id: Int,
    val mirrorId: Int,
    val groupId: Int,
    val name: String,
    val owners: Set<Int>,
    val args: Map<String, Any>,
    val isSoftFail: Boolean,
    val period: SchedulePeriod,
    val format: ScheduleFormat,
    val queue: String,
    val priority: SchedulePriority,
    val pendingTimeout: Int,
    val runningTimeout: Int,
    val parent: Map<Int, ScheduleDependencyInfo>,
    val children: Set<Int>,
    val retries: Int,
    val retryDelay: Int,
    val isValid: Boolean,
    val createTime: LocalDateTime,
    val updateTime: LocalDateTime
)