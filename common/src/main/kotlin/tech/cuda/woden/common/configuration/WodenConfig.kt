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
package tech.cuda.woden.common.configuration

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import tech.cuda.woden.common.configuration.datasource.DataSourceConfig
import tech.cuda.woden.common.configuration.email.EmailConfig
import tech.cuda.woden.common.configuration.scheduler.SchedulerConfig
import java.sql.DriverManager
import javax.sql.DataSource

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@Serializable
data class WodenConfig(
    @SerialName("datasource") private val datasourceConfig: DataSourceConfig,
    val email: EmailConfig,
    val scheduler: SchedulerConfig
) {
    val datasource: DataSource by lazy {
        try {
            HikariDataSource(datasourceConfig.hikariConfig)
        } catch (e: HikariPool.PoolInitializationException) {
            if (e.message == "Failed to initialize pool: Unknown database '${datasourceConfig.dbName}'") {
                val urlWithoutDbName = datasourceConfig.hikariConfig.jdbcUrl.replace(datasourceConfig.dbName, "")
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(urlWithoutDbName, datasourceConfig.username, datasourceConfig.password).use {
                    it.createStatement().use { statement ->
                        statement.execute("create database if not exists ${datasourceConfig.dbName} default character set = 'utf8mb4'")
                    }
                }
                HikariDataSource(datasourceConfig.hikariConfig)
            } else {
                throw e
            }
        }
    }
}

val Woden = Hocon.decodeFromConfig(
    WodenConfig.serializer(),
    ConfigFactory.parseResources("woden.conf")
)
