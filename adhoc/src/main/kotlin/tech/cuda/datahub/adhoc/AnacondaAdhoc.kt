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
package tech.cuda.datahub.adhoc

import tech.cuda.datahub.config.Datahub

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class AnacondaAdhoc(
    code: String,
    arguments: List<String> = listOf(),
    kvArguments: Map<String, String> = mapOf()
) : AbstractBashAdhoc(
    executorPath = Datahub.scheduler.anacondaPath,
    code = """
        import sys, io
        from functools import partial
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, newline='\n') # 避免在 Windows 下换行符自动转为 \r\n
        print = partial(print, flush = True) # 为了将 print 实时地输出到 std
    """.trimIndent() + "\n" + code,
    arguments = arguments,
    kvArguments = kvArguments
)