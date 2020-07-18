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

import io.kotest.core.spec.DoNotParallelize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import tech.cuda.datahub.scheduler.TestWithDistribution
import tech.cuda.datahub.service.TaskService

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@DoNotParallelize
class BashOperatorTest : TestWithDistribution("tasks", "file_mirrors"), TempFileGetter {

    @Test
    fun success() {
        val bash = BashOperator(TaskService.findById(3)!!) // mirror_id = 143
        bash.start()
        val tempFile = Thread.sleep(100).run { getTempFile("bash_") }
        tempFile.exists() shouldBe true
        bash.isFinish shouldBe false
        bash.isSuccess shouldBe false
        bash.output shouldBe ""

        var hasBufferOutput = false
        while (!bash.isFinish) {
            if (bash.output != "") {
                bash.output shouldBe "hello\n"
                hasBufferOutput = true
                break
            }
            Thread.sleep(123)
        }
        hasBufferOutput shouldBe true

        bash.join()
        bash.isFinish shouldBe true
        bash.isSuccess shouldBe true
        bash.output shouldBe "hello\nworld\n"
        tempFile.exists() shouldBe false
    }

    @Test
    fun failed() {
        val bash = BashOperator(TaskService.findById(8)!!) // mirror_id = 190
        bash.start()
        val tempFile = Thread.sleep(100).run { getTempFile("bash_") }
        tempFile.exists() shouldBe true
        bash.isFinish shouldBe false
        bash.isSuccess shouldBe false
        bash.join()
        bash.output shouldContain "line 1: command_not_exists: command not found"
        bash.isFinish shouldBe true
        bash.isSuccess shouldBe false
        tempFile.exists() shouldBe false
    }

    @Test
    fun kill() {
        val bash = BashOperator(TaskService.findById(4)!!) // mirror_id = 58
        bash.start()
        val tempFile = Thread.sleep(100).run { getTempFile("bash_") }
        tempFile.exists() shouldBe true
        bash.kill()
        bash.join()
        bash.isFinish shouldBe true
        bash.isSuccess shouldBe false
        tempFile.exists() shouldBe false
    }

    @Test
    fun userDefineArgument() {
        val bash = BashOperator(
            task = TaskService.findById(22)!!, // mirror_id = 69
            argument = listOf("first", "second")
        )
        bash.start()
        val tempFile = Thread.sleep(100).run { getTempFile("bash_") }
        tempFile.exists() shouldBe true
        bash.isFinish shouldBe false
        bash.isSuccess shouldBe false
        bash.output shouldBe ""

        bash.join()
        bash.isFinish shouldBe true
        bash.isSuccess shouldBe true
        bash.output shouldBe "first\nsecond\n"
        tempFile.exists() shouldBe false
    }

    @Test
    fun userDefineKvArgument() {
        val bash = BashOperator(
            task = TaskService.findById(25)!!, // mirror_id = 274
            kvArguments = mapOf("-f" to "1", "--second" to "2")
        )
        bash.start()
        val tempFile = Thread.sleep(100).run { getTempFile("bash_") }
        tempFile.exists() shouldBe true
        bash.isFinish shouldBe false
        bash.isSuccess shouldBe false
        bash.output shouldBe ""

        bash.join()
        bash.isFinish shouldBe true
        bash.isSuccess shouldBe true
        bash.output shouldBe "first = 1\nsecond = 2\n"
        tempFile.exists() shouldBe false
    }

}