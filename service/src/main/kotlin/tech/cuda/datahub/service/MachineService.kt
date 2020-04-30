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
import tech.cuda.datahub.service.dao.MachineDAO
import tech.cuda.datahub.service.dto.MachineDTO
import tech.cuda.datahub.service.dto.toMachineDTO
import tech.cuda.datahub.service.exception.DuplicateException
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.po.MachinePO
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object MachineService : Service(MachineDAO) {

    fun listing(page: Int, pageSize: Int, pattern: String? = null): Pair<List<MachineDTO>, Int> {
        val (machines, count) = batch<MachinePO>(
            pageId = page,
            pageSize = pageSize,
            filter = MachineDAO.isRemove eq false,
            like = MachineDAO.hostname.match(pattern),
            orderBy = MachineDAO.id.asc()
        )
        return machines.map { it.toMachineDTO() } to count
    }

    fun findById(id: Int) = find<MachinePO>(MachineDAO.id eq id and (MachineDAO.isRemove eq false))?.toMachineDTO()

    fun findByHostname(name: String) = find<MachinePO>(MachineDAO.hostname eq name and (MachineDAO.isRemove eq false))?.toMachineDTO()

    fun findByIP(ip: String) = find<MachinePO>(MachineDAO.isRemove eq false and (MachineDAO.ip eq ip))?.toMachineDTO()

    fun create(ip: String): MachineDTO {
        findByIP(ip)?.let { throw DuplicateException("服务器地址 $ip 已存在") }
        val machine = MachinePO {
            this.ip = ip
            this.isRemove = false
            this.createTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
            this.hostname = "" // 以下字段由 MachineTracker 自动更新
            this.mac = ""
            this.cpuLoad = 0
            this.memLoad = 0
            this.diskUsage = 0
        }
        MachineDAO.add(machine)
        return machine.toMachineDTO()
    }

    fun update(
        id: Int,
        ip: String? = null,
        hostname: String? = null,
        mac: String? = null,
        cpuLoad: Int? = null,
        memLoad: Int? = null,
        diskUsage: Int? = null
    ): MachineDTO {
        val machine = find<MachinePO>(MachineDAO.id eq id and (MachineDAO.isRemove eq false))
            ?: throw NotFoundException("服务器 $id 不存在或已被删除")
        ip?.let {
            findByIP(ip)?.let { throw DuplicateException("服务器地址 $ip 已存在") }
            machine.ip = ip
        }
        hostname?.let { machine.hostname = hostname }
        mac?.let { machine.mac = mac }
        cpuLoad?.let { machine.cpuLoad = cpuLoad }
        memLoad?.let { machine.memLoad = memLoad }
        diskUsage?.let { machine.diskUsage = diskUsage }
        anyNotNull(ip, hostname, mac, cpuLoad, memLoad, diskUsage)?.let {
            machine.updateTime = LocalDateTime.now()
            machine.flushChanges()
        }
        return machine.toMachineDTO()
    }

    fun remove(id: Int) {
        val machine = find<MachinePO>(MachineDAO.id eq id and (MachineDAO.isRemove eq false))
            ?: throw NotFoundException("服务器 $id 不存在或已被删除")
        machine.isRemove = true
        machine.updateTime = LocalDateTime.now()
        machine.flushChanges()
    }

}