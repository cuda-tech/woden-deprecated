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
package tech.cuda.woden.common.service.po.dtype

import java.time.LocalDateTime

/**
 * 文件类型
 * @author Jensen Qi  <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
enum class FileType {
    SPARK_SQL,
    SPARK_SHELL,
    PY_SPARK,
    MR,
    ANACONDA,
    BASH;

    fun initVal(author: String, createTime: LocalDateTime) =
        """
            ## @author               $author
            ## @date                 $createTime
            ## @describe
        """.trimIndent()
}
