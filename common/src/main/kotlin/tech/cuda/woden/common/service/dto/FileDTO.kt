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
import tech.cuda.woden.common.service.po.FilePO
import tech.cuda.woden.common.service.po.dtype.FileType
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@DTO(FilePO::class)
data class FileDTO(
    val id: Int,
    val teamId: Int,
    val ownerId: Int,
    val name: String,
    val type: FileType,
    val parentId: Int?,
    val createTime: LocalDateTime,
    val updateTime: LocalDateTime
)
