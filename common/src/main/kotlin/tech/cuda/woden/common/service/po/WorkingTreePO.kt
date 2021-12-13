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
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
interface WorkingTreePO : Entity<WorkingTreePO> {
    companion object : Entity.Factory<WorkingTreePO>()

    val id: Int
    var name: String
    var parentId: Int?
    var stage: String?
    var commitId: Int?
    var isRemove: Boolean
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}