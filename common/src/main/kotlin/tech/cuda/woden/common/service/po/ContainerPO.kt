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
package tech.cuda.woden.common.service.po

import me.liuwj.ktorm.entity.Entity
import tech.cuda.woden.common.service.po.dtype.ContainerRole
import java.time.LocalDateTime

/**
 * 调度容器
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
internal interface ContainerPO : Entity<ContainerPO> {
    companion object : Entity.Factory<ContainerPO>()

    val id: Int
    var hostname: String
    var cpuLoad: Int
    var memLoad: Int
    var diskUsage: Int
    var role: ContainerRole
    var isActive: Boolean
    var isRemove: Boolean
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}


