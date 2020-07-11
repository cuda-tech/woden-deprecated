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
package tech.cuda.datahub.service.mysql.type

import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.Column
import me.liuwj.ktorm.schema.SqlType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
fun <E : Any> BaseTable<E>.longtext(name: String): Column<String> {
    return registerColumn(name, LongTextSqlType)
}

object LongTextSqlType : SqlType<String>(Types.LONGVARCHAR, "longtext") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: String) {
        ps.setString(index, parameter)
    }

    override fun doGetResult(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }
}