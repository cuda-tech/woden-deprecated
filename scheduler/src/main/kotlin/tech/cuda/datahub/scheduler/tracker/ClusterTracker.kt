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
package tech.cuda.datahub.scheduler.tracker

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.global.global
import tech.cuda.datahub.service.MachineService
import tech.cuda.datahub.service.dto.MachineDTO
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.po.dtype.MachineRole
import java.time.LocalDateTime

/**
 * 集群状态 Tracker
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class ClusterTracker(private val machine: MachineDTO, private val afterStarted: () -> Unit = {}) : Tracker() {

    private val maxHeartbeatTimeout = 30L

    private val currentRole get() = MachineService.findById(machine.id)?.role ?: throw NotFoundException()

    /**
     * 从 slave 中选举 master，当前只是简单地选取内存和 CPU 占用最低的 slave 作为 master, 后续需要优化为 paxos 的选举
     * 如果当前没有存活的 slave, 则抛出 NotFoundException
     */
    private fun electMasterFromSlave() {
        val (slaves, slaveCount) = MachineService.listingActiveSlave()
        if (slaveCount == 0) throw NotFoundException()
        val nextMaster = slaves.sortedWith(compareBy({ it.memLoad }, { it.cpuLoad })).first()
        MachineService.update(nextMaster.id, role = MachineRole.MASTER)
    }

    /**
     * 将所有 master 重置为 slave
     */
    private fun resetMasters(masters: List<MachineDTO>) = masters.forEach {
        MachineService.update(it.id, role = MachineRole.SLAVE)
    }


    /**
     * 确保只有一个 master
     * 如果没有 master，则从 slave 中选一个
     * 如果有多个 master，则重置 master 为 slave，然后从 slave 中选一个
     */
    private fun ensureOnlyOneMaster() {
        val (masters, masterCount) = MachineService.listingActiveMaster()
        when (masterCount) {
            0 -> electMasterFromSlave()
            1 -> return
            else -> resetMasters(masters).also { electMasterFromSlave() }
        }
    }

    private fun checkSlaveAlive() {
        val now = LocalDateTime.now()
        val (slaves, count) = MachineService.listingActiveSlave()
        slaves.filter { it.updateTime.plusSeconds(maxHeartbeatTimeout).isBefore(now) }.forEach {
            MachineService.update(it.id, isActive = false)
        }
    }

    private fun checkMasterAlive() {
        val now = LocalDateTime.now()
        val (masters, count) = MachineService.listingActiveMaster()
        masters.filter { it.updateTime.plusSeconds(maxHeartbeatTimeout).isBefore(now) }.forEach {
            MachineService.update(it.id, isActive = false)
        }
    }


    override fun onStarted() {
        onHeartBeat()
        afterStarted()
    }

    override fun onDestroyed() {}

    override fun onDateChange() {}

    override fun onHourChange() {}

    override fun onHeartBeat() = Database.global.useTransaction {
        ensureOnlyOneMaster()
        if (currentRole == MachineRole.MASTER) {
            checkSlaveAlive()
        } else {
            checkMasterAlive()
        }
    }
}