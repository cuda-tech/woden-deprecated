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

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.global.*
import tech.cuda.woden.common.service.dao.GlobalLockDAO
import tech.cuda.woden.common.service.dto.LockDTO
import tech.cuda.woden.common.service.dto.toLockDTO
import tech.cuda.woden.common.service.mysql.function.unixTimestamp
import tech.cuda.woden.common.service.po.GlobalLockPO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object LockService {

    /**
     * 尝试获得名为[name]的锁，如果获取成功，则将其失效时间置为[expire]秒后
     * 如果获取失败，则返回 null
     *
     * p.s 因为 ktorm 不支持 Insert xxx values (xxx, function()) 的方式和 update xxx set column = function()
     * 因此这里直接用 native 的 SQL 进行插入 & 更新
     */
    private fun tryGetLock(name: String, message: String = "", expire: Int = 5): GlobalLockPO? {
        try {
            Database.global.useConnection { conn ->
                conn.prepareStatement(
                    """insert into ${conn.catalog}.${GlobalLockDAO.tableName}
                       values (?, ?, unix_timestamp(), unix_timestamp() + ?)
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, name)
                    statement.setString(2, message)
                    statement.setInt(3, expire)
                    statement.execute()
                }
            }
            return GlobalLockDAO.select()
                .where { GlobalLockDAO.name eq name }
                .map { GlobalLockDAO.createEntity(it) }
                .first()
        } catch (e: MySQLIntegrityConstraintViolationException) {
            val expireLock = GlobalLockDAO.select()
                .where { (GlobalLockDAO.name eq name) and (GlobalLockDAO.expireTime less unixTimestamp()) }
                .map { GlobalLockDAO.createEntity(it) }
                .firstOrNull()
            if (expireLock != null) {
                var updateSuccess: Boolean
                Database.global.useConnection { conn ->
                    conn.prepareStatement(
                        """update ${conn.catalog}.${GlobalLockDAO.tableName}
                            set message = ?,
                                ${GlobalLockDAO.lockTime.name} = unix_timestamp() ,
                                ${GlobalLockDAO.expireTime.name} = unix_timestamp() + ?
                            where name = ? and ${GlobalLockDAO.lockTime.name} = ?
                                and ${GlobalLockDAO.expireTime.name} = ?
                        """.trimIndent()
                    ).use { statement ->
                        statement.setString(1, message)
                        statement.setInt(2, expire)
                        statement.setString(3, name)
                        statement.setLong(4, expireLock.lockTime)
                        statement.setLong(5, expireLock.expireTime)
                        updateSuccess = statement.executeUpdate() == 1
                    }
                }
                if (updateSuccess) {
                    return GlobalLockDAO.select()
                        .where { GlobalLockDAO.name eq name }
                        .map { GlobalLockDAO.createEntity(it) }
                        .first()
                }
            }
        }
        return null
    }

    /**
     * 释放[lock]，并返回是否释放成功
     */
    fun unlock(lock: LockDTO?): Boolean {
        return if (lock == null) {
            return false
        } else {
            GlobalLockDAO.delete { (it.name eq lock.name) and (it.lockTime eq lock.lockTime) } == 1
        }
    }

    /**
     * 尝试获取名为[name]的全局锁，最多尝试 [retryCount] + 1 次
     * 如果获取成功，则返回锁，否则返回 null
     */
    fun lock(
        name: String,
        message: String = "",
        expire: Int = 5,
        retryCount: Int = 0,
        retryDelay: Long = 100
    ): LockDTO? {
        var lock: GlobalLockPO? = null
        for (i in -1 until retryCount) {
            lock = tryGetLock(name, message, expire)
            if (lock != null) {
                break
            }
            Thread.sleep(retryDelay)
        }
        return lock?.toLockDTO()
    }
}