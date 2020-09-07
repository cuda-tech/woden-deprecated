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
package tech.cuda.datahub.scheduler.ops

import io.kotest.matchers.shouldBe
import tech.cuda.datahub.scheduler.TestWithDistribution
import tech.cuda.datahub.scheduler.livy.mocker.LivyServer
import tech.cuda.datahub.service.TaskService

class SparkSqlOperatorTest : TestWithDistribution("tasks", "file_mirrors") {

    @BeforeEach
    fun startLivyServer() {
        LivyServer.start()
    }

    @BeforeAll
    fun stopLivyServer() {
        LivyServer.stop()
    }

    @Test
    fun submitSparkSql() {
        // task(49) -> mirror(290)
        val sql = SparkSqlOperator(TaskService.findById(49)!!)
        sql.start()
        sql.isFinish shouldBe false
        while (!sql.isFinish) {
            Thread.sleep(123)
        }
        sql.isFinish shouldBe true
        sql.isSuccess shouldBe true
        sql.output shouldBe """
            {"schema":{"type":"struct","fields":[{"name":"hello world","type":"string","nullable":false,"metadata":{}}]},"data":[["hello world"]]}
        """.trimIndent()
    }

    @Test
    fun submitWrongSparkSql() {
        // task(50) -> mirror(278)
        val sql = SparkSqlOperator(TaskService.findById(50)!!)
        sql.start()
        sql.isFinish shouldBe false
        while (!sql.isFinish) {
            Thread.sleep(123)
        }
        sql.isFinish shouldBe true
        sql.isSuccess shouldBe false
        sql.output shouldBe "Table or view not found: not_exist_table; line 1 pos 14".trimIndent()
    }

}