package tech.cuda.datahub.scheduler.livy.mocker

import tech.cuda.datahub.scheduler.exception.LivyException
import kotlin.reflect.full.memberProperties

object StatementExample {
    data class Example(val code: String, val stdout: String? = null, val stderr: String? = null, val errorName: String? = null)

    fun findByCode(code: String): Example? {
        val statement = this::class.memberProperties.map {
            val example = it.getter.call(this)
            if (example is Example && example.code == code) {
                example
            } else {
                null
            }
        }.filterNotNull()
        if (statement.size > 1) {
            throw LivyException("statement example has duplicate code $code")
        } else {
            return statement.firstOrNull()
        }
    }

    val SQL_SELECT_HELLO_WORLD = Example(
        code = """
            select "hello world"
        """.trimIndent(),
        stdout = """
            {"schema":{"type":"struct","fields":[{"name":"hello world","type":"string","nullable":false,"metadata":{}}]},"data":[["hello world"]]}
        """.trimIndent()
    )

    val SQL_SELECT_PRIMARY_TYPE = Example(
        code = """
            select 
                1 as integer_col,
                'a' as string_col,
                1.0 as decimal_col,
                named_struct('a', 1, 'b', 2) as struct_col,
                array(1, 2, 3) as array_col
            union all
            select 
                2 as integer_col,
                'b' as string_col,
                2.0 as decimal_col,
                named_struct('a', 3, 'b', 4) as struct_col,
                array(4, 5, 6) as array_col
        """.trimIndent(),
        stdout = """
            {"schema":{"type":"struct","fields":[{"name":"integer_col","type":"integer","nullable":false,"metadata":{}},{"name":"string_col","type":"string","nullable":false,"metadata":{}},{"name":"decimal_col","type":"decimal(2,1)","nullable":false,"metadata":{}},{"name":"struct_col","type":{"type":"struct","fields":[{"name":"a","type":"integer","nullable":false,"metadata":{}},{"name":"b","type":"integer","nullable":false,"metadata":{}}]},"nullable":false,"metadata":{}},{"name":"array_col","type":{"type":"array","elementType":"integer","containsNull":false},"nullable":false,"metadata":{}}]},"data":[[1,"a",1.0,{"schema":[{"name":"a","dataType":{},"nullable":false,"metadata":{"map":{}}},{"name":"b","dataType":{},"nullable":false,"metadata":{"map":{}}}],"values":[1,2]},[1,2,3]],[2,"b",2.0,{"schema":[{"name":"a","dataType":{},"nullable":false,"metadata":{"map":{}}},{"name":"b","dataType":{},"nullable":false,"metadata":{"map":{}}}],"values":[3,4]},[4,5,6]]]}
        """.trimIndent()

    )

    val SQL_SELECT_FROM_NOT_EXISTS_TABLE = Example(
        code = "select 1 from not_exist_table",
        stderr = "Table or view not found: not_exist_table; line 1 pos 14",
        errorName = "Error"
    )

    val SQL_SLEEP = Example(
        code = "select reflect('java.lang.Thread', 'sleep', bigint(5000))",
        stdout = ""
    )

    val SPARK_WORD_COUNT = Example(
        code = """
            sc.parallelize(Array(
                "apple apple facebook microsoft apple microsoft google apple google google",
                "alibaba tencent alibaba alibaba"
            )).flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey((a, b) => a + b).map(a => a._2).reduce((a, b) => a + b)
        """.trimIndent(),
        stdout = "res0: Int = 14\n"
    )

    val SPARK_NOT_EXISTS_VARIABLE = Example(
        code = "notExistsVariableInSpark",
        stderr = "<console>:24: error: not found: value notExistsVariableInSpark",
        errorName = "Error"
    )

    val SPARK_SLEEP = Example(
        code = "Thread.sleep(5000)",
        stdout = ""
    )

    val PY_SPARK_WORD_COUNT = Example(
        code = """
            sc.parallelize([
                'apple apple facebook microsoft apple microsoft google apple google google',
                'alibaba tencent alibaba alibaba'
            ]).flatMap(lambda line: line.split(' ')) \
            .map(lambda word: (word, 1)) \
            .reduceByKey(lambda a, b: a + b) \
            .map(lambda a: a[1]) \
            .reduce(lambda a, b: a + b)
        """.trimIndent(),
        stdout = "14"
    )

    val PY_SPARK_NOT_EXISTS_VARIABLE = Example(
        code = "print(notExistsVariable)",
        stderr = "name 'notExistsVariable' is not defined",
        errorName = "NameError"
    )

    val PY_SPARK_SLEEP = Example(
        code = "import time; time.sleep(5); print('hello world')",
        stdout = ""
    )

    val SPARK_R_WORDCOUNT = Example(
        code = """
            options(warn=-1)
            df <- as.DataFrame(list(
                "apple apple facebook microsoft apple microsoft google apple google google",
                "alibaba tencent alibaba alibaba"
            ), structType(structField("line", "string")))
            createOrReplaceTempView(df, "file")
            head(sql("select count(*) from ( select explode(split(line, ' ')) as word from file) as t "))[[1]]
        """.trimIndent(),
        stdout = "[1] 14"
    )

    val SPARK_R_NOT_EXISTS_VARIABLE = Example(
        code = "notExistsVariableInSparkR",
        stderr = "[1] \"Error in eval(parse(text = \\\"notExistsVariableInSparkR\\\")): object 'notExistsVariableInSparkR' not found\"",
        errorName = "Error"
    )

    val SPARK_R_SLEEP = Example(
        code = "Sys.sleep(5)\n12345",
        stdout = ""
    )

}