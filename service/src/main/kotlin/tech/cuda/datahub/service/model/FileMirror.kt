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
import java.time.LocalDateTime

/**
 * 文件镜像，当对文件执行提交操作后，生成一个文件镜像
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
interface FileMirror : Entity<FileMirror> {
    companion object : Entity.Factory<FileMirror>()


    val id: Int


    var fileId: Int

    @TEXT
    @COMMENT("镜像内容")
    var content: String

    @VARCHAR(512)
    @COMMENT("镜像说明")
    var message: String

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