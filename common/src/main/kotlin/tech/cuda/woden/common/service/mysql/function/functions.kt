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
package tech.cuda.woden.common.service.mysql.function

import me.liuwj.ktorm.expression.ArgumentExpression
import me.liuwj.ktorm.expression.FunctionExpression
import me.liuwj.ktorm.schema.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
fun ColumnDeclaring<Set<Int>>.contains(item: Int): FunctionExpression<Boolean> {
    return FunctionExpression(
        functionName = "json_contains",
        arguments = listOf(
            this.asExpression(),
            ArgumentExpression(item.toString(), VarcharSqlType),
            ArgumentExpression("$", VarcharSqlType)
        ),
        sqlType = BooleanSqlType
    )
}

fun ColumnDeclaring<LocalDateTime>.toDate(): FunctionExpression<LocalDate> {
    return FunctionExpression(
        functionName = "date",
        arguments = listOf(
            this.asExpression()
        ),
        sqlType = LocalDateSqlType
    )
}

fun unixTimestamp(): FunctionExpression<Long> {
    return FunctionExpression(functionName = "unix_timestamp", arguments = emptyList(), sqlType = LongSqlType)
}
