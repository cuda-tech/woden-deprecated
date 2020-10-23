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
package tech.cuda.woden.config.database

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@JsonRootName("druid")
data class DruidConfig(
    val name: String, // Druid 监控名称

    @JsonProperty("initial-size")
    val initialSize: Int?, // 初始化时建立物理连接的个数

    @JsonProperty("max-active")
    val maxActive: Int?, // 最大连接池数量

    @JsonProperty("min-active")
    val minIdle: Int?, // 最小连接池数量

    @JsonProperty("max-wait")
    val maxWait: Int?, // 获取连接时最大等待时间，单位毫秒

    @JsonProperty("connection-init-sqls")
    val connectionInitSqls: String?, // 物理连接初始化的时候执行的sql

    @JsonProperty("pool-prepared-statements")
    val poolPreparedStatements: Boolean?, // 是否使用 PSCache

    @JsonProperty("max-pool-prepared-statement-per-connection-size")
    val maxPoolPreparedStatementPerConnectionSize: Int?, // PsCache 最大数量, 若启用 PSCache则此参数必须 > 0

    @JsonProperty("validation-query")
    val validationQuery: String?, // 用来检测连接是否有效的 SQL

    @JsonProperty("validation-query-timeout")
    val validationQueryTimeout: Int?, // 检测连接是否有效的超时时间，单位秒

    @JsonProperty("test-on-borrow")
    val testOnBorrow: Boolean?, // 是否在申请连接时执行 validationQuery 检测连接有效性

    @JsonProperty("test-on-return")
    val testOnReturn: Boolean?, // 是否在归还连接时执行 validationQuery 检测连接有效性

    @JsonProperty("test-while-idle")
    val testWhileIdle: Boolean?, // 是否在空闲时间大于 timeBetweenEvictionRunsMillis 时执行 validationQuery 检测连接有效性

    @JsonProperty("time-between-eviction-runs-millis")
    val timeBetweenEvictionRunsMillis: Int?, // testWhileIdle 判据，单位分钟

    @JsonProperty("keep-alive")
    val keepAlive: Boolean?, // 是否在连接池的连接数量 < minIdle 且空闲时间 > minEvictableIdleTimeMillis 时执行keepAlive操作

    @JsonProperty("min-evictable-idle-time-millis")
    val minEvictableIdleTimeMillis: Int?, // keepAlive 判据，单位分钟

    val filters: String? // 扩展插件，可选值 filter:stat (用户监控统计)、 filter:log4j (用于日志)、 filter:wall (用于防御sql注入)

)