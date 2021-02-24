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
package tech.cuda.woden.webserver

import com.google.common.collect.Maps

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object Response {

    fun withData(status: String, data: Any) = mapOf("status" to status, "data" to data)
    fun withMessage(status: String, message: Any) = mapOf("status" to status, "message" to message)
    fun withError(status: String, message: Any) = mapOf("status" to status, "error" to message)

    object Success {
        private const val status = "success"
        fun <K, V> data(vararg pairs: Pair<K, V>) = withData(status,
            if (pairs.isNotEmpty()) pairs.toMap(Maps.newLinkedHashMapWithExpectedSize(pairs.size))
            else Maps.newLinkedHashMap<K, V>()
        )

        fun message(message: Any) = withMessage(status, message)
    }

    object Failed {
        private const val status = "failed"
        fun WithError(error: Any) = withError(status, error)
        fun DataNotFound(data: Any?) = withError(status, "$data not found")
    }

}