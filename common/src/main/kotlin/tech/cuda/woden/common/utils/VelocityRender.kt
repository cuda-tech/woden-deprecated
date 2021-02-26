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
package tech.cuda.woden.common.utils

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import java.io.StringWriter
import java.time.LocalDate

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object VelocityRender {
    private const val TEMPLATE_NAME = "WODEN"
    private val velocityEngine = VelocityEngine().also {
        it.setProperty(Velocity.RESOURCE_LOADERS, "string");
        it.setProperty("resource.loader.string.class", StringResourceLoader::class.java.name)
        it.init()
    }
    private val repository = StringResourceLoader.getRepository()

    fun render(template: String, bizdate: LocalDate = LocalDate.now()): String {
        repository.putStringResource(TEMPLATE_NAME, template)
        val context = VelocityContext().also {
            it.put("bizdate", bizdate)
        }
        return StringWriter().also {
            velocityEngine.getTemplate(TEMPLATE_NAME).merge(context, it)
        }.toString()
    }
}