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
import tech.cuda.woden.common.service.po.ContainerPO
import tech.cuda.woden.common.service.po.dtype.ContainerRole

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object ContainerDAO : Table<ContainerPO>("containers") {
    @BIGINT
    @UNSIGNED
    @AUTO_INCREMENT
    @PRIMARY_KEY
    @COMMENT("容器 ID")
    val id = int("id").primaryKey().bindTo { it.id }

    @VARCHAR(128)
    @COMMENT("容器名称")
    val hostname = varchar("hostname").bindTo { it.hostname }

    @TINYINT(4)
    @COMMENT("CPU 负载")
    val cpuLoad = int("cpu_load").bindTo { it.cpuLoad }

    @TINYINT(4)
    @COMMENT("内存负载")
    val memLoad = int("mem_load").bindTo { it.memLoad }

    @TINYINT(4)
    @COMMENT("磁盘负载")
    val diskUsage = int("disk_usage").bindTo { it.diskUsage }

    @VARCHAR(8)
    @COMMENT("容器角色: MASTER/SLAVE")
    val role = enum("role", typeRef<ContainerRole>()).bindTo { it.role }

    @BOOL
    @COMMENT("容器是否存活")
    val isActive = boolean("is_active").bindTo { it.isActive }

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
