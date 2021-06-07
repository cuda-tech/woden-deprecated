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
package tech.cuda.woden.scheduler.runner

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class SparkSQLRunnerTest : AnnotationSpec() {

    @Test
    fun testErrorStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner(code = "select not_exists_column")
        job.startAndJoin()
        job.status shouldBe RunnerStatus.FAILED
        job.output shouldContain "Error in query: cannot resolve '`not_exists_column`' given input columns: []; line 1 pos 7;"
        job.close()
    }

    @Test
    fun testNotExistsTableStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner(code = "select 1 from not_exists_table")
        job.startAndJoin()
        job.status shouldBe RunnerStatus.FAILED
        job.output shouldContain "Error in query: Table or view not found: not_exists_table; line 1 pos 14"
        job.close()
    }

    @Test
    fun testSelectStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner("""
            select
                1 as int_column,
                1.234 as double_col,
                from_unixtime(unix_timestamp('2020-09-27 01:38:00')) as datetime_col,
                "hello world" as string_col,
                array(1, 2, 3) as list_col,
                map("a", 1, "b", 2) as map_col,
                named_struct("c", 3, "d", 4) as struct_col
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe RunnerStatus.SUCCESS
        job.output shouldContain """
            int_column	double_col	datetime_col	string_col	list_col	map_col	struct_col
            1	1.234	2020-09-27 01:38:00	hello world	[1,2,3]	{"a":1,"b":2}	{"c":3,"d":4}
        """.trimIndent()
        job.close()
    }

    @Test
    fun testUnionAllStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner("""
            set spark.sql.crossJoin.enabled = true;
            select
                1 as int_column,
                1.234 as double_col,
                from_unixtime(unix_timestamp('2020-09-27 01:38:00'))  as datetime_col,
                "hello world" as string_col,
                array(1, 2, 3) as list_col,
                map("a", 1, "b", 2) as map_col,
                named_struct("c", 3, "d", 4) as struct_col
            union all
            select
                2 as int_column,
                5.678 as double_col,
                from_unixtime(unix_timestamp('2020-09-27 01:58:26')) as datetime_col,
                "show me the code" as string_col,
                array(4, 5, 6) as list_col,
                map("x", 5, "y", 6) as map_col,
                named_struct("c", 7, "d", 8) as struct_col
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe RunnerStatus.SUCCESS
        job.output shouldContain """
            int_column	double_col	datetime_col	string_col	list_col	map_col	struct_col
            1	1.234	2020-09-27 01:38:00	hello world	[1,2,3]	{"a":1,"b":2}	{"c":3,"d":4}
            2	5.678	2020-09-27 01:58:26	show me the code	[4,5,6]	{"x":5,"y":6}	{"c":7,"d":8}
        """.trimIndent()
        job.close()
    }

    @Test
    fun testGroupByStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner("""
            select
                int_column,
                sum(double_col) as sum_double_col,
                count(*) as col_cnt
            from(
                select
                    1 as int_column,
                    1.234 as double_col,
                    from_unixtime(1601141880) as datetime_col,
                    "hello world" as string_col,
                    array(1, 2, 3) as list_col,
                    map("a", 1, "b", 2) as map_col,
                    named_struct("c", 3, "d", 4) as struct_col
                union all
                select
                    2 as int_column,
                    5.678 as double_col,
                    from_unixtime(1601143106) as datetime_col,
                    "show me the code" as string_col,
                    array(4, 5, 6) as list_col,
                    map("x", 5, "y", 6) as map_col,
                    named_struct("c", 7, "d", 8) as struct_col
                union all
                select
                    2 as int_column,
                    5.678 as double_col,
                    from_unixtime(1601143106) as datetime_col,
                    "show me the code" as string_col,
                    array(4, 5, 6) as list_col,
                    map("x", 5, "y", 6) as map_col,
                    named_struct("c", 7, "d", 8) as struct_col
            ) as t
            group by int_column
            order by int_column
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe RunnerStatus.SUCCESS
        job.output shouldContain """
            int_column	sum_double_col	col_cnt
            1	1.234	1
            2	11.356	2
        """.trimIndent()
        job.close()
    }

    @Test
    fun testInnerJoinStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner("""
            select
                t1.int_column,
                t1.string_col as t1_string,
                t2.string_col as t2_string
            from (
                select
                    1 as int_column,
                    "hello world" as string_col
                union all
                select
                    2 as int_column,
                    "show me the code" as string_col
            ) as t1 join (
                select
                    1 as int_column,
                    "hello world!" as string_col
            ) as t2 on t1.int_column = t2.int_column
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe RunnerStatus.SUCCESS
        job.output shouldContain """
            int_column	t1_string	t2_string
            1	hello world	hello world!
        """.trimIndent()
        job.close()
    }

    @Test
    fun testLeftJoinStatement() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner("""
            select
                t1.int_column,
                t1.string_col as t1_string,
                t2.string_col as t2_string
            from (
                select
                    1 as int_column,
                    "hello world" as string_col
                union all
                select
                    2 as int_column,
                    "show me the code" as string_col
            ) as t1 left outer join (
                select
                    1 as int_column,
                    "hello world!" as string_col
            ) as t2 on t1.int_column = t2.int_column
            order by int_column
        """.trimIndent())
        job.startAndJoin()
        job.status shouldBe RunnerStatus.SUCCESS
        job.output shouldContain """
            int_column	t1_string	t2_string
            1	hello world	hello world!
            2	show me the code	NULL
        """.trimIndent()
        job.close()
    }

    @Test
    fun testKillJob() = EnvSetter.autoSetLocalAndDerbyDir {
        val job = SparkSQLRunner("select reflect('java.lang.Thread', 'sleep', bigint(30000))")
        job.start()
        while (job.status != RunnerStatus.RUNNING) {
            Thread.sleep(1000)
        }
        Thread.sleep(3000).also { job.kill() }
        do {
            Thread.sleep(1000)
        } while (job.status == RunnerStatus.RUNNING)
        job.status shouldBe RunnerStatus.KILLED
        job.close()
    }

}