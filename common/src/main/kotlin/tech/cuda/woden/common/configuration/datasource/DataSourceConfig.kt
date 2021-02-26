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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
import com.zaxxer.hikari.HikariConfig

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@JsonRootName("datasource")
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
    @JsonIgnore
    val hikariConfig = HikariConfig().also {
        it.jdbcUrl = "jdbc:mysql://${host}:${port}/${dbName}?characterEncoding=UTF-8"
        it.poolName = name
        it.username = username
        it.password = password
        it.minimumIdle = minimumIdle
        it.maximumPoolSize = maximumPoolSize
    }
}