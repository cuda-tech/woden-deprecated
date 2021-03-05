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

import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.*
import tech.cuda.woden.annotation.mysql.*
import tech.cuda.woden.common.service.po.UserPO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object UserDAO : Table<UserPO>("users") {

    @BIGINT
    @COMMENT("用户 ID")
    @PRIMARY_KEY
    @AUTO_INCREMENT
    val id = int("id").primaryKey().bindTo { it.id }

    @JSON
    @COMMENT("归属项目组 ID 列表")
    val teams = json("teams", typeRef<Set<Int>>()).bindTo { it.teams }

    @VARCHAR(256)
    @COMMENT("用户名")
    val name = varchar("name").bindTo { it.name }

    @VARCHAR(256)
    @COMMENT("用户邮箱")
    val email = varchar("email").bindTo { it.email }

    @VARCHAR(256)
    @COMMENT("登录密码")
    val password = varchar("password").bindTo { it.password }

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
