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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import tech.cuda.datahub.scheduler.exception.LivyException
import tech.cuda.datahub.scheduler.livy.LivyClient
import tech.cuda.datahub.scheduler.livy.session.SessionKind
import tech.cuda.datahub.scheduler.livy.statement.Statement
import tech.cuda.datahub.scheduler.livy.statement.StatementState
import tech.cuda.datahub.service.dto.TaskDTO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class SparkSqlOperator(task: TaskDTO) : Operator(task) {
    private lateinit var statement: Statement

    override val isFinish: Boolean
        get() = if (this::statement.isInitialized) {
            statement.state == StatementState.AVAILABLE || statement.state == StatementState.CANCELLED
        } else {
            false
        }


    override val isSuccess: Boolean
        get() = if (this::statement.isInitialized) {
            statement.state == StatementState.AVAILABLE
                && statement.output.errorName == null
        } else {
            false
        }

    override val output: String
        get() = if (this::statement.isInitialized) {
            statement.output.errorValue ?: statement.output.stdout
        } else {
            ""
        }


    override fun start() {
        this.job = GlobalScope.async {
            try {
                val session = LivyClient.createSession(SessionKind.SQL)
                session.waitIDLE()
                statement = session.createStatement(mirror?.content ?: "") ?: throw LivyException()
            } catch (e: Throwable) {
                e.printStackTrace()
                logger.error(e)
            }
        }
    }

    override fun kill() {
        if (this::statement.isInitialized) {
            statement.cancel()
        }
    }

}