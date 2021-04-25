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
import me.liuwj.ktorm.global.add
import me.liuwj.ktorm.global.global
import me.liuwj.ktorm.global.select
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.dao.PersonDAO
import tech.cuda.woden.common.service.dto.PersonDTO
import tech.cuda.woden.common.service.dto.toPersonDTO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.po.PersonPO
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
    fun listing(page: Int, pageSize: Int, pattern: String? = null): Pair<List<PersonDTO>, Int> {
        val (persons, count) = batch<PersonPO>(
            page,
            pageSize,
            exclude = PersonDAO.password,
            filter = PersonDAO.isRemove eq false,
            like = PersonDAO.name.match(pattern),
            orderBy = PersonDAO.id.asc()
        )
        return persons.map { it.toPersonDTO() } to count
    }

    /**
     * 查找用户名为[name]的用户信息
     * 如果不存在或已被删除则返回 null
     */
    fun findByName(name: String) = find<PersonPO>(
        where = (PersonDAO.isRemove eq false) and (PersonDAO.name eq name),
        exclude = PersonDAO.password
    )?.toPersonDTO()

    /**
     * 通过[id]查找用户信息
     * 如果不存在或已被删除则返回 null
     */
    fun findById(id: Int) = find<PersonPO>(
        where = (PersonDAO.isRemove eq false) and (PersonDAO.id eq id),
        exclude = PersonDAO.password
    )?.toPersonDTO()

    /**
     * 创建用户
     * 如果已存在用户名为[name]的用户，则抛出 DuplicateException
     */
    fun create(name: String, password: String, teams: Set<Int>, email: String): PersonDTO =
        Database.global.useTransaction {
            findByName(name)?.let { throw DuplicateException(I18N.person, name, I18N.existsAlready) }
            val person = PersonPO {
                this.name = name
                this.teams = teams
                this.password = Encoder.md5(password)
                this.email = email
                this.isRemove = false
                this.createTime = LocalDateTime.now()
                this.updateTime = LocalDateTime.now()
            }
            PersonDAO.add(person)
            return person.toPersonDTO()
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
        teams: Set<Int>? = null,
        email: String? = null
    ): PersonDTO {
        val person = find<PersonPO>(
            where = (PersonDAO.isRemove eq false) and (PersonDAO.id eq id),
            exclude = PersonDAO.password
        ) ?: throw NotFoundException(I18N.person, id, I18N.notExistsOrHasBeenRemove)
        name?.let {
            findByName(name)?.let { throw DuplicateException(I18N.person, name, I18N.existsAlready) }
            person.name = name
        }
        password?.let { person.password = Encoder.md5(password) }
        teams?.let { person.teams = teams }
        email?.let { person.email = email }
        anyNotNull(name, password, teams, email)?.let {
            person.updateTime = LocalDateTime.now()
            person.flushChanges()
        }
        return person.toPersonDTO()
    }

    /**
     * 删除指定用户[id]
     * 如果指定的用户[id]不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) {
        val person = find<PersonPO>(
            where = (PersonDAO.isRemove eq false) and (PersonDAO.id eq id),
            exclude = PersonDAO.password
        ) ?: throw NotFoundException(I18N.person, id, I18N.notExistsOrHasBeenRemove)
        person.isRemove = true
        person.updateTime = LocalDateTime.now()
        person.flushChanges()
    }
}

