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

import org.apache.livy.LivyClientBuilder
import tech.cuda.datahub.service.dto.TaskDTO
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.net.URI

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class SparkSqlOperator(task: TaskDTO) : HadoopBaseOperator(task, "hive") {
    fun submit() {
        val tempFile = File.createTempFile("hql_", ".temp").also {
            val writer = BufferedWriter(FileWriter(it))
            writer.write(commands)
            writer.flush()
            writer.close()
        }

        val livyUrl = URI("localhost")
        val livyClient = LivyClientBuilder()
            .setURI(livyUrl)
            .build()
        val job = livyClient.submit {

        }



//        val ret = CliDriver().run(arrayOf(
//            "-f", "./src/test/java/test.hive",
//            "-hiveconf", "hive.exec.scratchdir=file:///C:/tmp"
//        ))
    }

}