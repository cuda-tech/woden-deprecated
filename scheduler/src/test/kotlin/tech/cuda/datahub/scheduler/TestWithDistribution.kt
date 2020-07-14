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
import io.mockk.*
import tech.cuda.datahub.config.Datahub
import tech.cuda.datahub.service.Database
import java.time.*
import java.util.*


/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
open class TestWithDistribution(private vararg val tables: String = arrayOf()) : AnnotationSpec() {

    @BeforeAll
    fun beforeAll() {
        val db = DB.newEmbeddedDB(DBConfigurationBuilder.newBuilder().also {
            it.port = 0
            it.baseDir = System.getProperty("java.io.tmpdir") + this.javaClass.simpleName
        }.build()).also { it.start() }
        mockkObject(Datahub.database)
        every { Datahub.database.properties } returns Properties().also { props ->
            props["url"] = "jdbc:mysql://localhost:${db.configuration.port}/?characterEncoding=UTF-8"
            props["username"] = "root"
        }
        Database.connect(Datahub.database)
    }

    @AfterAll
    fun afterAll() {
        unmockkObject(Datahub.database)
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

