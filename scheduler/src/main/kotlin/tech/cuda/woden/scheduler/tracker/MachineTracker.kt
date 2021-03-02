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
package tech.cuda.woden.scheduler.tracker

import tech.cuda.woden.scheduler.util.MachineUtil
import tech.cuda.woden.common.service.MachineService
import tech.cuda.woden.common.service.dto.MachineDTO

/**
 * 负责服务器注册 & 负载更新
 * 其中 [afterStarted] 是启动后的回调，一般只用于单测
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class MachineTracker(private val afterStarted: (MachineTracker) -> Unit = {}) : Tracker() {

    lateinit var machine: MachineDTO

    /**
     * 启动时根据 hostname 地址判断机器是否已注册
     * 如果尚未注册，则注册机器
     * 最后更新机器的负载信息
     */
    override fun onStarted() {
        val systemInfo = MachineUtil.systemInfo
        val loadInfo = MachineUtil.loadInfo
        machine = MachineService.findByHostname(systemInfo.hostname) ?: MachineService.create(systemInfo.hostname)
        machine = MachineService.update(
            id = machine.id,
            cpuLoad = loadInfo.cpu,
            memLoad = loadInfo.memory,
            diskUsage = loadInfo.diskUsage
        )
        afterStarted(this)
    }

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