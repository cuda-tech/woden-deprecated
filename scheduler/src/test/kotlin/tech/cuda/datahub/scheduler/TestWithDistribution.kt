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
package tech.cuda.datahub.scheduler

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import tech.cuda.datahub.service.Database
import tech.cuda.datahub.service.config.DatabaseConfig
import java.io.File
import java.time.*


/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
open class TestWithDistribution(private vararg val tables: String = arrayOf()) : AnnotationSpec() {

    @BeforeAll
    fun beforeAll() {
        val db = DB.newEmbeddedDB(DBConfigurationBuilder.newBuilder().also {
            it.port = 0
            it.baseDir = System.getProperty("java.io.tmpdir") +  this.javaClass.simpleName
        }.build()).also { it.start() }
        Database.connect(DatabaseConfig(port = db.configuration.port))
//        Database.connect(DatabaseConfig(port = 3306))
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
}

