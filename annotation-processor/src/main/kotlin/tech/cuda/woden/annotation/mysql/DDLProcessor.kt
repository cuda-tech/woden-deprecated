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
package tech.cuda.woden.annotation.mysql

import com.google.auto.service.AutoService
import com.google.common.io.Files
import com.sun.tools.javac.api.JavacTrees
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Name
import org.apache.commons.lang3.StringEscapeUtils
import java.io.File
import java.lang.Exception
import java.nio.charset.Charset
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * STORE_IN_MYSQL 注解处理器，对于被其标记的类，将生成 ddl 属性
 * 由于 kotlin 的编译过程无法修改 java AST，也就无法直接修改最终生成的类
 * 所以我们通过生成一个扩展方法来近似地模拟
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@AutoService(Processor::class)
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("tech.cuda.woden.annotation.mysql.STORE_IN_MYSQL")
@SupportedOptions(DDLProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class DDLProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private lateinit var trees: JavacTrees

    override fun init(env: ProcessingEnvironment) = super.init(env).also {
        with(env as JavacProcessingEnvironment) {
            trees = JavacTrees.instance(context)
        }
    }

    /**
     * 因为 kotlin 的编译过程传过来的是只保留类结构的存根类(stubs)
     * 这个类里面所有方法的方法体为空，因此无法获取到 tableName，columnName 等属性值
     * 所以这里用 quick & dirty 的方法，直接读取源文件，正则提取出表名 & 列名
     */
    private fun Symbol.ClassSymbol.sourceCode(): String {
        val path = this.sourcefile.toUri().path
            .replace("build/tmp/kapt3/stubs/main", "src/main/kotlin")
            .replace(".java", ".kt")
        return Files.readLines(File(path), Charset.defaultCharset()).joinToString("\n")
    }

    private fun String.tableName(className: Name): String {
        val pattern = listOf("object", className, ":", "Table", "<", ".*", ">", "\\(", "\"(.*?)\"", "\\)").joinToString("\\s*", "\\s*", "\\s*")
        return Regex(pattern).find(this)?.groupValues?.lastOrNull() ?: throw Exception("无法解析 $className 的表名")
    }

    private fun String.columnName(varName: Name): String {
        val name = varName.toString().replace("\$annotations", "")
        val pattern = listOf("val", name, "=", ".*", "\\(", "\"(.*?)\"", ".*", "\\)", ".*", "\\.bindTo").joinToString("\\s*", "\\s*", "\\s*")
        return Regex(pattern).find(this)?.groupValues?.lastOrNull() ?: throw Exception("无法解析 $name 列名")
    }

    /**
     * 获取注解的值，如果没有，则返回提供的默认值
     */
    private fun List<JCTree.JCExpression>.orDefault(default: Any): String {
        this as com.sun.tools.javac.util.List
        if (this.isEmpty()) {
            return default.toString()
        }
        val value = StringEscapeUtils.unescapeJava((this.first() as JCTree.JCAssign).rhs.toString())
        return if (value.isBlank()) default.toString() else value
    }

    override fun process(annotations: MutableSet<out TypeElement>?, env: RoundEnvironment): Boolean {
        env.getElementsAnnotatedWith(STORE_IN_MYSQL::class.java).forEach { elem ->
            elem as Symbol.ClassSymbol
            val sourceCode = elem.sourceCode()
            val tableName = sourceCode.tableName(elem.name)

            trees.getTree(elem).accept(object : TreeTranslator() {
                override fun visitClassDef(clzz: JCTree.JCClassDecl) {
                    super.visitClassDef(clzz)
                    val ddl = clzz.defs.asSequence()
                        .filter { it.tag.name == "METHODDEF" }
                        .map { it as JCTree.JCMethodDecl }
                        .filter { it.name.endsWith("\$annotations") }
                        .map {
                            var autoIncrement = false
                            var isPrimaryKey = false
                            var isUnsigned = false
                            var isNotNull = false
                            var columnType: String? = null
                            var comment = ""
                            it.mods.annotations.forEach { annotation ->
                                when (annotation.type.toString()) {
                                    // 修饰符
                                    COMMENT::class.qualifiedName -> comment = annotation.args.orDefault("")
                                    AUTO_INCREMENT::class.qualifiedName -> autoIncrement = true
                                    NOT_NULL::class.qualifiedName -> isNotNull = true
                                    PRIMARY_KEY::class.qualifiedName -> isPrimaryKey = true
                                    UNSIGNED::class.qualifiedName -> isUnsigned = true

                                    // 整型
                                    TINYINT::class.qualifiedName -> columnType = "tinyint(${annotation.args.orDefault(4)})"
                                    SMALLINT::class.qualifiedName -> columnType = "smallint(${annotation.args.orDefault(6)})"
                                    MEDIUMINT::class.qualifiedName -> columnType = "mediumint(${annotation.args.orDefault(9)})"
                                    INT::class.qualifiedName -> columnType = "int(${annotation.args.orDefault(11)})"
                                    BIGINT::class.qualifiedName -> columnType = "bigint(${annotation.args.orDefault(20)})"

                                    // 浮点型

                                    // 字符型
                                    VARCHAR::class.qualifiedName -> columnType = "varchar(${annotation.args.orDefault(16)})"
                                    CHAR::class.qualifiedName -> columnType = "char(${annotation.args.orDefault(16)})"

                                    TINYTEXT::class.qualifiedName -> columnType = "tinytext"
                                    TEXT::class.qualifiedName -> columnType = "text"
                                    MEDIUMTEXT::class.qualifiedName -> columnType = "mediumtext"
                                    LONGTEXT::class.qualifiedName -> columnType = "longtext"

                                    TINYBLOB::class.qualifiedName -> columnType = "tinyblob"
                                    BLOB::class.qualifiedName -> columnType = "blob"
                                    MEDIUMBLOB::class.qualifiedName -> columnType = "mediumblob"
                                    LONGBLOB::class.qualifiedName -> columnType = "longblob"

                                    // 时间型
                                    DATETIME::class.qualifiedName -> columnType = "datetime"
                                    DATE::class.qualifiedName -> columnType = "date"
                                    TIME::class.qualifiedName -> columnType = "time"
                                    TIMESTAMP::class.qualifiedName -> columnType = "timestamp"
                                    YEAR::class.qualifiedName -> columnType = "year"

                                    // 其他
                                    BOOL::class.qualifiedName -> columnType = "bool"
                                    JSON::class.qualifiedName -> columnType = "json"
                                }
                            }
                            listOfNotNull(
                                sourceCode.columnName(it.name),
                                columnType,
                                if (isUnsigned) "unsigned" else null,
                                if (isNotNull) "not null" else null,
                                if (isPrimaryKey) "primary key" else null,
                                if (autoIncrement) "auto_increment" else null,
                                "comment", comment
                            ).joinToString(" ")
                        }.joinToString(",\n", "\"\"\"create table if not exists $tableName(\n", "\n)default charset=utf8mb4\"\"\"")

                    // 生成扩展属性
                    File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME], "${clzz.name}DDL.kt").apply {
                        parentFile.mkdirs()
                        writeText("""
                        package tech.cuda.woden.service.dao
                        import tech.cuda.woden.service.dao.${clzz.name}
                        internal val ${clzz.name}.DDL: String
                            get() = $ddl
                    """.trimIndent())
                    }
                }
            })
        }
        return true
    }

    private fun Any?.log() = processingEnv.messager.printMessage(javax.tools.Diagnostic.Kind.WARNING, "\n$this\n")
}
