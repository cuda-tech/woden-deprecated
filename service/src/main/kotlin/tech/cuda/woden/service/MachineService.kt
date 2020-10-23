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
package tech.cuda.woden.service

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.asc
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.global.add
import me.liuwj.ktorm.global.global
import tech.cuda.woden.i18n.I18N
import tech.cuda.woden.service.dao.MachineDAO
import tech.cuda.woden.service.dto.MachineDTO
import tech.cuda.woden.service.dto.toMachineDTO
import tech.cuda.woden.service.exception.DuplicateException
import tech.cuda.woden.service.exception.NotFoundException
import tech.cuda.woden.service.po.MachinePO
import tech.cuda.woden.service.po.dtype.MachineRole
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object MachineService : Service(MachineDAO) {

    /**
     * 分页查询服务器信息
     * 如果提供了[pattern]，则对 hostname 进行模糊查询
     * 如果提供了[role], 则返回对应角色的机器
     * 如果提供了[isActive]，则返回对应存活状态的机器
     */
    fun listing(
        page: Int? = null,
        pageSize: Int? = null,
        pattern: String? = null,
        role: MachineRole? = null,
        isActive: Boolean? = null
    ): Pair<List<MachineDTO>, Int> {
        val conditions = mutableListOf(MachineDAO.isRemove eq false)
        role?.let { conditions.add(MachineDAO.role eq role) }
        isActive?.let { conditions.add(MachineDAO.isActive eq isActive) }
        val (machines, count) = batch<MachinePO>(
            pageId = page,
            pageSize = pageSize,
            filter = conditions.reduce { a, b -> a and b },
            like = MachineDAO.hostname.match(pattern),
            orderBy = MachineDAO.id.asc()
        )
        return machines.map { it.toMachineDTO() } to count
    }

    /**
     * 通过[id]查找服务器信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findById(id: Int) = find<MachinePO>(MachineDAO.id eq id and (MachineDAO.isRemove eq false))?.toMachineDTO()

    /**
     * 查找 hostname 为[name]的服务器
     * 如果找不到或已被删除，则返回 null
     */
    fun findByHostname(name: String) = find<MachinePO>(MachineDAO.hostname eq name and (MachineDAO.isRemove eq false))?.toMachineDTO()

    /**
     * 通过[ip]查找服务器信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findByIP(ip: String) = find<MachinePO>(MachineDAO.isRemove eq false and (MachineDAO.ip eq ip))?.toMachineDTO()

    /**
     * 通过[mac]查找服务器信息
     * 如果找不到或已被删除，则返回 null
     */
    fun findByMac(mac: String) = find<MachinePO>(MachineDAO.isRemove eq false and (MachineDAO.mac eq mac))?.toMachineDTO()

    /**
     * 返回一台正在摸鱼的服务器(ie. 内存 & CPU 负载最低的服务器)
     */
    fun findSlackMachine(): MachineDTO {
        val (machines, count) = listingActiveSlave()
        if (count == 0) throw NotFoundException()
        return machines.sortedWith(compareBy({ it.memLoad }, { it.cpuLoad })).first()
    }

    /**
     * 查找当前所有生效的 master
     */
    fun listingActiveMaster() = listing(role = MachineRole.MASTER, isActive = true)

    /**
     * 查询当前多有生效的 slave
     */
    fun listingActiveSlave() = listing(role = MachineRole.SLAVE, isActive = true)


    /**
     * 创建服务器
     * 如果提供的[ip]或[hostname]或[mac]已存在，则抛出 DuplicateException
     * 服务器的 cpu/内存/磁盘 负载由 Tracker 自行获取，因此不需要提供
     */
    fun create(ip: String, hostname: String, mac: String): MachineDTO = Database.global.useTransaction {
        findByIP(ip)?.let { throw DuplicateException(I18N.ipAddress, ip, I18N.existsAlready) }
        findByHostname(hostname)?.let { throw DuplicateException(I18N.hostname, hostname, I18N.existsAlready) }
        findByMac(mac)?.let { throw DuplicateException(I18N.mac, mac, I18N.existsAlready) }
        val machine = MachinePO {
            this.ip = ip
            this.isActive = true
            this.role = MachineRole.SLAVE
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
            this.hostname = hostname
            this.mac = mac
            this.cpuLoad = 0 // 以下字段由 MachineTracker 自动更新
            this.memLoad = 0
            this.diskUsage = 0
        }
        MachineDAO.add(machine)
        return machine.toMachineDTO()
    }

    /**
     * 更新服务器信息
     * 如果给定的服务器[id]不存在或已被删除，则抛出 NotFoundException
     * 如果试图更新[ip], 且[ip]已存在，则抛出 DuplicateException
     * 如果试图更新[hostname], 且[hostname]已存在，则抛出 DuplicateException
     */
    fun update(
        id: Int,
        ip: String? = null,
        hostname: String? = null,
        cpuLoad: Int? = null,
        memLoad: Int? = null,
        diskUsage: Int? = null,
        isActive: Boolean? = null,
        role: MachineRole? = null
    ): MachineDTO = Database.global.useTransaction {
        val machine = find<MachinePO>(MachineDAO.id eq id and (MachineDAO.isRemove eq false))
            ?: throw NotFoundException(I18N.machine, id, I18N.notExistsOrHasBeenRemove)
        ip?.let {
            findByIP(ip)?.let { throw DuplicateException(I18N.ipAddress, ip, I18N.existsAlready) }
            machine.ip = ip
        }
        hostname?.let {
            findByHostname(hostname)?.let { throw DuplicateException(I18N.hostname, hostname, I18N.existsAlready) }
            machine.hostname = hostname
        }
        cpuLoad?.let { machine.cpuLoad = cpuLoad }
        memLoad?.let { machine.memLoad = memLoad }
        diskUsage?.let { machine.diskUsage = diskUsage }
        isActive?.let { machine.isActive = isActive }
        role?.let { machine.role = role }
        anyNotNull(ip, hostname, cpuLoad, memLoad, diskUsage, isActive, role)?.let {
            machine.updateTime = LocalDateTime.now()
            machine.flushChanges()
        }
        return machine.toMachineDTO()
    }

    /**
     * 删除服务器[id]
     * 如果指定的服务器[id]不存在或已被删除，则抛出 NotFoundException
     */
    fun remove(id: Int) = Database.global.useTransaction {
        val machine = find<MachinePO>(MachineDAO.id eq id and (MachineDAO.isRemove eq false))
            ?: throw NotFoundException(I18N.machine, id, I18N.notExistsOrHasBeenRemove)
        machine.isRemove = true
        machine.updateTime = LocalDateTime.now()
        machine.flushChanges()
    }

}