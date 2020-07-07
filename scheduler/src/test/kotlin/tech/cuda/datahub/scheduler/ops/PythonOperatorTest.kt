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
class PythonOperatorTest : TestWithDistribution("tasks", "file_mirrors"), TempFileGetter {

    /**
     * 由于生产环境用的是 Anaconda，因此使用 python 命令
     * 但默认的 Linux 和 WSL 不自带 python, 因此将 type 字段设置成 python3
     * 后面的测试用例同理
     */
    @Test
    fun success() {
        val python = PythonOperator(TaskService.findById(10)!!, "python3") // mirror_id = 153
        python.start()
        val tempFile = Thread.sleep(100).run { getTempFile("python3_") }
        tempFile.exists() shouldBe true
        python.isFinish shouldBe false
        python.isSuccess shouldBe false
        python.output shouldBe ""

        var bufferOutput = ""
        while (!python.isFinish) {
            bufferOutput = python.output
            Thread.sleep(123)
        }
        bufferOutput shouldBe "hello\n"

        python.join()
        python.isFinish shouldBe true
        python.isSuccess shouldBe true
        python.output shouldBe "hello\nworld\n"
        tempFile.exists() shouldBe false
    }

    @Test
    fun failed() {
        val python = PythonOperator(TaskService.findById(11)!!, "python3") // mirror_id = 65
        python.start()
        val tempFile = Thread.sleep(100).run { getTempFile("python3_") }
        tempFile.exists() shouldBe true
        python.isFinish shouldBe false
        python.isSuccess shouldBe false
        python.join()
        python.isFinish shouldBe true
        python.isSuccess shouldBe false
        python.output shouldContain "NameError: name 'notExists' is not defined"
        tempFile.exists() shouldBe false
    }

    @Test
    fun raiseException() {
        val python = PythonOperator(TaskService.findById(14)!!, "python3") // mirror_id = 124
        python.start()
        val tempFile = Thread.sleep(100).run { getTempFile("python3_") }
        tempFile.exists() shouldBe true
        python.isFinish shouldBe false
        python.isSuccess shouldBe false
        python.join()
        python.isFinish shouldBe true
        python.isSuccess shouldBe false
        python.output shouldContain "Exception: error"
        tempFile.exists() shouldBe false
    }

    @Test
    fun kill() {
        val python = PythonOperator(TaskService.findById(16)!!, "python3") // mirror_id = 38
        python.start()
        val tempFile = Thread.sleep(100).run { getTempFile("python3_") }
        tempFile.exists() shouldBe true
        python.isFinish shouldBe false
        python.isSuccess shouldBe false
        python.kill()
        python.join()
        python.isFinish shouldBe true
        python.isSuccess shouldBe false
        tempFile.exists() shouldBe false
        println(python.output)
    }
}