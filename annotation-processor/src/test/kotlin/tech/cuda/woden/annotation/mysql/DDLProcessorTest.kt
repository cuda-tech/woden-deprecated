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
class DDLProcessorTest : AnnotationSpec() {

    @Test
    fun testCreateTableStatement() = NoisyLog.shutUp {
        val kotlinSource = SourceFile.kotlin("PersonDAO.kt", """
            package tech.cuda.woden.common.service.dao

            import tech.cuda.woden.annotation.mysql.*
            import tech.cuda.woden.common.service.po.PersonPO

            @STORE_IN_MYSQL
            internal object PersonDAO : Table<PersonPO>("person") {
                @BIGINT
                @AUTO_INCREMENT
                @PRIMARY_KEY
                @COMMENT("主键")
                val primaryKeyColumn = int("primary_key_column").primaryKey().bindTo { it.primaryKeyColumn }
                
                // 整型
                
                @BIGINT
                @COMMENT("bigint 类型")
                val bigintColumn = int("bigint_column").bindTo { it.bigintColumn }
                
                @BIGINT(25)
                @COMMENT("bigint25 类型")
                val bigint25Column = int("bigint25_column").bindTo { it.bigint25Column }
                
                @INT
                @COMMENT("int 类型")
                val intColumn = int("int_column").bindTo { it.intColumn }
                
                @INT(16)
                @COMMENT("int16 类型")
                val int16Column = int("int16_column").bindTo { it.int16Column }
                
                @MEDIUMINT
                @COMMENT("medium int 类型")
                val mediumIntColumn = int("medium_int_column").bindTo { it.mediumIntColumn }
                
                @MEDIUMINT(12)
                @COMMENT("medium12 int 类型")
                val medium12IntColumn = int("medium12_int_column").bindTo { it.medium12IntColumn }

                @SMALLINT
                @COMMENT("small int 类型")
                val smallIntColumn = int("small_int_column").bindTo { it.smallIntColumn }
                
                @SMALLINT(8)
                @COMMENT("small8 int 类型")
                val smallInt8Column = int("small_int8_column").bindTo { it.smallInt8Column }
                
                @TINYINT
                @COMMENT("tiny int 类型")
                val tinyIntColumn = int("tiny_int_column").bindTo { it.tinyIntColumn }
                
                @TINYINT(6)
                @COMMENT("tiny int6 类型")
                val tinyInt6Column = int("tiny_int6_column").bindTo { it.tinyInt6Column }
                
                // 字符类型
                
                @VARCHAR
                @COMMENT("varchar 类型")
                val varcharColumn = varchar("varchar_column").bindTo { it.varcharColumn }
                
                @VARCHAR(32)
                @COMMENT("enum 类型")
                val enumColumn = enum("enum_column", typeRef<EnumType>()).bindTo { it.enumColumn }
                
                @CHAR
                @COMMENT("char 类型")
                val charColumn = char("char_column").bindTo { it.charColumn }
                
                @CHAR(20)
                @COMMENT("char20 类型")
                val char20Column = char("char20_column").bindTo { it.charColumn }
                
                @TINYTEXT
                @COMMENT("tiny text 类型")
                val tinyTextColumn = tinytext("tiny_text_column").bindTo { it.tinyTextColumn }
                
                @TEXT
                @COMMENT("text 类型")
                val textColumn = text("text_column").bindTo { it.textColumn }
                
                @MEDIUMTEXT
                @COMMENT("medium text 类型")
                val mediumTextColumn = mediumtext("medium_text_column").bindTo { it.mediumTextColumn }
                
                @LONGTEXT
                @COMMENT("long text 类型")
                val longtextColumn = longtext("longtext_column").bindTo { it.longtextColumn }
                
                @TINYBLOB
                @COMMENT("tiny blob 类型")
                val tinyblobColumn = tinyblob("tiny_blob_column").bindTo { it.tinyblobColumn }
                
                @BLOB
                @COMMENT("blob 类型")
                val blobColumn = blob("blob_column").bindTo { it.blobColumn }
                
                @MEDIUMBLOB
                @COMMENT("medium blob 类型")
                val mediumblobColumn = mediumblob("medium_blob_column").bindTo { it.mediumblobColumn }
                
                @LONGBLOB
                @COMMENT("long blob 类型")
                val longblobColumn = longblob("long_blob_column").bindTo { it.longblobColumn }
 
                
                // 时间型
                @DATETIME
                @COMMENT("datetime 类型")
                val datetimeColumn = datetime("datetime_column").bindTo { it.datetimeColumn }
                
                @DATE
                @COMMENT("date 类型")
                val dateColumn = date("date_column").bindTo { it.dateColumn }
                
                @TIME
                @COMMENT("time 类型")
                val timeColumn = time("time_column").bindTo { it.timeColumn }
                
                @TIMESTAMP
                @COMMENT("timestamp 类型")
                val timestampColumn = timestamp("timestamp_column").bindTo { it.timestampColumn }
                
                @YEAR
                @COMMENT("year 类型")
                val yearColumn = year("year_column").bindTo { it.yearColumn }
                
                // 其它类型
                @BOOL
                @COMMENT("bool 类型")
                val boolColumn = bool("bool_column").bindTo { it.boolColumn }
                
                @JSON
                @COMMENT("json 类型")
                val jsonColumn = json("json_column").bindTo { it.jsonColumn }
                
                
                // 其它
                
                @INT
                @NOT_NULL
                @COMMENT("非空修饰符")
                val notNullColumn = int("not_null_column").bindTo { it.NotNullColumn }
                
                @INT
                @UNSIGNED
                @COMMENT("无符号修饰符")
                val unsignedColumn = int("unsignedColumn").bindTo {it.unsignedColumn}

                // 索引
                @INT
                @UNIQUE_INDEX
                @COMMENT("唯一索引")
                val uniqueIndexColumn = int("unique_index").bindTo { it.uniqueIndexColumn }

                @INT
                @UNIQUE_INDEX("bigint_column,int_column")
                @COMMENT("联合唯一索引")
                val unionUniqueIndexColumn = int("union_unique_index").bindTo { it.unionUniqueIndexColumn }

                @INT
                @INDEX
                @COMMENT("普通索引")
                val normalIndexColumn = int("normal_index").bindTo { it.normalIndexColumn }

                @INT
                @INDEX("bigint_column,int_column")
                @COMMENT("联合普通索引")
                val unionNormalIndexColumn = int("union_normal_index").bindTo { it.unionNormalIndexColumn }
            }
        """.trimIndent())

        val result = KotlinCompilation().apply {
            workingDir = Files.createTempDir().also { it.deleteOnExit() }
            sources = listOf(kotlinSource)
            annotationProcessors = listOf(DDLProcessor())
            inheritClassPath = true
            verbose = false
            suppressWarnings = true
        }.compile()
        result.messages.replace("\r\n", "\n") shouldContain """
            |  create table if not exists person(
            |  primary_key_column bigint(20) primary key auto_increment comment "主键",
            |  bigint_column bigint(20) comment "bigint 类型",
            |  bigint25_column bigint(25) comment "bigint25 类型",
            |  int_column int(11) comment "int 类型",
            |  int16_column int(16) comment "int16 类型",
            |  medium_int_column mediumint(9) comment "medium int 类型",
            |  medium12_int_column mediumint(12) comment "medium12 int 类型",
            |  small_int_column smallint(6) comment "small int 类型",
            |  small_int8_column smallint(8) comment "small8 int 类型",
            |  tiny_int_column tinyint(4) comment "tiny int 类型",
            |  tiny_int6_column tinyint(6) comment "tiny int6 类型",
            |  varchar_column varchar(16) comment "varchar 类型",
            |  enum_column varchar(32) comment "enum 类型",
            |  char_column char(16) comment "char 类型",
            |  char20_column char(20) comment "char20 类型",
            |  tiny_text_column tinytext comment "tiny text 类型",
            |  text_column text comment "text 类型",
            |  medium_text_column mediumtext comment "medium text 类型",
            |  longtext_column longtext comment "long text 类型",
            |  tiny_blob_column tinyblob comment "tiny blob 类型",
            |  blob_column blob comment "blob 类型",
            |  medium_blob_column mediumblob comment "medium blob 类型",
            |  long_blob_column longblob comment "long blob 类型",
            |  datetime_column datetime comment "datetime 类型",
            |  date_column date comment "date 类型",
            |  time_column time comment "time 类型",
            |  timestamp_column timestamp comment "timestamp 类型",
            |  year_column year comment "year 类型",
            |  bool_column bool comment "bool 类型",
            |  json_column json comment "json 类型",
            |  not_null_column int(11) not null comment "非空修饰符",
            |  unsignedColumn int(11) unsigned comment "无符号修饰符",
            |  unique_index int(11) comment "唯一索引",
            |  union_unique_index int(11) comment "联合唯一索引",
            |  normal_index int(11) comment "普通索引",
            |  union_normal_index int(11) comment "联合普通索引",
            |  unique(unique_index),
            |  unique(bigint_column,int_column,union_unique_index),
            |  index(normal_index),
            |  index(bigint_column,int_column,union_normal_index)
            |  )default charset=utf8mb4
        """.trimMargin()
    }

}