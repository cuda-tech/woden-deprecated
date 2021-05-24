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

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.long
import me.liuwj.ktorm.schema.varchar
import tech.cuda.woden.annotation.mysql.*
import tech.cuda.woden.common.service.po.GlobalLockPO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object GlobalLockDAO : Table<GlobalLockPO>("global_lock") {

    @VARCHAR(256)
    @UNIQUE_INDEX
    @COMMENT("实例 ID")
    val name = varchar("name").bindTo { it.name }

    @VARCHAR(256)
    @COMMENT("详情")
    val message = varchar("message").bindTo { it.message }

    @BIGINT
    @COMMENT("获取锁时的时间戳")
    val lockTime = long("lock_time").bindTo { it.lockTime }

    @BIGINT
    @COMMENT("锁失效的时间戳")
    val expireTime = long("expire_time").bindTo { it.expireTime }
}