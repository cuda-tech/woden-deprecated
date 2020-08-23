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

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@Suppress("UNCHECKED_CAST")
class SqlStatementOutput(json: Map<String, Any>) : StatementOutput(json) {

    data class Field(val name: String, val type: String)

    val fields: List<Field>
    val df: List<List<Any>>

    init {
        val jsonData = json.getOrDefault("data", mapOf("application/json" to mapOf<String, Any>())).asMap()
            .getOrDefault("application/json", mapOf<String, Any>()).asMap()
        fields = jsonData.getOrDefault("schema", mapOf("fields" to listOf<Map<*, *>>()))
            .asMap().getOrDefault("fields", listOf<Map<*, *>>())
            .asList().map {
                it as Map<String, Any>
                Field(it["name"].toString(), it["type"].toString())
            }
        df = jsonData.getOrDefault("data", listOf(listOf<Any>())) as List<List<Any>>
    }

    override fun toString(): String {
        return mapOf(
            "fields" to fields,
            "df" to df,
            "status" to status,
            "execution_count" to executionCount,
            "error_name" to errorName,
            "error_value" to "\n$errorValue\n",
            "traceback" to traceback?.joinToString("\n", "\n", "\n")
        ).toString()
    }


}