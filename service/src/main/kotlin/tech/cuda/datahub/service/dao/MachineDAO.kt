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
package tech.cuda.datahub.service.dao

import tech.cuda.datahub.service.po.MachinePO
import me.liuwj.ktorm.schema.*
import tech.cuda.datahub.annotation.mysql.*

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@STORE_IN_MYSQL
internal object MachineDAO : Table<MachinePO>("machines") {
    @BIGINT
    @UNSIGNED
    @AUTO_INCREMENT
    @PRIMARY_KEY
    @COMMENT("服务器 ID")
    val id by int("id").primaryKey().bindTo { it.id }

    @VARCHAR(128)
    @COMMENT("服务器名称")
    val hostname by varchar("hostname").bindTo { it.hostname }

    @VARCHAR(17)
    @COMMENT("服务器 MAC 地址")
    val mac by varchar("mac").bindTo { it.mac }


    @VARCHAR(15)
    @COMMENT("服务器 IP 地址")
    val ip by varchar("ip").bindTo { it.ip }

    @TINYINT(4)
    @COMMENT("CPU 负载")
    val cpuLoad by int("cpu_load").bindTo { it.cpuLoad }

    @TINYINT(4)
    @COMMENT("内存负载")
    val memLoad by int("mem_load").bindTo { it.memLoad }

    @TINYINT(4)
    @COMMENT("磁盘负载")
    val diskUsage by int("disk_usage").bindTo { it.diskUsage }

    @BOOL
    @COMMENT("逻辑删除")
    val isRemove by boolean("is_remove").bindTo { it.isRemove }

    @DATETIME
    @COMMENT("创建时间")
    val createTime by datetime("create_time").bindTo { it.createTime }

    @DATETIME
    @COMMENT("更新时间")
    val updateTime by datetime("update_time").bindTo { it.updateTime }
}
