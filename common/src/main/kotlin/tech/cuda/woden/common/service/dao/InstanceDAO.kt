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
package tech.cuda.woden.common.service.dao

import me.liuwj.ktorm.schema.*
import tech.cuda.woden.annotation.mysql.*
import tech.cuda.woden.common.service.mysql.type.longtext
import tech.cuda.woden.common.service.po.InstancePO
import tech.cuda.woden.common.service.po.dtype.InstanceStatus

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object InstanceDAO : Table<InstancePO>("instances") {

    @BIGINT
    @UNSIGNED
    @AUTO_INCREMENT
    @PRIMARY_KEY
    @COMMENT("实例 ID")
    val id = int("id").primaryKey().bindTo { it.id }

    @BIGINT
    @UNSIGNED
    @COMMENT("作业 ID")
    val jobId = int("job_id").bindTo { it.jobId }

    @VARCHAR(10)
    @COMMENT("实例状态")
    val status = enum("status", typeRef<InstanceStatus>()).bindTo { it.status }

    @LONGTEXT
    @COMMENT("执行日志")
    val log = longtext("log").bindTo { it.log }

    @BOOL
    @COMMENT("逻辑删除")
    val isRemove = boolean("is_remove").bindTo { it.isRemove }

    @DATETIME
    @COMMENT("创建时间")
    val createTime = datetime("create_time").bindTo { it.createTime }

    @DATETIME
    @COMMENT("更新时间")
    val updateTime = datetime("update_time").bindTo { it.updateTime }
}