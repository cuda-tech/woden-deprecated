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
import tech.cuda.woden.common.service.po.WorkingTreePO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@STORE_IN_MYSQL
internal object WorkingTreeDAO : Table<WorkingTreePO>("working_tree") {
    @BIGINT
    @UNSIGNED
    @AUTO_INCREMENT
    @PRIMARY_KEY
    @COMMENT("Working Tree 节点 ID")
    val id = int("id").primaryKey().bindTo { it.id }


    @VARCHAR(256)
    @COMMENT("节点名，有后缀的为叶子节点，无后缀的为非叶子节点(即目录节点)")
    val name = varchar("name").bindTo { it.name }

    @BIGINT
    @UNSIGNED
    @COMMENT("父节点ID")
    val parentId = int("parent_id").bindTo { it.parentId }

    @LONGTEXT
    @COMMENT("当前 stage 区内容")
    val stage = longtext("stage").bindTo { it.stage }

    @BIGINT
    @UNSIGNED
    @COMMENT("当天已发布的 commit ID")
    val commitId = int("commit_id").bindTo { it.commitId }

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