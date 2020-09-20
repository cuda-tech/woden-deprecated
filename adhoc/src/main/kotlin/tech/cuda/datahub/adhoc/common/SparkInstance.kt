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
package tech.cuda.datahub.adhoc.common

import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.`SparkSession$`
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.api.java.JavaStreamingContext
import java.io.File
import java.io.IOException
import java.net.URI

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class SparkInstance(conf: SparkConf = SparkConf(), private val localTmpDir: File = File("/tmp")) {

    private val javaSparkContext = JavaSparkContext(SparkContext.getOrCreate(conf))

    val sc = javaSparkContext.sc()

    val sqlContext by lazy { SQLContext(javaSparkContext) }

    private val streamingContextDelegate = lazy {
        JavaStreamingContext(javaSparkContext, Duration(1000L))
    }
    val streamingContext by streamingContextDelegate


    val sparkSession by lazy {
        val builder = SparkSession.builder().sparkContext(sc)
        val isHiveCatalog = javaSparkContext.conf
            .get("spark.sql.catalogImplementation", "in-memory")
            .toLowerCase() == "hive"
        if (isHiveCatalog && `SparkSession$`.`MODULE$`.hiveClassesArePresent()) {
            val loader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
            if (loader.getResource("hive-site.xml") == null) {
                println("enable-hive-context is true but no hive-site.xml found on classpath")
            }
            builder.enableHiveSupport()
        } else {
            builder.config("spark.sql.catalogImplementation", "in-memory")
        }
        builder.orCreate
    }

    fun stop() {
        if (streamingContextDelegate.isInitialized()) {
            streamingContext.close()
        }
        javaSparkContext.stop()
    }

    fun addFile(path: String) {
        javaSparkContext.addFile(path)
    }

    fun addJar(path: String) {
        val localCopyDir = File(localTmpDir, "__adhoc__")
        if (!localCopyDir.isDirectory && !localCopyDir.mkdir()) {
            throw IOException("Failed to create directory to add temporary file")
        }
        val uri = URI(path)
        val name = File(uri.fragment ?: uri.path).name
        val localCopy = File(localCopyDir, name)
        if (localCopy.exists()) {
            throw IOException("A file with name $name has already been uploaded.")
        }
        FileSystem.get(uri, sc.hadoopConfiguration())
            .copyToLocalFile(Path(uri), Path(localCopy.toURI()))

        javaSparkContext.addJar(path)
    }

    fun addPyFile(path: String) = addJar(path)


}