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
package tech.cuda.woden.common.configuration.scheduler

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@Serializable
@SerialName("scheduler")
data class SchedulerConfig(
    val role: String,
    @SerialName("spark.home") private val _sparkHome: String?,
    @SerialName("anaconda.path") private val _anacondaPath: String?,
    @SerialName("bash.path") private val _bashPath: String?
) {
    @Transient
    val sparkHome: String = _sparkHome
        ?: System.getenv("SPARK_HOME")
        ?: System.getProperty("SPARK_HOME")

    @Transient
    val anacondaPath: String = _anacondaPath
        ?: System.getenv("ANACONDA_PATH")
        ?: System.getProperty("ANACONDA_PATH")
        ?: "/usr/bin/python3"

    @Transient
    val bashPath: String = _bashPath
        ?: System.getenv("BASH_PATH")
        ?: System.getProperty("BASH_PATH")
        ?: "/bin/bash"
}