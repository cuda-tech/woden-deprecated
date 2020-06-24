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
package tech.cuda.datahub.service.config

import com.alibaba.druid.filter.Filter
import java.util.*

/**
 * 数据库配置文件，用于获取数据库信息 & 生成 Druid 需要的 Properties
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
data class DatabaseConfig(
    // 数据库基础配置
    val username: String = "root",
    val password: String? = "root",
    val host: String = "localhost",
    val port: Int = 3306,
    val encoding: String = "UTF-8",
    val dbName: String = "datahub",

    // Druid 特有配置，具体见文档 https://github.com/alibaba/druid/wiki 的 DruidDataSource 配置属性列表章节
    val name: String = "DataHubService", // Druid 监控名称
    val initialSize: Int? = 1, // 初始化时建立物理连接的个数
    val maxActive: Int? = null, // 最大连接池数量
    val minIdle: Int? = null, // 最小连接池数量
    val maxWait: Int? = null, // 获取连接时最大等待时间，单位毫秒
    val connectionInitSqls: String? = null, // 物理连接初始化的时候执行的sql
    val poolPreparedStatements: Boolean? = null, // 是否使用 PSCache
    val maxPoolPreparedStatementPerConnectionSize: Int? = null, // PsCache 最大数量, 若启用 PSCache则此参数必须 > 0
    val validationQuery: String? = null, // 用来检测连接是否有效的 SQL
    val validationQueryTimeout: Int? = null, // 检测连接是否有效的超时时间，单位秒
    val testOnBorrow: Boolean? = null, // 是否在申请连接时执行 validationQuery 检测连接有效性
    val testOnReturn: Boolean? = null, // 是否在归还连接时执行 validationQuery 检测连接有效性
    val testWhileIdle: Boolean? = null, // 是否在空闲时间大于 timeBetweenEvictionRunsMillis 时执行 validationQuery 检测连接有效性
    val timeBetweenEvictionRunsMillis: Int? = null, // testWhileIdle 判据，单位分钟
    val keepAlive: Boolean? = null, // 是否在连接池的连接数量 < minIdle 且空闲时间 > minEvictableIdleTimeMillis 时执行keepAlive操作
    val minEvictableIdleTimeMillis: Int? = null, // keepAlive 判据，单位分钟
    val filters: String? = null, // 扩展插件，可选值 filter:stat (用户监控统计)、 filter:log4j (用于日志)、 filter:wall (用于防御sql注入)
    val proxyFilters: List<Filter>? = null // 同时配置了 filters 和 proxyFilters，是组合关系
) {
    val properties: Properties
        get() = Properties().also { props ->
            props["name"] = name
            props["url"] = "jdbc:mysql://$host:$port/?characterEncoding=$encoding"
            props["username"] = username
            password?.let { props["password"] = password }
            initialSize?.let { props["initialSize"] = initialSize.toString() }
            maxActive?.let { props["maxActive"] = maxActive.toString() }
            minIdle?.let { props["minIdle"] = minIdle.toString() }
            maxWait?.let { props["maxWait"] = maxWait.toString() }
            connectionInitSqls?.let { props["connectionInitSqls"] = connectionInitSqls.toString() }
            poolPreparedStatements?.let { props["poolPreparedStatements"] = poolPreparedStatements.toString() }
            maxPoolPreparedStatementPerConnectionSize?.let {
                props["maxPoolPreparedStatementPerConnectionSize"] = maxPoolPreparedStatementPerConnectionSize.toString()
            }
            validationQuery?.let { props["validationQuery"] = validationQuery.toString() }
            validationQueryTimeout?.let { props["validationQueryTimeout"] = validationQueryTimeout }
            testOnBorrow?.let { props["testOnBorrow"] = testOnBorrow }
            testOnReturn?.let { props["testOnReturn"] = testOnReturn }
            testWhileIdle?.let { props["testWhileIdle"] = testWhileIdle }
            timeBetweenEvictionRunsMillis?.let {
                props["timeBetweenEvictionRunsMillis"] = timeBetweenEvictionRunsMillis
            }
            keepAlive?.let { props["keepAlive"] = keepAlive }
            minEvictableIdleTimeMillis?.let { props["minEvictableIdleTimeMillis"] = minEvictableIdleTimeMillis }
            filters?.let { props["filters"] = filters }
            proxyFilters?.let { props["proxyFilters"] = proxyFilters }

            // 阿里的垃圾代码会将你的 value 先转成 String 然后再转回对应的类型，而且转 String 这个过程用的是强转...
            // 所以你得把 value 都转成 String，否则就会抛出 ClassCastException
            // 脱裤子放屁，迷惑。
            props.keys.forEach { key -> props.setProperty(key.toString(), props[key].toString()) }
        }
}
