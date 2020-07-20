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
import tech.cuda.datahub.service.po.dtype.MachineRole
import java.time.LocalDateTime

/**
 * 调度服务器
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
internal interface MachinePO : Entity<MachinePO> {
    companion object : Entity.Factory<MachinePO>()

    val id: Int
    var hostname: String
    var mac: String
    var ip: String
    var cpuLoad: Int
    var memLoad: Int
    var diskUsage: Int
    var role: MachineRole
    var isActive: Boolean
    var isRemove: Boolean
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}


