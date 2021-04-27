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
package tech.cuda.woden.annotation.pojo

import com.google.common.io.Files
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.string.shouldContain
import tech.cuda.woden.annotation.NoisyLog

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class DTOProcessorTest : AnnotationSpec() {
    private val poSource = SourceFile.kotlin(
        "Example.kt", """
            package tech.cuda.woden.common.service.po
                        
            import java.time.LocalDateTime
                        
            interface Example {
                val id: Int
                val column1: Int
                val column2: String
                val column3: Set<Int>
                val column4: Map<String, Any>
                val column5: List<Int>?
                val isRemove: Boolean
                val createTime: LocalDateTime
                val updateTime: LocalDateTime
            }
        """.trimIndent()
    )

    @Test
    fun testDTOProcessForFullField() = NoisyLog.shutUp {
        val dtoSource = SourceFile.kotlin(
            "ExampleDTO.kt", """
            package tech.cuda.woden.common.service.dto
            
            import tech.cuda.woden.annotation.pojo.DTO
            import tech.cuda.woden.common.service.po.Example
            import java.time.LocalDateTime
            
            @DTO(Example::class)
            data class ExampleDTO(
                val id: Int,
                val column1: Int,
                val column2: String,
                val column3: Set<Int>,
                val column4: Map<String, Any>,
                val column5: List<Int>?,
                val createTime: LocalDateTime,
                val updateTime: LocalDateTime
             )
        """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            workingDir = Files.createTempDir().also { it.deleteOnExit() }
            sources = listOf(poSource, dtoSource)
            annotationProcessors = listOf(DTOProcessor())
            inheritClassPath = true
            verbose = false
            suppressWarnings = true
        }.compile()
        result.messages.replace("\r\n", "\n") shouldContain """
           |  import tech.cuda.woden.common.service.po.Example
           |  import tech.cuda.woden.common.service.dto.ExampleDTO
           |  
           |  internal fun tech.cuda.woden.common.service.po.Example.toExampleDTO() = ExampleDTO(
           |      id = this.id,
           |      column1 = this.column1,
           |      column2 = this.column2,
           |      column3 = this.column3,
           |      column4 = this.column4,
           |      column5 = this.column5,
           |      createTime = this.createTime,
           |      updateTime = this.updateTime
           |  )
        """.trimMargin()
    }

    @Test
    fun testDTOProcessForPartialField() = NoisyLog.shutUp {
        val dtoSource = SourceFile.kotlin(
            "ExampleDTO.kt", """
            package tech.cuda.woden.common.service.dto
            
            import tech.cuda.woden.annotation.pojo.DTO
            import tech.cuda.woden.common.service.po.Example
            import java.time.LocalDateTime
            
            @DTO(Example::class)
            data class ExampleDTO(
                val id: Int,
                val column1: Int
             )
        """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            workingDir = Files.createTempDir().also { it.deleteOnExit() }
            sources = listOf(poSource, dtoSource)
            annotationProcessors = listOf(DTOProcessor())
            inheritClassPath = true
            verbose = false
            suppressWarnings = true
        }.compile()
        result.messages.replace("\r\n", "\n") shouldContain """
           |  import tech.cuda.woden.common.service.po.Example
           |  import tech.cuda.woden.common.service.dto.ExampleDTO
           |  
           |  internal fun tech.cuda.woden.common.service.po.Example.toExampleDTO() = ExampleDTO(
           |      id = this.id,
           |      column1 = this.column1
           |  )
        """.trimMargin()
    }

    @Test
    fun testDTOProcessWithExtendedField() = NoisyLog.shutUp {
        val dtoSource = SourceFile.kotlin(
            "ExampleDTO.kt", """
            package tech.cuda.woden.common.service.dto
            
            import tech.cuda.woden.annotation.pojo.DTO
            import tech.cuda.woden.common.service.po.Example
            import java.time.LocalDateTime
            
            @DTO(Example::class)
            data class ExampleDTO(
                val id: Int,
                val column1: Int,
                val column100: Set<Int>
             )
        """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            workingDir = Files.createTempDir().also { it.deleteOnExit() }
            sources = listOf(poSource, dtoSource)
            annotationProcessors = listOf(DTOProcessor())
            inheritClassPath = true
            verbose = false
            suppressWarnings = true
        }.compile()
        result.messages.replace("\r\n", "\n") shouldContain """
           |  import tech.cuda.woden.common.service.po.Example
           |  import tech.cuda.woden.common.service.dto.ExampleDTO
           |  
           |  internal fun tech.cuda.woden.common.service.po.Example.toExampleDTOWith(column100: Set<java.lang.Integer>) = ExampleDTO(
           |      id = this.id,
           |      column1 = this.column1,
           |      column100 = column100
           |  )
        """.trimMargin()
    }
}