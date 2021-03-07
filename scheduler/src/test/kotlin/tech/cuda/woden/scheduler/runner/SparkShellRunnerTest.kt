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
package tech.cuda.woden.scheduler.runner

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class SparkShellRunnerTest : AnnotationSpec() {

    @Test
    fun testSparkContextInited() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkShellRunner(code = """println("sc = " + sc.toString())""")
        job.startAndJoin()
        job.status shouldBe RunnerStatus.SUCCESS
        job.output shouldContain "sc = org.apache.spark.SparkContext@"
        job.close()
    }

    @Test
    fun testWrongStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkShellRunner(code = "println(notExistsVariable)")
        job.startAndJoin()
        job.status shouldBe RunnerStatus.FAILED
        job.output shouldContain "error: not found: value notExistsVariable"
        job.close()
    }

    @Test
    fun testWordCount() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkShellRunner(code = """
            val wordCount = sc.parallelize(Array(
                "apple apple facebook microsoft apple microsoft google apple google google",
                "alibaba tencent alibaba alibaba"
            )).flatMap(line => line.split(" "))
              .map(word => (word, 1))
              .reduceByKey((a, b) => a + b)
              .map(a => a._2)
              .reduce((a, b) => a + b)
            println("word count = " + wordCount)
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe RunnerStatus.SUCCESS
        job.output shouldContain "word count = 14"
        job.close()
    }

    @Test
    fun testKillJob() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkShellRunner(code = """
            Thread.sleep(30000)
            println("now, wake up")
        """.trimIndent())
        job.start()
        while (job.status != RunnerStatus.RUNNING) {
            Thread.sleep(1000)
        }
        Thread.sleep(3000).also { job.kill() }
        do {
            Thread.sleep(1000)
        } while (job.status == RunnerStatus.RUNNING)
        job.status shouldBe RunnerStatus.KILLED
        job.output shouldNotContain "now, wake up"
        job.close()
    }
}