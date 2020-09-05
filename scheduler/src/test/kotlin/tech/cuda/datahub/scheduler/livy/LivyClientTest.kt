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
package tech.cuda.datahub.scheduler.livy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.datahub.scheduler.exception.LivyException
import tech.cuda.datahub.scheduler.livy.mocker.LivyServer
import tech.cuda.datahub.scheduler.livy.mocker.StatementExample
import tech.cuda.datahub.scheduler.livy.session.Session
import tech.cuda.datahub.scheduler.livy.session.SessionKind
import tech.cuda.datahub.scheduler.livy.session.SessionState
import tech.cuda.datahub.scheduler.livy.statement.*
import tech.cuda.datahub.scheduler.util.MachineUtil

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@DoNotParallelize
class LivyClientTest : AnnotationSpec() {

    @BeforeEach
    fun startLivyServer() {
        LivyServer.start()
    }

    @BeforeAll
    @AfterEach
    fun stopLivyServer() {
        LivyServer.stop()
    }

    @Test
    fun testCreateSession() {
        with(LivyClient.createSession(SessionKind.SQL, name = "testSQL")) {
            this.id shouldBe 0
            this.name shouldBe "testSQL"
            this.appId shouldBe null
            this.owner shouldBe null
            this.proxyUser shouldBe null
            this.state shouldBe SessionState.STARTING
            this.kind shouldBe SessionKind.SQL
            this.appInfo["driverLogUrl"] shouldBe null
            this.appInfo["sparkUiUrl"] shouldBe null
            this.log.size shouldBeGreaterThan 0
        }
        shouldThrow<LivyException> {
            LivyClient.createSession(SessionKind.SQL, name = "testSQL")
        }.message shouldBe "400 Bad Request Duplicate session name: Some(testSQL) for session 1"

        with(LivyClient.createSession(SessionKind.PY_SPARK, name = "testPySpark")) {
            this.id shouldBe 2
            this.name shouldBe "testPySpark"
            this.appId shouldBe null
            this.owner shouldBe null
            this.proxyUser shouldBe null
            this.state shouldBe SessionState.STARTING
            this.kind shouldBe SessionKind.PY_SPARK
            this.appInfo["driverLogUrl"] shouldBe null
            this.appInfo["sparkUiUrl"] shouldBe null
            this.log.size shouldBeGreaterThan 0
        }
        shouldThrow<LivyException> {
            LivyClient.createSession(SessionKind.PY_SPARK, name = "testPySpark")
        }.message shouldBe "400 Bad Request Duplicate session name: Some(testPySpark) for session 3"

        with(LivyClient.createSession(SessionKind.SPARK, name = "testSpark")) {
            this.id shouldBe 4
            this.name shouldBe "testSpark"
            this.appId shouldBe null
            this.owner shouldBe null
            this.proxyUser shouldBe null
            this.state shouldBe SessionState.STARTING
            this.kind shouldBe SessionKind.SPARK
            this.appInfo["driverLogUrl"] shouldBe null
            this.appInfo["sparkUiUrl"] shouldBe null
            this.log.size shouldBeGreaterThan 0
        }
        shouldThrow<LivyException> {
            LivyClient.createSession(SessionKind.SPARK, name = "testSpark")
        }.message shouldBe "400 Bad Request Duplicate session name: Some(testSpark) for session 5"

        with(LivyClient.createSession(SessionKind.SPARK_R, name = "testSparkR")) {
            this.id shouldBe 6
            this.name shouldBe "testSparkR"
            this.appId shouldBe null
            this.owner shouldBe null
            this.proxyUser shouldBe null
            this.state shouldBe SessionState.STARTING
            this.kind shouldBe SessionKind.SPARK_R
            this.appInfo["driverLogUrl"] shouldBe null
            this.appInfo["sparkUiUrl"] shouldBe null
            this.log.size shouldBeGreaterThan 0
        }
        shouldThrow<LivyException> {
            LivyClient.createSession(SessionKind.SPARK_R, name = "testSparkR")
        }.message shouldBe "400 Bad Request Duplicate session name: Some(testSparkR) for session 7"
    }

    @Test
    fun listingSessions() {
        LivyClient.createSession(SessionKind.SQL, name = "testSQL")
        LivyClient.listSessions().size shouldBe 1

        LivyClient.createSession(SessionKind.PY_SPARK, name = "testPySpark")
        LivyClient.listSessions().size shouldBe 2

        LivyClient.createSession(SessionKind.SPARK, name = "testSpark")
        LivyClient.listSessions().size shouldBe 3

        LivyClient.createSession(SessionKind.SPARK_R, name = "testSparkR")
        LivyClient.listSessions().size shouldBe 4

        LivyClient.listSessions(4, 10).size shouldBe 0
        val sessions = LivyClient.listSessions()
        sessions.size shouldBe 4
        sessions.map { it.id } shouldContainExactlyInAnyOrder listOf(0, 1, 2, 3)
        sessions.map { it.name } shouldContainExactlyInAnyOrder listOf("testSQL", "testPySpark", "testSpark", "testSparkR")
        sessions.map { it.kind } shouldContainExactlyInAnyOrder listOf(SessionKind.SPARK_R, SessionKind.SPARK, SessionKind.PY_SPARK, SessionKind.SQL)
    }

