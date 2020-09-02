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
package tech.cuda.datahub.scheduler.livy.statement

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@Suppress("UNCHECKED_CAST")
class StatementOutput(datatype: String, json: Map<String, Any>) {
    val status = json["status"] as String?
    val executionCount = json["execution_count"] as Int?
    val errorName = json["ename"] as String?
    val errorValue = json["evalue"] as String?
    val traceback = json["traceback"] as List<*>?

    val stdout: String = if (datatype == "text/plain") {
        json.getOrDefault("data", mapOf(datatype to "")).asMap()
            .getOrDefault(datatype, "").toString()
    } else { // json
        ObjectMapper().registerKotlinModule().writeValueAsString(
            json.getOrDefault("data", mapOf(datatype to mapOf<String, Any>())).asMap()
                .getOrDefault(datatype, mapOf<String, Any>())
        )
    }

    private fun Any.asMap(): Map<String, Any> = this as Map<String, Any>
    private fun Any.asList(): List<Any> = this as List<Any>
}