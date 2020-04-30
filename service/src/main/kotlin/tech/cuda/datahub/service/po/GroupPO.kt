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
import java.time.LocalDateTime

/**
 * 项目组，一个用户可以归属多个项目组，一个文件只能归属一个项目组
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
internal interface GroupPO : Entity<GroupPO> {
    companion object : Entity.Factory<GroupPO>()

    val id: Int
    var name: String
    var isRemove: Boolean
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}



