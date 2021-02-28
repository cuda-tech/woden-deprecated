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

import java.io.File

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class PySparkAdhoc(
    code: String,
    override val sparkConf: Map<String, String> = mapOf()
) : AbstractSparkAdhoc() {

    override val mainClass = "org.apache.spark.deploy.SparkSubmit"

    private val tempFile = File.createTempFile("__adhoc__", ".py").also {
        it.writeText("""
        |import atexit, os, platform, warnings, py4j
        |from pyspark import SparkConf
        |from pyspark.context import SparkContext
        |from pyspark.sql import SparkSession, SQLContext

        |if os.environ.get("SPARK_EXECUTOR_URI"):
        |    SparkContext.setSystemProperty("spark.executor.uri", os.environ["SPARK_EXECUTOR_URI"])

        |SparkContext._ensure_initialized()

        |try:
        |    conf = SparkConf()
        |    if conf.get('spark.sql.catalogImplementation', 'hive').lower() == 'hive':
        |        SparkContext._jvm.org.apache.hadoop.hive.conf.HiveConf()
        |        spark = SparkSession.builder.enableHiveSupport().getOrCreate()
        |    else:
        |        spark = SparkSession.builder.getOrCreate()
        |except Exception:
        |    if conf.get('spark.sql.catalogImplementation', '').lower() == 'hive':
        |        warnings.warn("Fall back to non-hive support because failing to access HiveConf, please make sure you build spark with hive")
        |    spark = SparkSession.builder.getOrCreate()

        |sc, sql = spark.sparkContext, spark.sql
        |atexit.register(lambda: sc.stop())
        |sqlContext = sqlCtx = spark._wrapped

        |print('''Welcome to
        |      ____              __
        |     / __/__  ___ _____/ /__
        |    _\ \/ _ \/ _ `/ __/  '_/
        |   /__ / .__/\_,_/_/ /_/\_\   version %s
        |      /_/
        |''' % sc.version)
        |print("Using Python version %s (%s, %s)" % (platform.python_version(), platform.python_build()[0], platform.python_build()[1]))
        |print("SparkSession available as 'spark'.")
        
        |$code
        """.trimMargin())
        it.deleteOnExit()
    }

    override val appArgs = listOf(tempFile.path)

}