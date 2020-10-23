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
package tech.cuda.woden.adhoc

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class AnacondaAdhocTest : AnnotationSpec() {

    @Test
    fun testSuccessJob() {
        val job = AnacondaAdhoc("""
            import time
            print("hello")
            time.sleep(5)
            print("world")
        """.trimIndent())
        job.status shouldBe AdhocStatus.NOT_START
        job.start()
        do {
            Thread.sleep(100)
        } while (job.status == AdhocStatus.NOT_START)
        var hasBufferOutput = false
        while (job.status == AdhocStatus.RUNNING) {
            if (job.output != "") {
                job.output shouldBe "hello\n"
                hasBufferOutput = true
                break
            }
            Thread.sleep(123)
        }
        hasBufferOutput shouldBe true
        job.join()
        job.status shouldBe AdhocStatus.SUCCESS
        job.output shouldBe "hello\nworld\n"
    }

    @Test
    fun testWrongStatement() {
        val job = AnacondaAdhoc("print(notExists)")
        job.status shouldBe AdhocStatus.NOT_START
        job.startAndJoin()
        job.status shouldBe AdhocStatus.FAILED
        job.output shouldContain "NameError: name 'notExists' is not defined"
    }

    @Test
    fun testWrongIndent() {
        val job = AnacondaAdhoc("  print(1234)")
        job.status shouldBe AdhocStatus.NOT_START
        job.startAndJoin()
        job.status shouldBe AdhocStatus.FAILED
        job.output shouldContain "IndentationError: unexpected indent"
    }

    @Test
    fun testRaiseException() {
        val job = AnacondaAdhoc("""
            print("hello")
            raise Exception("stop here")
            print("world")
        """.trimIndent())
        job.status shouldBe AdhocStatus.NOT_START
        job.startAndJoin()
        job.status shouldBe AdhocStatus.FAILED
        job.output shouldContain "hello\n"
        job.output shouldContain "Exception: stop here"
        job.output shouldNotContain "world"
    }

    @Test
    fun testKill() {
        val job = AnacondaAdhoc("""
            import time
            print("hello")
            time.sleep(10)
            print("world")
        """.trimIndent())
        job.status shouldBe AdhocStatus.NOT_START
        job.start()
        while (job.output != "hello\n") {
            Thread.sleep(123)
        }
        job.kill()
        job.join()
        job.status shouldBe AdhocStatus.KILLED
        job.output shouldContain "hello\n"
        job.output shouldContain "Process exited with an error"
        job.output shouldNotContain "world"
    }

    @Test
    fun testUserDefineArgument() {
        val job = AnacondaAdhoc(
            code = """
                import sys
                print(sys.argv[1])
                print(sys.argv[2])
            """.trimIndent(),
            arguments = listOf("hello", "world!")
        )
        job.status shouldBe AdhocStatus.NOT_START
        job.startAndJoin()
        job.status shouldBe AdhocStatus.SUCCESS
        job.output shouldBe "hello\nworld!\n"
    }

    @Test
    fun testUserDefineKvArgument() {
        val job = AnacondaAdhoc(
            code = """
                import argparse
                parser = argparse.ArgumentParser()
                parser.add_argument("--first", "-f")
                parser.add_argument("--second", "-s")
                args = parser.parse_args()
                print("first =", args.first)
                print("second =", args.second)
            """.trimIndent(),
            kvArguments = mapOf("-f" to "1", "--second" to "2")
        )
        job.status shouldBe AdhocStatus.NOT_START
        job.startAndJoin()
        job.status shouldBe AdhocStatus.SUCCESS
        job.output shouldBe "first = 1\nsecond = 2\n"
    }
}