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

import com.google.auto.service.AutoService
import com.sun.tools.javac.api.JavacTrees
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeTranslator
import tech.cuda.woden.annotation.mysql.DDLProcessor
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@AutoService(Processor::class)
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedOptions(DTOProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class DTOProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private val isUnitTest = System.getProperty("WODEN_UNITTEST") == "true"

    private lateinit var trees: JavacTrees

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(DTO::class.java.canonicalName)
    }

    override fun init(env: ProcessingEnvironment) = super.init(env).also {
        with(env as JavacProcessingEnvironment) {
            trees = JavacTrees.instance(context)
        }
    }

    override fun process(annotations: MutableSet<out TypeElement>?, env: RoundEnvironment): Boolean {
        env.getElementsAnnotatedWith(DTO::class.java).forEach { elem ->
            elem as Symbol.ClassSymbol
            val poCanonicalName = elem.rawAttributes.first {
                it.type.toString() == DTO::class.java.canonicalName
            }.values.first().snd.toString().replace(".class", "")

            trees.getTree(elem).accept(object : TreeTranslator() {
                override fun visitClassDef(clzz: JCTree.JCClassDecl) {
                    super.visitClassDef(clzz)
                    val neededMembers = clzz.defs.asSequence()
                        .filter { it.tag.name == "VARDEF" }
                        .map {
                            val neededMember = (it as JCTree.JCVariableDecl).name.toString()
                            "$neededMember = this.$neededMember"
                        }.joinToString(",\n    ")

                    val extendFunction = """
                        |package ${elem.packge()}
                        |import $poCanonicalName
                        |import ${elem.packge()}.${clzz.name}
                        |internal fun $poCanonicalName.to${clzz.name}() = ${clzz.name}(
                        |    $neededMembers
                        |)
                    """.trimMargin()

                    if (isUnitTest) {
                        processingEnv.messager.printMessage(javax.tools.Diagnostic.Kind.NOTE, extendFunction)
                    } else {
                        File(processingEnv.options[DDLProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME], "${clzz.name}Util.kt").apply {
                            parentFile.mkdirs()
                            writeText(extendFunction)
                        }
                    }
                }
            })
        }
        return true
    }

    private fun Any?.log() = processingEnv.messager.printMessage(javax.tools.Diagnostic.Kind.WARNING, "\n$this\n")
}