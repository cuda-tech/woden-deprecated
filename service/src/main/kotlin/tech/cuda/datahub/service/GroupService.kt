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
package tech.cuda.datahub.service

import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.asc
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.add
import tech.cuda.datahub.service.dao.Groups
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.model.Group
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object GroupService : Service(Groups) {

    fun listing(page: Int, pageSize: Int, pattern: String? = null) = batch<Group>(
        pageId = page,
        pageSize = pageSize,
        filter = Groups.isRemove eq false,
        like = Groups.name.match(pattern),
        orderBy = Groups.id.asc()
    )

    fun findById(id: Int) = find<Group>(where = (Groups.isRemove eq false) and (Groups.id eq id))

    fun findByName(name: String) = find<Group>(where = (Groups.isRemove eq false) and (Groups.name eq name))

    fun create(name: String): Group {
        findByName(name)?.let { throw DuplicateException("项目组 $name 已存在") }
        val group = Group {
            this.name = name
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
        }
        Groups.add(group)
        return group
    }

    fun update(id: Int, name: String? = null): Group {
        val group = findById(id) ?: throw NotFoundException("项目组 $id 不存在或已被删除")
        name?.let {
            findByName(name)?.let { throw DuplicateException("项目组 $name 已存在") }
            group.name = name
        }
        anyNotNull(name)?.let {
            group.updateTime = LocalDateTime.now()
            group.flushChanges()
        }
        return group
    }

    fun remove(id: Int) {
        val group = findById(id) ?: throw NotFoundException("项目组 $id 不存在或已被删除")
        group.isRemove = true
        group.updateTime = LocalDateTime.now()
        group.flushChanges()
    }

}