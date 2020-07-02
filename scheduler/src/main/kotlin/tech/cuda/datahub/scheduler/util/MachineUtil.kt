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
package tech.cuda.datahub.scheduler.util

import com.sun.management.OperatingSystemMXBean
import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import tech.cuda.datahub.scheduler.exception.HardwareException
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.math.ceil

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object MachineUtil {

    data class SystemInfo(val ip: String, val mac: String, val hostname: String, val isWindows: Boolean = false)
    data class LoadInfo(val cpu: Int, val memory: Int, val diskUsage: Int)

    val systemInfo: SystemInfo

    private val processor = SystemInfo().hardware.processor
    private var prevTicks = processor.systemCpuLoadTicks

    /**
     * 判断一个 MAC 是否为虚拟网卡的 MAC
     */
    private val ByteArray.isVMMac
        get() = listOf(
            listOf(0x00, 0x05, 0x69), // VMWare
            listOf(0x00, 0x1C, 0x14), // VMWare
            listOf(0x00, 0x0C, 0x29), // VMWare
            listOf(0x00, 0x50, 0x56), // VMWare
            listOf(0x08, 0x00, 0x27), // VirtualBox
            listOf(0x0A, 0x00, 0x27), // VirtualBox
            listOf(0x00, 0x03, 0xFF), // Virtual-PC
            listOf(0x00, 0x15, 0x5D)  // Hyper-V
        ).any {
            it[0].toByte() == this[0] && it[1].toByte() == this[1] && it[2].toByte() == this[2]
        }


    init {
        // 获取系统的 hostname、IP、MAC
        // 如果存在多张正在使用中的网卡，则抛出 HardwareException
        val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList().filter {
            it != null && it.hardwareAddress != null && it.isUp // 只保留使用中的网卡
                && !it.isVirtual // 过滤掉虚拟网卡
                && !it.displayName.toUpperCase().contains("BLUETOOTH") // 过滤掉蓝牙，quick & dirty 地通过设备名来判断是否为蓝牙
                && !it.hardwareAddress.isVMMac // 过滤掉虚拟机网卡
        }
        systemInfo = when (networkInterfaces.size) {
            0 -> throw HardwareException()
            1 -> {
                val networkInterface = networkInterfaces.first()
                val mac = networkInterface.hardwareAddress.joinToString("-") { String.format("%02X", it) }
                val ip = networkInterface.inetAddresses.toList().first {
                    !it.isLoopbackAddress && !it.hostAddress.contains(':')
                }.hostAddress
                val hostname = InetAddress.getLocalHost().hostName
                SystemInfo(ip, mac, hostname, System.getProperty("os.name").toLowerCase().contains("windows"))
            }
            else -> throw  HardwareException()
        }
    }


    private fun percent(free: Number, total: Number) = ceil(100.0 - (100.0 * free.toDouble() / total.toDouble())).toInt()

    val loadInfo: LoadInfo
        get() {
            // 获取 CPU
            val nextTicks = processor.systemCpuLoadTicks
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