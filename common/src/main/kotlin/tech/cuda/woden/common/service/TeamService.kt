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
package tech.cuda.woden.common.service

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.asc
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.inList
import me.liuwj.ktorm.global.addEntity
import me.liuwj.ktorm.global.global
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.dao.TeamDAO
import tech.cuda.woden.common.service.dto.TeamDTO
import tech.cuda.woden.common.service.dto.toTeamDTO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.po.TeamPO
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object TeamService : Service(TeamDAO) {

    /**
     * 分页查询项目组信息
     * 如果提供了[pattern]，则进行模糊查询
     */
    fun listing(page: Int, pageSize: Int, pattern: String? = null, ids: List<Int>? = null): Pair<List<TeamDTO>, Int> {
        val conditions = mutableListOf(TeamDAO.isRemove eq false)
        if (ids != null && ids.isNotEmpty()) {
            conditions.add(TeamDAO.id.inList(ids) eq true)
        }
        val (teams, count) = batch<TeamPO>(
            pageId = page,
            pageSize = pageSize,
            filter = conditions.reduce { a, b -> a and b },
            like = TeamDAO.name.match(pattern),
            orderBy = TeamDAO.id.asc()
        )
        return teams.map { it.toTeamDTO() } to count
    }

    /**
     * 通过[id]查找项目组信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findById(id: Int) = find<TeamPO>(where = (TeamDAO.isRemove eq false) and (TeamDAO.id eq id))?.toTeamDTO()

    /**
     * 通过[name]查找项目组信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findByName(name: String) =
        find<TeamPO>(where = (TeamDAO.isRemove eq false) and (TeamDAO.name eq name))?.toTeamDTO()

    /**
     * 通过文件路径查找项目组
     * 如果找不到或已被删除，则返回 null
     */
    fun findByFilePath(path: String) = findByName(path.trimStart('/').split('/').first())

    /**
     * 创建名称为[name]项目组
     * 如果已存在名称为[name]的项目组，则抛出 DuplicateException
     */
    fun create(name: String): TeamDTO = Database.global.useTransaction {
        find<TeamPO>(where = (TeamDAO.isRemove eq false) and (TeamDAO.name eq name))?.let {
            throw DuplicateException(I18N.team, it.name, I18N.existsAlready)
        }
        val team = TeamPO {
            this.name = name
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
        }
        TeamDAO.addEntity(team)
        return team.toTeamDTO()
    }

    /**
     * 更新项目组[id]信息
     * 如果给定的项目组[id]不存在或已被删除，则抛出 NotFoundException
     * 如果试图更新[name]，且已存在名称为[name]的项目组，则抛出 DuplicateException
     */
    fun update(id: Int, name: String? = null): TeamDTO = Database.global.useTransaction {
        val team = find<TeamPO>(where = (TeamDAO.isRemove eq false) and (TeamDAO.id eq id))
            ?: throw NotFoundException(I18N.team, id, I18N.notExistsOrHasBeenRemove)
        name?.let {
            findByName(name)?.let { throw DuplicateException(I18N.team, name, I18N.existsAlready) }
            team.name = name
        }
        anyNotNull(name)?.let {
            team.updateTime = LocalDateTime.now()
            team.flushChanges()
        }
        return team.toTeamDTO()
    }

    /**
     * 删除项目组[id]
     * 如果指定的项目组[id]不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) = Database.global.useTransaction {
        val team = find<TeamPO>(where = (TeamDAO.isRemove eq false) and (TeamDAO.id eq id))
            ?: throw NotFoundException(I18N.team, id, I18N.notExistsOrHasBeenRemove)
        team.isRemove = true
        team.updateTime = LocalDateTime.now()
        team.flushChanges()
    }

    /**
     * 检查[teamIds]对应的项目组是否都存在，如果存在则返回，否则抛出 NotFoundException
     */
    fun checkTeamsAllExistsAndReturn(teamIds: Set<Int>): List<TeamDTO> {
        val teams = listing(page = 1, pageSize = teamIds.size, ids = teamIds.toList()).first
        if (teamIds.size != teams.size) {
            val exists = teams.map { it.id }.toSet()
            val missing = teamIds.filter { !exists.contains(it) }.joinToString(",")
            throw NotFoundException(I18N.team, missing, I18N.notExistsOrHasBeenRemove)
        }
        return teams
    }

}