    @Test
    fun findById() {
        LivyClient.createSession(SessionKind.SQL, name = "testSQL")
        with(LivyClient.getSession(0)) {
            this shouldNotBe null
            this!!
            this.name shouldBe "testSQL"
        }

        LivyClient.getSession(1) shouldBe null
        LivyClient.createSession(SessionKind.SPARK, name = "testSpark")
        with(LivyClient.getSession(1)) {
            this shouldNotBe null
            this!!
            this.name shouldBe "testSpark"
        }
    }

    @Test
    fun testRemoveSession() {
        LivyClient.createSession(SessionKind.SQL, name = "testSQL")
        val session = LivyClient.createSession(SessionKind.SPARK, name = "testSpark")
        LivyClient.createSession(SessionKind.PY_SPARK, name = "testPySpark")
        with(LivyClient.listSessions()) {
            this.size shouldBe 3
            this.map { it.name } shouldContainExactlyInAnyOrder listOf("testSQL", "testSpark", "testPySpark")
        }

        session.kill() shouldBe true
        with(LivyClient.listSessions()) {
            this.size shouldBe 2
            this.map { it.name } shouldContainExactlyInAnyOrder listOf("testPySpark", "testSQL")
        }
    }

    private fun Session.runSuccessStatement(example: StatementExample.Example, expectStatementId: Int) {
        val statement = this.createStatement(example.code)
        statement shouldNotBe null
        statement!!
        statement.sessionId shouldBe this.id
        statement.id shouldBe expectStatementId
        statement.code shouldBe example.code
        statement.state shouldBeIn listOf(StatementState.WAITING, StatementState.RUNNING)
        statement.progress shouldBeEqualComparingTo 0.0
        statement.waitFinishedAndReturnState() shouldBe StatementState.AVAILABLE
        statement.progress shouldBeEqualComparingTo 1.0
        statement.output.status shouldBe "ok"
        statement.output.stdout shouldBe example.stdout
        statement.output.executionCount shouldBe expectStatementId
        statement.output.errorName shouldBe null
        statement.output.errorValue shouldBe null
        statement.output.traceback shouldBe null
    }

    private fun Session.runFailedStatement(example: StatementExample.Example, expectStatementId: Int) {
        val statement = this.createStatement(example.code)
        statement shouldNotBe null
        statement!!
        statement.sessionId shouldBe this.id
        statement.id shouldBe expectStatementId
        statement.code shouldBe example.code
        statement.state shouldBeIn listOf(StatementState.WAITING, StatementState.RUNNING)
        statement.progress shouldBeEqualComparingTo 0.0
        statement.waitFinishedAndReturnState() shouldBe StatementState.AVAILABLE
        statement.state shouldBe StatementState.AVAILABLE
        statement.progress shouldBeEqualComparingTo 1.0
        statement.output.status shouldBe "error"
        statement.output.executionCount shouldBe 1
        statement.output.errorName shouldBe example.errorName
        statement.output.errorValue shouldBe example.stderr
        statement.output.traceback shouldNotBe null
    }

    private fun Session.runCancelStatement(example: StatementExample.Example, expectStatementId: Int) {
        val statement = this.createStatement(example.code)
        statement shouldNotBe null
        statement!!
        statement.sessionId shouldBe 0
        statement.id shouldBe expectStatementId
        statement.code shouldBe example.code
        statement.state shouldBeIn listOf(StatementState.WAITING, StatementState.RUNNING)
        statement.progress shouldBeEqualComparingTo 0.0
        Thread.sleep(1000L).also {
            shouldThrow<LivyException> {
                statement.cancel()
            }
        }

    }

    @Test
    fun testSqlStatement() {
        val session = LivyClient.createSession(SessionKind.SQL, name = "testSqlStatement")
        session.waitIDLE() shouldBe true
        session.runSuccessStatement(StatementExample.SQL_SELECT_PRIMARY_TYPE, 0)
        session.runFailedStatement(StatementExample.SQL_SELECT_FROM_NOT_EXISTS_TABLE, 1)
        session.runCancelStatement(StatementExample.SQL_SLEEP, 2)
    }

    @Test
    fun testSparkStatement() {
        val session = LivyClient.createSession(SessionKind.SPARK, name = "testSparkStatement")
        session.waitIDLE() shouldBe true
        session.runSuccessStatement(StatementExample.SPARK_WORD_COUNT, 0)
        session.runFailedStatement(StatementExample.SPARK_NOT_EXISTS_VARIABLE, 1)
        session.runCancelStatement(StatementExample.SPARK_SLEEP, 2)
    }

    @Test
    fun testPySparkStatement() {
        val session = LivyClient.createSession(SessionKind.PY_SPARK, name = "testPySparkStatement")
        session.waitIDLE() shouldBe true
        session.runSuccessStatement(StatementExample.PY_SPARK_WORD_COUNT, 0)
        session.runFailedStatement(StatementExample.PY_SPARK_NOT_EXISTS_VARIABLE, 1)
        session.runCancelStatement(StatementExample.PY_SPARK_SLEEP, 2)
    }

    @Test
    fun testSparkRStatement() {
        val session = LivyClient.createSession(SessionKind.SPARK_R, name = "testSparkRStatement")
        session.waitIDLE() shouldBe true
        session.runSuccessStatement(StatementExample.SPARK_R_WORDCOUNT, 0)
        session.runFailedStatement(StatementExample.SPARK_R_NOT_EXISTS_VARIABLE, 1)
        session.runCancelStatement(StatementExample.SPARK_R_SLEEP, 2)
    }

}
