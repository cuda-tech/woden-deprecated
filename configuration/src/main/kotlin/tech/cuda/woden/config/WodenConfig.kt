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
package tech.cuda.woden.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.io.Resources
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import tech.cuda.woden.config.datasource.DataSourceConfig
import tech.cuda.woden.config.email.EmailConfig
import tech.cuda.woden.config.scheduler.SchedulerConfig
import java.sql.DriverManager
import javax.sql.DataSource

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@JsonRootName("woden")
data class WodenConfig(
    @JsonProperty("datasource")
    private val datasourceConfig: DataSourceConfig,
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

private val mapper = XmlMapper().registerKotlinModule()
val Woden = mapper.readValue<WodenConfig>(Resources.getResource("woden.xml"))

