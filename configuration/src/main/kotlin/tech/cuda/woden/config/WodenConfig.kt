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

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.io.Resources
import tech.cuda.woden.config.database.DatabaseConfig
import tech.cuda.woden.config.email.EmailConfig
import tech.cuda.woden.config.scheduler.SchedulerConfig

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@JsonRootName("woden")
data class WodenConfig(
    val database: DatabaseConfig,
    val email: EmailConfig,
    val scheduler: SchedulerConfig
)

private val mapper = XmlMapper().registerKotlinModule()
val Woden = mapper.readValue<WodenConfig>(Resources.getResource("woden.xml"))

