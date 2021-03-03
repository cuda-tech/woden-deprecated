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
import me.liuwj.ktorm.global.add
import me.liuwj.ktorm.global.global
import tech.cuda.woden.common.i18n.I18N
import tech.cuda.woden.common.service.dao.ContainerDAO
import tech.cuda.woden.common.service.dto.ContainerDTO
import tech.cuda.woden.common.service.dto.toContainerDTO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.po.ContainerPO
import tech.cuda.woden.common.service.po.dtype.ContainerRole
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object ContainerService : Service(ContainerDAO) {

    /**
     * 分页查询容器信息
     * 如果提供了[pattern]，则对 hostname 进行模糊查询
     * 如果提供了[role], 则返回对应角色的容器
     * 如果提供了[isActive]，则返回对应存活状态的容器
     */
    fun listing(
        page: Int? = null,
        pageSize: Int? = null,
        pattern: String? = null,
        role: ContainerRole? = null,
        isActive: Boolean? = null
    ): Pair<List<ContainerDTO>, Int> {
        val conditions = mutableListOf(ContainerDAO.isRemove eq false)
        role?.let { conditions.add(ContainerDAO.role eq role) }
        isActive?.let { conditions.add(ContainerDAO.isActive eq isActive) }
        val (containers, count) = batch<ContainerPO>(
            pageId = page,
            pageSize = pageSize,
            filter = conditions.reduce { a, b -> a and b },
            like = ContainerDAO.hostname.match(pattern),
            orderBy = ContainerDAO.id.asc()
        )
        return containers.map { it.toContainerDTO() } to count
    }

    /**
     * 通过[id]查找容器信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findById(id: Int) = find<ContainerPO>(ContainerDAO.id eq id and (ContainerDAO.isRemove eq false))?.toContainerDTO()

    /**
     * 查找 hostname 为[name]的容器
     * 如果找不到或已被删除，则返回 null
     */
    fun findByHostname(name: String) = find<ContainerPO>(ContainerDAO.hostname eq name and (ContainerDAO.isRemove eq false))?.toContainerDTO()

    /**
     * 返回一台正在摸鱼的容器(ie. 内存 & CPU 负载最低的容器)
     */
    fun findSlackContainer(): ContainerDTO {
        val (containers, count) = listingActiveSlave()
        if (count == 0) throw NotFoundException()
        return containers.sortedWith(compareBy({ it.memLoad }, { it.cpuLoad })).first()
    }

    /**
     * 查找当前所有生效的 master
     */
    fun listingActiveMaster() = listing(role = ContainerRole.MASTER, isActive = true)

    /**
     * 查询当前多有生效的 slave
     */
    fun listingActiveSlave() = listing(role = ContainerRole.SLAVE, isActive = true)


    /**
     * 创建容器
     * 如果提供的[hostname]已存在，则抛出 DuplicateException
     * 容器的 cpu/内存/磁盘 负载由 Tracker 自行获取，因此不需要提供
     */
    fun create(hostname: String): ContainerDTO = Database.global.useTransaction {
        findByHostname(hostname)?.let { throw DuplicateException(I18N.hostname, hostname, I18N.existsAlready) }
        val container = ContainerPO {
            this.isActive = true
            this.role = ContainerRole.SLAVE
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
            this.hostname = hostname
            this.cpuLoad = 0 // 以下字段由 ContainerTracker 自动更新
            this.memLoad = 0
            this.diskUsage = 0
        }
        ContainerDAO.add(container)
        return container.toContainerDTO()
    }

    /**
     * 更新容器信息
     * 如果给定的容器[id]不存在或已被删除，则抛出 NotFoundException
     * 如果试图更新[hostname], 且[hostname]已存在，则抛出 DuplicateException
     */
    fun update(
        id: Int,
        hostname: String? = null,
        cpuLoad: Int? = null,
        memLoad: Int? = null,
        diskUsage: Int? = null,
        isActive: Boolean? = null,
        role: ContainerRole? = null
    ): ContainerDTO = Database.global.useTransaction {
        val container = find<ContainerPO>(ContainerDAO.id eq id and (ContainerDAO.isRemove eq false))
            ?: throw NotFoundException(I18N.container, id, I18N.notExistsOrHasBeenRemove)
        hostname?.let {
            findByHostname(hostname)?.let { throw DuplicateException(I18N.hostname, hostname, I18N.existsAlready) }
            container.hostname = hostname
        }
        cpuLoad?.let { container.cpuLoad = cpuLoad }
        memLoad?.let { container.memLoad = memLoad }
        diskUsage?.let { container.diskUsage = diskUsage }
        isActive?.let { container.isActive = isActive }
        role?.let { container.role = role }
        anyNotNull(hostname, cpuLoad, memLoad, diskUsage, isActive, role)?.let {
            container.updateTime = LocalDateTime.now()
            container.flushChanges()
        }
        return container.toContainerDTO()
    }

    /**
     * 删除容器[id]
     * 如果指定的容器[id]不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) = Database.global.useTransaction {
        val container = find<ContainerPO>(ContainerDAO.id eq id and (ContainerDAO.isRemove eq false))
            ?: throw NotFoundException(I18N.container, id, I18N.notExistsOrHasBeenRemove)
        container.isRemove = true
        container.updateTime = LocalDateTime.now()
        container.flushChanges()
    }

}