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

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.asc
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.add
import tech.cuda.datahub.i18n.I18N
import tech.cuda.datahub.service.dao.GroupDAO
import tech.cuda.datahub.service.dto.GroupDTO
import tech.cuda.datahub.service.dto.toGroupDTO
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.po.GroupPO
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object GroupService : Service(GroupDAO) {

    /**
     * 分页查询项目组信息
     * 如果提供了[pattern]，则进行模糊查询
     */
    fun listing(page: Int, pageSize: Int, pattern: String? = null): Pair<List<GroupDTO>, Int> {
        val (groups, count) = batch<GroupPO>(
            pageId = page,
            pageSize = pageSize,
            filter = GroupDAO.isRemove eq false,
            like = GroupDAO.name.match(pattern),
            orderBy = GroupDAO.id.asc()
        )
        return groups.map { it.toGroupDTO() } to count
    }

    /**
     * 通过[id]查找项目组信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findById(id: Int) = find<GroupPO>(where = (GroupDAO.isRemove eq false) and (GroupDAO.id eq id))?.toGroupDTO()

    /**
     * 通过[name]查找项目组信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findByName(name: String) = find<GroupPO>(where = (GroupDAO.isRemove eq false) and (GroupDAO.name eq name))?.toGroupDTO()

    /**
     * 创建名称为[name]项目组
     * 如果已存在名称为[name]的项目组，则抛出 DuplicateException
     */
    fun create(name: String): GroupDTO = Database.global.useTransaction {
        find<GroupPO>(where = (GroupDAO.isRemove eq false) and (GroupDAO.name eq name))?.let {
            throw DuplicateException(I18N.group, it.name, I18N.existsAlready)
        }
        val group = GroupPO {
            this.name = name
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
        }
        GroupDAO.add(group)
        return group.toGroupDTO()
    }

    /**
     * 更新项目组[id]信息
     * 如果给定的项目组[id]不存在或已被删除，则抛出 NotFoundException
     * 如果试图更新[name]，且已存在名称为[name]的项目组，则抛出 DuplicateException
     */
    fun update(id: Int, name: String? = null): GroupDTO = Database.global.useTransaction {
        val group = find<GroupPO>(where = (GroupDAO.isRemove eq false) and (GroupDAO.id eq id))
            ?: throw NotFoundException(I18N.group, id, I18N.notExistsOrHasBeenRemove)
        name?.let {
            findByName(name)?.let { throw DuplicateException(I18N.group, name, I18N.existsAlready) }
            group.name = name
        }
        anyNotNull(name)?.let {
            group.updateTime = LocalDateTime.now()
            group.flushChanges()
        }
        return group.toGroupDTO()
    }

    /**
     * 删除项目组[id]
     * 如果指定的项目组[id]不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) = Database.global.useTransaction {
        val group = find<GroupPO>(where = (GroupDAO.isRemove eq false) and (GroupDAO.id eq id))
            ?: throw NotFoundException(I18N.group, id, I18N.notExistsOrHasBeenRemove)
        group.isRemove = true
        group.updateTime = LocalDateTime.now()
        group.flushChanges()
    }

}