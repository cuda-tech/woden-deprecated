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
package tech.cuda.datahub.adhoc

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class PySparkJobTest : AnnotationSpec() {

    private fun autoSetLocalDir(block: (Map<String, String>) -> Unit) {
        val tempDir = createTempDir(prefix = "__adhoc__", suffix = ".pyspark.local")
        val sparkLocal = tempDir.path.replace("\\", "/")
        val log4j = this.javaClass.classLoader.getResource("log4j.properties")!!.path
        block(mapOf(
            "spark.local.dir" to sparkLocal,
            "spark.driver.extraJavaOptions" to "-Dlog4j.configuration=file:$log4j",
            "spark.executor.extraJavaOptions" to "-Dlog4j.configuration=file:$log4j"
        ))
        tempDir.deleteRecursively()
    }

    @Test
    fun testContextInited() = autoSetLocalDir { sparkConf ->
        val job = PySparkJob(code = """
            print("sc =", sc)
            print("sql =", sql)
            print("sqlContext =", sqlContext)
            print("sqlCtx =", sqlCtx)
        """.trimIndent(), sparkConf = sparkConf)
        job.startAndJoin()
        job.status shouldBe JobStatus.SUCCESS
        job.output shouldContain "sc = <SparkContext master="
        job.output shouldContain "sql = <bound method SparkSession.sql of <pyspark.sql.session.SparkSession object at "
        job.output shouldContain "sqlContext = <pyspark.sql.context.SQLContext object at "
        job.output shouldContain "sqlCtx = <pyspark.sql.context.SQLContext object at "
        job.close()
    }

    @Test
    fun testWrongStatement() = autoSetLocalDir { sparkConf ->
        val job = PySparkJob(code = "print(notExistsVariable)", sparkConf = sparkConf)
        job.startAndJoin()
        job.status shouldBe JobStatus.FAILED
        job.output shouldContain "NameError: name 'notExistsVariable' is not defined"
        job.close()
    }

    @Test
    fun testWordCount() = autoSetLocalDir { sparkConf ->
        val job = PySparkJob(code = """
            word_count = sc.parallelize([
                'apple apple facebook microsoft apple microsoft google apple google google',
                'alibaba tencent alibaba alibaba'
            ]).flatMap(lambda line: line.split(' ')) \
            .map(lambda word: (word, 1)) \
            .reduceByKey(lambda a, b: a + b) \
            .map(lambda a: a[1]) \
            .reduce(lambda a, b: a + b)
            print("word count =", word_count)
        """.trimIndent(), sparkConf = sparkConf)
        job.startAndJoin()
        job.status shouldBe JobStatus.SUCCESS
        job.output shouldContain "word count = 14"
        job.close()
    }

    @Test
    fun testKillJob() = autoSetLocalDir { sparkConf ->
        val job = PySparkJob(code = """
            import time
            time.sleep(30)
            print("now, wake up")
        """.trimIndent(), sparkConf = sparkConf)
        job.start()
        while (job.status != JobStatus.RUNNING) {
            Thread.sleep(1000)
        }
        Thread.sleep(3000).also { job.kill() }
        do {
            Thread.sleep(1000)
        } while (job.status == JobStatus.RUNNING)
        job.status shouldBe JobStatus.KILLED
        job.output shouldNotContain "now, wake up"
        job.close()
    }
}