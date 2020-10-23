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

import com.fasterxml.jackson.annotation.JsonRootName
import java.util.*

/**
 * 数据库配置文件，用于获取数据库信息 & 生成 Druid 需要的 Properties
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@JsonRootName("database")
class DatabaseConfig(
    val mysql: MySQLConfig,
    val druid: DruidConfig
) {
    val properties: Properties
        get() = Properties().also { props ->
            props["name"] = druid.name
            props["url"] = "jdbc:mysql://${mysql.host}:${mysql.port}/${mysql.dbName}?characterEncoding=${mysql.encoding}"
            props["username"] = mysql.username
            mysql.password?.let { props["password"] = mysql.password }
            druid.initialSize?.let { props["initialSize"] = druid.initialSize.toString() }
            druid.maxActive?.let { props["maxActive"] = druid.maxActive.toString() }
            druid.minIdle?.let { props["minIdle"] = druid.minIdle.toString() }
            druid.maxWait?.let { props["maxWait"] = druid.maxWait.toString() }
            druid.connectionInitSqls?.let { props["connectionInitSqls"] = druid.connectionInitSqls.toString() }
            druid.poolPreparedStatements?.let { props["poolPreparedStatements"] = druid.poolPreparedStatements.toString() }
            druid.maxPoolPreparedStatementPerConnectionSize?.let {
                props["maxPoolPreparedStatementPerConnectionSize"] = druid.maxPoolPreparedStatementPerConnectionSize.toString()
            }
            druid.validationQuery?.let { props["validationQuery"] = druid.validationQuery.toString() }
            druid.validationQueryTimeout?.let { props["validationQueryTimeout"] = druid.validationQueryTimeout }
            druid.testOnBorrow?.let { props["testOnBorrow"] = druid.testOnBorrow }
            druid.testOnReturn?.let { props["testOnReturn"] = druid.testOnReturn }
            druid.testWhileIdle?.let { props["testWhileIdle"] = druid.testWhileIdle }
            druid.timeBetweenEvictionRunsMillis?.let {
                props["timeBetweenEvictionRunsMillis"] = druid.timeBetweenEvictionRunsMillis
            }
            druid.keepAlive?.let { props["keepAlive"] = druid.keepAlive }
            druid.minEvictableIdleTimeMillis?.let { props["minEvictableIdleTimeMillis"] = druid.minEvictableIdleTimeMillis }
            druid.filters?.let { props["filters"] = druid.filters }

            // 阿里的垃圾代码会将你的 value 先转成 String 然后再转回对应的类型，而且转 String 这个过程用的是强转...
            // 所以你得把 value 都转成 String，否则就会抛出 ClassCastException
            // 脱裤子放屁，迷惑。
            props.keys.forEach { key -> props.setProperty(key.toString(), props[key].toString()) }
        }

    val dbNotExistsproperties: Properties
        get() = Properties().also { props ->
            props["url"] = "jdbc:mysql://${mysql.host}:${mysql.port}/?characterEncoding=${mysql.encoding}"
            props["username"] = mysql.username
            mysql.password?.let { props["password"] = mysql.password }
        }
}
