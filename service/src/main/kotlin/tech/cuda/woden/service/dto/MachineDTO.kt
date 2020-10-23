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
package tech.cuda.woden.service.dto

import tech.cuda.woden.annotation.pojo.DTO
import tech.cuda.woden.service.po.MachinePO
import tech.cuda.woden.service.po.dtype.MachineRole
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@DTO(MachinePO::class)
data class MachineDTO(
    val id: Int,
    val hostname: String,
    val mac: String,
    val ip: String,
    val cpuLoad: Int,
    val memLoad: Int,
    val diskUsage: Int,
    val role: MachineRole,
    val isActive: Boolean,
    val createTime: LocalDateTime,
    val updateTime: LocalDateTime
)