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
package tech.cuda.datahub.scheduler.ops

import tech.cuda.datahub.service.dto.TaskDTO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class PythonOperator(task: TaskDTO, type: String = "python") : BashBaseOperator(task, type) {
    private val initScript = """
        from functools import partial
        print = partial(print, flush = True) # 为了将 print 实时地输出到 std
    """.trimIndent()

    override val commands: String
        get() = "$initScript\n${mirror?.content ?: ""}"
}