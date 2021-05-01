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
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.datetime
import me.liuwj.ktorm.schema.int
import tech.cuda.woden.annotation.mysql.*
import tech.cuda.woden.common.service.po.PersonTeamMappingPO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object PersonTeamMappingDAO :Table<PersonTeamMappingPO>("person_team_mapping") {

    @BIGINT
    @COMMENT("用户 ID")
    val personId = int("person_id").bindTo { it.personId }

    @BIGINT
    @COMMENT("项目组 ID")
    val teamId = int("team_id").bindTo { it.teamId }

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