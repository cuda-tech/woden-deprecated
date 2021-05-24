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
package tech.cuda.woden.common.configuration.datasource

import com.zaxxer.hikari.HikariConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@Serializable
@SerialName("datasource")
data class DataSourceConfig(
    val name: String,
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
    val dbName: String,
    val minimumIdle: Int,
    val maximumPoolSize: Int
) {
    @Transient
    val hikariConfig = HikariConfig().also {
        it.jdbcUrl = "jdbc:mysql://${host}:${port}/${dbName}?characterEncoding=UTF-8&useAffectedRows=true"
        it.poolName = name
        it.username = username
        it.password = password
        it.minimumIdle = minimumIdle
        it.maximumPoolSize = maximumPoolSize
    }
}