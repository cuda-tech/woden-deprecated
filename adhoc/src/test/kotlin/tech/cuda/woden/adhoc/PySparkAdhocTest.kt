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
class PySparkAdhocTest : AnnotationSpec() {

    @Test
    fun testContextInited() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = PySparkAdhoc(code = """
            print("sc =", sc)
            print("sql =", sql)
            print("sqlContext =", sqlContext)
            print("sqlCtx =", sqlCtx)
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe AdhocStatus.SUCCESS
        job.output shouldContain "sc = <SparkContext master="
        job.output shouldContain "sql = <bound method SparkSession.sql of <pyspark.sql.session.SparkSession object at "
        job.output shouldContain "sqlContext = <pyspark.sql.context.SQLContext object at "
        job.output shouldContain "sqlCtx = <pyspark.sql.context.SQLContext object at "
        job.close()
    }

    @Test
    fun testWrongStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = PySparkAdhoc(code = "print(notExistsVariable)")
        job.startAndJoin()
        job.status shouldBe AdhocStatus.FAILED
        job.output shouldContain "NameError: name 'notExistsVariable' is not defined"
        job.close()
    }

    @Test
    fun testWordCount() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = PySparkAdhoc(code = """
            word_count = sc.parallelize([
                'apple apple facebook microsoft apple microsoft google apple google google',
                'alibaba tencent alibaba alibaba'
            ]).flatMap(lambda line: line.split(' ')) \
            .map(lambda word: (word, 1)) \
            .reduceByKey(lambda a, b: a + b) \
            .map(lambda a: a[1]) \
            .reduce(lambda a, b: a + b)
            print("word count =", word_count)
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe AdhocStatus.SUCCESS
        job.output shouldContain "word count = 14"
        job.close()
    }

    @Test
    fun testKillJob() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = PySparkAdhoc(code = """
            import time
            time.sleep(30)
            print("now, wake up")
        """.trimIndent())
        job.start()
        while (job.status != AdhocStatus.RUNNING) {
            Thread.sleep(1000)
        }
        Thread.sleep(3000).also { job.kill() }
        do {
            Thread.sleep(1000)
        } while (job.status == AdhocStatus.RUNNING)
        job.status shouldBe AdhocStatus.KILLED
        job.output shouldNotContain "now, wake up"
        job.close()
    }
}