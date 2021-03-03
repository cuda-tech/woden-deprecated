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
package tech.cuda.woden.scheduler.util

import com.sun.management.OperatingSystemMXBean
import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import kotlin.math.ceil

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object ContainerUtil {

    data class SystemInfo(val hostname: String, val isWindows: Boolean = false)
    data class LoadInfo(val cpu: Int, val memory: Int, val diskUsage: Int)

    val systemInfo: SystemInfo

    private val processor = SystemInfo().hardware.processor
    private var prevTicks = processor.systemCpuLoadTicks


    init {
        val hostname = InetAddress.getLocalHost().hostName
        systemInfo = SystemInfo(hostname, System.getProperty("os.name").toLowerCase().contains("windows"))
    }


    private fun percent(free: Number, total: Number) = ceil(100.0 - (100.0 * free.toDouble() / total.toDouble())).toInt()

    val loadInfo: LoadInfo
        get() {
            // 获取 CPU
            val nextTicks = if (processor.systemCpuLoadTicks?.contentEquals(prevTicks) == true) {
                Thread.sleep(1000)
                processor.systemCpuLoadTicks
            } else {
                processor.systemCpuLoadTicks
            }
            val nice = nextTicks[CentralProcessor.TickType.NICE.index] - prevTicks[CentralProcessor.TickType.NICE.index]
            val irq = nextTicks[CentralProcessor.TickType.IRQ.index] - prevTicks[CentralProcessor.TickType.IRQ.index]
            val softIrq = nextTicks[CentralProcessor.TickType.SOFTIRQ.index] - prevTicks[CentralProcessor.TickType.SOFTIRQ.index]
            val steal = nextTicks[CentralProcessor.TickType.STEAL.index] - prevTicks[CentralProcessor.TickType.STEAL.index]
            val cSys = nextTicks[CentralProcessor.TickType.SYSTEM.index] - prevTicks[CentralProcessor.TickType.SYSTEM.index]
            val user = nextTicks[CentralProcessor.TickType.USER.index] - prevTicks[CentralProcessor.TickType.USER.index]
            val ioWait = nextTicks[CentralProcessor.TickType.IOWAIT.index] - prevTicks[CentralProcessor.TickType.IOWAIT.index]
            val idle = nextTicks[CentralProcessor.TickType.IDLE.index] - prevTicks[CentralProcessor.TickType.IDLE.index]
            val totalCpu = user + nice + cSys + idle + ioWait + irq + softIrq + steal
            prevTicks = nextTicks

            // 获取内存
            val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

            // 获取根磁盘
            val root = File("/")

            return LoadInfo(
                cpu = percent(idle, totalCpu),
                memory = percent(os.freePhysicalMemorySize, os.totalPhysicalMemorySize),
                diskUsage = percent(root.freeSpace, root.totalSpace)
            )
        }


}