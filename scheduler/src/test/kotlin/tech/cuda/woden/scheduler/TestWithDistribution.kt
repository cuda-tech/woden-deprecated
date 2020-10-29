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
package tech.cuda.woden.scheduler

import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.*
import tech.cuda.woden.config.DataSourceMocker
import tech.cuda.woden.config.Woden
import tech.cuda.woden.scheduler.util.MachineUtil
import tech.cuda.woden.service.Database
import tech.cuda.woden.service.MachineService
import tech.cuda.woden.service.exception.NotFoundException
import java.time.LocalDate
import java.time.LocalDateTime


/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
abstract class TestWithDistribution(private vararg val tables: String = arrayOf()) : AnnotationSpec() {

    @BeforeAll
    fun beforeAll() {
        DataSourceMocker.mock()
        Database.connect(Woden.datasource)
    }

    @AfterAll
    fun afterAll() {
        DataSourceMocker.unMock()
    }

    @BeforeEach
    fun beforeEach() {
        Database.rebuild()
        tables.forEach { Database.mock(it) }
    }

    /**
     * mock LocalDateTime 和 LocalDate 的 now 方法，返回期望的时间
     * 如果 [year], [month], [day], [hour], [minute], [second] 不指定，则采用当前的值
     */
    protected fun supposeNowIs(year: Int? = null, month: Int? = null, day: Int? = null,
                               hour: Int? = null, minute: Int? = null, second: Int? = null,
                               block: () -> Unit) {
        val now = LocalDateTime.now()
        val mock = LocalDateTime.of(
            year ?: now.year,
            month ?: now.monthValue,
            day ?: now.dayOfMonth,
            hour ?: now.hour,
            minute ?: now.minute,
            second ?: now.second
        )
        mockkStatic(LocalDateTime::class)
        mockkStatic(LocalDate::class)
        every { LocalDateTime.now() } returns mock
        every { LocalDate.now() } returns mock.toLocalDate()
        block()
        unmockkStatic(LocalDateTime::class)
        unmockkStatic(LocalDate::class)
    }

    /**
     * mock 机器，假定自己是编号为[id]的服务器
     */
    protected fun supposeImMachine(id: Int, block: () -> Unit) {
        mockkObject(MachineUtil)
        val machine = MachineService.findById(id) ?: throw NotFoundException()
        every { MachineUtil.systemInfo } returns MachineUtil.SystemInfo( // machine ID = 1
            ip = machine.ip,
            mac = machine.mac,
            hostname = machine.hostname,
            isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
        )
        block()
        unmockkObject(MachineUtil)
    }
}

