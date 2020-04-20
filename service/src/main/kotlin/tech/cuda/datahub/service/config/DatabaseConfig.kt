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

import java.util.*

/**
 * 数据库配置文件，用于获取数据库信息 & 生成 Druid 需要的 Properties
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
data class DatabaseConfig(
    val username: String = "root",
    val password: String = "root",
    val host: String = "localhost",
    val port: Int = 3306,
    val encoding: String = "UTF-8",
    val dbName: String = "datahub",
    val initialSize: Int = 5
) {
    val properties: Properties
        get() = Properties().also { props ->
            props["url"] = "jdbc:mysql://$host:$port/?characterEncoding=$encoding"
            props["username"] = username
            password?.let { props["password"] = password }
            props["initial-size"] = initialSize
        }
}
