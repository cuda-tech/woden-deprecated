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

import tech.cuda.datahub.scheduler.util.MachineUtil
import tech.cuda.datahub.service.MachineService
import tech.cuda.datahub.service.dto.MachineDTO

/**
 * 负责服务器注册 & 负载更新
 * [afterStarted]: Tracker 启动后的回调
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class MachineTracker(private val afterStarted: (MachineDTO) -> Unit = {}) : Tracker() {

    lateinit var machine: MachineDTO

    /**
     * 启动时根据 MAC 地址判断机器是否已注册
     * 如果尚未注册，则注册机器
     * 如果已注册，则检查 IP 和 hostname 是否发生变更，如果发生变更则更新
     * 最后更新机器的负载信息
     */
    override fun onStarted() {
        val systemInfo = MachineUtil.systemInfo
        val loadInfo = MachineUtil.loadInfo
        machine = MachineService.findByMac(systemInfo.mac)
            ?: MachineService.create(systemInfo.ip, systemInfo.hostname, systemInfo.mac)
        machine = MachineService.update(
            id = machine.id,
            ip = if (systemInfo.ip != machine.ip) systemInfo.ip else null,
            hostname = if (systemInfo.hostname != machine.hostname) systemInfo.hostname else null,
            cpuLoad = loadInfo.cpu,
            memLoad = loadInfo.memory,
            diskUsage = loadInfo.diskUsage
        )
        afterStarted(machine)
    }

    override fun onDestroyed() {}

    override fun onDateChange() {}

    override fun onHourChange() {}

    /**
     * 每次心跳检查机器负载并更新
     */
    override fun onHeartBeat() {
        val loadInfo = MachineUtil.loadInfo
        machine = MachineService.update(
            id = machine.id,
            cpuLoad = loadInfo.cpu,
            memLoad = loadInfo.memory,
            diskUsage = loadInfo.diskUsage
        )
    }

}