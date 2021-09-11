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

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.global.*
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.dao.PersonDAO
import tech.cuda.woden.common.service.dao.PersonTeamMappingDAO
import tech.cuda.woden.common.service.dto.PersonDTO
import tech.cuda.woden.common.service.dto.TeamDTO
import tech.cuda.woden.common.service.dto.toPersonDTOWith
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.exception.PermissionException
import tech.cuda.woden.common.service.po.PersonPO
import tech.cuda.woden.common.service.po.PersonTeamMappingPO
import tech.cuda.woden.common.utils.Encoder
import java.time.LocalDateTime
import java.util.*

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object PersonService : Service(PersonDAO) {

    private const val EXPIRE_TIME = 86400000L // 默认 token 失效时间为 1 天
    private const val CLAIM = "person_name"

    /**
     * 检查提供的 [name] 和 [password] 是否与数据库中的匹配, 常用于第一次登录
     * 如果匹配，则生成为期 1 天的 token
     * 如果不匹配或生成失败，则返回 null
     */
    fun sign(name: String, password: String): String? {
        val person = find<PersonPO>(where = PersonDAO.name eq name) ?: return null
        if (person.password != Encoder.md5(password)) return null
        return try {
            JWT.create().withClaim(CLAIM, person.name)
                .withExpiresAt(Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(Algorithm.HMAC256(person.password))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从[token]中解析出 person_name 如果解析失败，则返回 null
     */
    private fun getPersonName(token: String) = try {
        JWT.decode(token).getClaim(CLAIM).asString()
    } catch (e: JWTDecodeException) {
        null
    }

    /**
     * 通过 token 获取用户信息，如果 token 不合法，则返回 null
     */
    fun getPersonByToken(token: String): PersonDTO? {
        if (!verify(token)) return null
        val name = getPersonName(token) ?: return null
        return findByName(name)
    }

    /**
     * 判断 token 是否为数据库中某一条记录生成的
     * 如果匹配，则返回 true 否则返回 false
     */
    fun verify(token: String): Boolean {
        val name = getPersonName(token) ?: return false
        val person = PersonDAO.select(PersonDAO.password)
            .where { PersonDAO.isRemove eq false and (PersonDAO.name eq name) }
            .map { PersonDAO.createEntity(it) }
            .firstOrNull() ?: return false
        return try {
            JWT.require(Algorithm.HMAC256(person.password)).withClaim(CLAIM, name).build().verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 分页查询用户列表，支持模糊搜索
     */
    fun listing(page: Int? = null, pageSize: Int? = null, pattern: String? = null): Pair<List<PersonDTO>, Int> {
        val (persons, count) = batch<PersonPO>(
            page,
            pageSize,
            exclude = PersonDAO.password,
            filter = PersonDAO.isRemove eq false,
            like = PersonDAO.name.match(pattern),
            orderBy = PersonDAO.id.asc()
        )
        val teamsMapping = findTeamsByPersonIds(persons.map { it.id })
        return persons.map { it.toPersonDTOWith(teamsMapping.getOrDefault(it.id, setOf())) } to count
    }

    /**
     * 查找用户名为[name]的用户信息
     * 如果不存在或已被删除则返回 null
     */
    fun findByName(name: String): PersonDTO? {
        val person = find<PersonPO>(
            where = (PersonDAO.isRemove eq false) and (PersonDAO.name eq name),
            exclude = PersonDAO.password
        ) ?: return null
        return person.toPersonDTOWith(teams = findTeamByPersonId(personId = person.id))
    }

    /**
     * 通过[id]查找用户信息
     * 如果不存在或已被删除则返回 null
     */
    fun findById(id: Int): PersonDTO? {
        val person = find<PersonPO>(
            where = (PersonDAO.isRemove eq false) and (PersonDAO.id eq id),
            exclude = PersonDAO.password
        ) ?: return null
        return person.toPersonDTOWith(teams = findTeamByPersonId(personId = person.id))
    }

    /**
     * 检查 [personIds] 对应的用户是否存在，如果不存在，则抛出 NotFoundException
     */
    fun requireExists(personIds: List<Int>): PersonService {
        val notExists = PersonDAO.select(PersonDAO.id)
            .where { (PersonDAO.isRemove eq false) and (PersonDAO.id.inList(personIds.toSet().toList()) eq true) }
            .totalRecords != personIds.toSet().size
        if (notExists) {
            throw NotFoundException(I18N.person, personIds.joinToString(","), I18N.notExistsOrHasBeenRemove)
        } else {
            return this
        }
    }

    /**
     * 检查 [personId] 对应的用户是否存在，如果不存在，则抛出 NotFoundException
     */
    fun requireExists(personId: Int) = requireExists(listOf(personId))

    /**
     * 检查用户 [personId] 是否归属于项目组 [teamIds]
     * 如果不归属，则抛出 PermissionException
     */
    fun requireTeamMember(personId: Int, teamIds: List<Int>): PersonService {
        if (teamIds.isEmpty()) {
            return this
        }
        val notMember = PersonTeamMappingDAO.select(PersonTeamMappingDAO.personId)
            .where {
                (PersonTeamMappingDAO.personId eq personId) and
                    (PersonTeamMappingDAO.teamId.inList(teamIds) eq true) and
                    (PersonTeamMappingDAO.isRemove eq false)
            }.totalRecords != teamIds.size
        if (notMember) {
            throw PermissionException(I18N.person, personId, I18N.notBelongTo, I18N.team, teamIds.joinToString(","))
        } else {
            return this
        }
    }

    /**
     * 检查用户 [personId] 是否归属于项目组 [teamId]
     * 如果不归属，则抛出 PermissionException
     */
    fun requireTeamMember(personId: Int, teamId: Int) = requireTeamMember(personId, listOf(teamId))


    /**
     * 通过用户[personIds]查找各自的 teams
     */
    private fun findTeamsByPersonIds(personIds: List<Int>): Map<Int, Set<TeamDTO>> {
        if (personIds.isEmpty()) {
            return mapOf()
        }
        val mapping = PersonTeamMappingDAO.select()
            .where { (PersonTeamMappingDAO.isRemove eq false) and (PersonTeamMappingDAO.personId.inList(personIds) eq true) }
            .map { PersonTeamMappingDAO.createEntity(it) }
        val teams = TeamService.listing(
            page = 1,
            pageSize = mapping.size,
            ids = mapping.map { it.teamId }.toSet().toList()
        ).first.groupBy { it.id }
            .mapValues { it.value.first() }
        return mapping.groupBy { it.personId }
            .mapValues {
                it.value.map { t ->
                    teams[t.teamId] ?: throw NotFoundException(
                        I18N.team,
                        t.teamId,
                        I18N.notExistsOrHasBeenRemove
                    )
                }.toSet()
            }
    }

    private fun findTeamByPersonId(personId: Int): Set<TeamDTO> =
        findTeamsByPersonIds(listOf(personId)).getOrDefault(personId, setOf())


    /**
     * 创建用户
     * 如果已存在用户名为[name]的用户，则抛出 DuplicateException
     */
    fun create(name: String, password: String, teamIds: Set<Int>, email: String): PersonDTO =
        Database.global.useTransaction {
            findByName(name)?.let { throw DuplicateException(I18N.person, name, I18N.existsAlready) }
            val teams = TeamService.checkTeamsAllExistsAndReturn(teamIds = teamIds)
            val now = LocalDateTime.now()
            val person = PersonPO {
                this.name = name
                this.password = Encoder.md5(password)
                this.email = email
                this.isRemove = false
                this.createTime = now
                this.updateTime = now
            }
            PersonDAO.addEntity(person)
            teamIds.forEach {
                PersonTeamMappingDAO.addEntity(
                    PersonTeamMappingPO {
                        personId = person.id
                        teamId = it
                        isRemove = false
                        createTime = now
                        updateTime = now
                    }
                )
            }
            return person.toPersonDTOWith(teams.toSet())
        }

    /**
     * 更新用户信息
     * 如果给定的用户[id]不存在或已被删除，则抛出 NotFoundException
     * 如果试图更新[name], 且已存在用户名为[name]的用户，则抛出 DuplicateException
     */
    fun update(
        id: Int,
        name: String? = null,
        password: String? = null,
        teamIds: Set<Int>? = null,
        email: String? = null
    ): PersonDTO = Database.global.useTransaction {
        val person = find<PersonPO>(
            where = (PersonDAO.isRemove eq false) and (PersonDAO.id eq id),
            exclude = PersonDAO.password
        ) ?: throw NotFoundException(I18N.person, id, I18N.notExistsOrHasBeenRemove)
        name?.let {
            findByName(name)?.let { throw DuplicateException(I18N.person, name, I18N.existsAlready) }
            person.name = name
        }
        password?.let { person.password = Encoder.md5(password) }
        val now = LocalDateTime.now()
        val teams = teamIds?.let {
            val currentTeams = findTeamByPersonId(person.id)
            val targetTeams = TeamService.checkTeamsAllExistsAndReturn(teamIds)
            val toBeRemove = currentTeams.map { it.id } - targetTeams.map { it.id }
            val toBeInsert = targetTeams.map { it.id } - currentTeams.map { it.id }
            if (toBeRemove.isNotEmpty()) {
                PersonTeamMappingDAO.update {
                    set(it.isRemove, true)
                    set(it.updateTime, now)
                    where {
                        (it.personId eq person.id) and (it.teamId.inList(toBeRemove) eq true)
                    }
                }
            }
            toBeInsert.forEach {
                PersonTeamMappingDAO.addEntity(
                    PersonTeamMappingPO {
                        personId = person.id
                        teamId = it
                        isRemove = false
                        createTime = now
                        updateTime = now
                    }
                )
            }
            targetTeams.toSet()
        } ?: findTeamByPersonId(person.id)
        email?.let { person.email = email }
        anyNotNull(name, password, email)?.let {
            person.updateTime = LocalDateTime.now()
            person.flushChanges()
        }
        return person.toPersonDTOWith(teams = teams)
    }

    /**
     * 删除指定用户[id]
     * 如果指定的用户[id]不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) = Database.global.useTransaction {
        val person = find<PersonPO>(
            where = (PersonDAO.isRemove eq false) and (PersonDAO.id eq id),
            exclude = PersonDAO.password
        ) ?: throw NotFoundException(I18N.person, id, I18N.notExistsOrHasBeenRemove)
        val now = LocalDateTime.now()
        PersonTeamMappingDAO.update {
            set(it.isRemove, true)
            set(it.updateTime, now)
            where { it.personId eq person.id }
        }
        person.isRemove = true
        person.updateTime = now
        person.flushChanges()
    }
}

