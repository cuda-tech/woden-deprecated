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

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.global.add
import me.liuwj.ktorm.global.global
import me.liuwj.ktorm.global.select
import tech.cuda.datahub.i18n.I18N
import tech.cuda.datahub.service.dao.UserDAO
import tech.cuda.datahub.service.dto.UserDTO
import tech.cuda.datahub.service.dto.toUserDTO
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.po.UserPO
import tech.cuda.datahub.service.utils.Encoder
import java.time.LocalDateTime
import java.util.*

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object UserService : Service(UserDAO) {

    private const val EXPIRE_TIME = 86400000L // 默认 token 失效时间为 1 天
    private const val CLAIM = "username"

    /**
     * 检查提供的 [username] 和 [password] 是否与数据库中的匹配, 常用于第一次登录
     * 如果匹配，则生成为期 1 天的 token
     * 如果不匹配或生成失败，则返回 null
     */
    fun sign(username: String, password: String): String? {
        val user = find<UserPO>(where = UserDAO.name eq username) ?: return null
        if (user.password != Encoder.md5(password)) return null
        return try {
            JWT.create().withClaim(CLAIM, user.name)
                .withExpiresAt(Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(Algorithm.HMAC256(user.password))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从[token]中解析出 username，如果解析失败，则返回 null
     */
    private fun getUsername(token: String) = try {
        JWT.decode(token).getClaim(CLAIM).asString()
    } catch (e: JWTDecodeException) {
        null
    }

    /**
     * 通过 token 获取用户信息，如果 token 不合法，则返回 null
     */
    fun getUserByToken(token: String): UserDTO? {
        if (!verify(token)) return null
        val username = getUsername(token) ?: return null
        return findByName(username)
    }

    /**
     * 判断 token 是否为数据库中某一条记录生成的
     * 如果匹配，则返回 true 否则返回 false
     */
    fun verify(token: String): Boolean {
        val username = getUsername(token) ?: return false
        val user = UserDAO.select(UserDAO.password)
            .where { UserDAO.isRemove eq false and (UserDAO.name eq username) }
            .map { UserDAO.createEntity(it) }
            .firstOrNull() ?: return false
        return try {
            JWT.require(Algorithm.HMAC256(user.password)).withClaim(CLAIM, username).build().verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 分页查询用户列表，支持模糊搜索
     */
    fun listing(page: Int, pageSize: Int, pattern: String? = null): Pair<List<UserDTO>, Int> {
        val (users, count) = batch<UserPO>(
            page,
            pageSize,
            exclude = UserDAO.password,
            filter = UserDAO.isRemove eq false,
            like = UserDAO.name.match(pattern),
            orderBy = UserDAO.id.asc()
        )
        return users.map { it.toUserDTO() } to count
    }

    /**
     * 查找用户名为[name]的用户信息
     * 如果不存在或已被删除则返回 null
     */
    fun findByName(name: String) = find<UserPO>(
        where = (UserDAO.isRemove eq false) and (UserDAO.name eq name),
        exclude = UserDAO.password
    )?.toUserDTO()

    /**
     * 通过[id]查找用户信息
     * 如果不存在或已被删除则返回 null
     */
    fun findById(id: Int) = find<UserPO>(
        where = (UserDAO.isRemove eq false) and (UserDAO.id eq id),
        exclude = UserDAO.password
    )?.toUserDTO()

    /**
     * 创建用户
     * 如果已存在用户名为[name]的用户，则抛出 DuplicateException
     */
    fun create(name: String, password: String, groups: Set<Int>, email: String): UserDTO = Database.global.useTransaction {
        findByName(name)?.let { throw DuplicateException(I18N.user, name, I18N.existsAlready) }
        val user = UserPO {
            this.name = name
            this.groups = groups
            this.password = Encoder.md5(password)
            this.email = email
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
        }
        UserDAO.add(user)
        return user.toUserDTO()
    }

    /**
     * 更新用户信息
     * 如果给定的用户[id]不存在或已被删除，则抛出 NotFoundException
     * 如果试图更新[name], 且已存在用户名为[name]的用户，则抛出 DuplicateException
     */
    fun update(id: Int, name: String? = null, password: String? = null, groups: Set<Int>? = null, email: String? = null): UserDTO {
        val user = find<UserPO>(
            where = (UserDAO.isRemove eq false) and (UserDAO.id eq id),
            exclude = UserDAO.password
        ) ?: throw NotFoundException(I18N.user, id, I18N.notExistsOrHasBeenRemove)
        name?.let {
            findByName(name)?.let { throw DuplicateException(I18N.user, name, I18N.existsAlready) }
            user.name = name
        }
        password?.let { user.password = Encoder.md5(password) }
        groups?.let { user.groups = groups }
        email?.let { user.email = email }
        anyNotNull(name, password, groups, email)?.let {
            user.updateTime = LocalDateTime.now()
            user.flushChanges()
        }
        return user.toUserDTO()
    }

    /**
     * 删除指定用户[id]
     * 如果指定的用户[id]不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) {
        val user = find<UserPO>(
            where = (UserDAO.isRemove eq false) and (UserDAO.id eq id),
            exclude = UserDAO.password
        ) ?: throw NotFoundException(I18N.user, id, I18N.notExistsOrHasBeenRemove)
        user.isRemove = true
        user.updateTime = LocalDateTime.now()
        user.flushChanges()
    }
}

