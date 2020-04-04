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
package tech.cuda.datahub.webserver.tools

import tech.cuda.datahub.service.SchemaUtils

/**
 * 将所有的测试数据加载进数据库
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
object TablesMocker {

    fun mock(table: String) = SchemaUtils.loadTable(
        "datahub.$table",
        this.javaClass.classLoader.getResource("tables/$table.txt")!!.path
    )

    fun mock(tables: List<String>) = tables.forEach { mock(it) }

    fun mockAllTable() {
        SchemaUtils.rebuildDB()
        mock(listOf("users", "files", "groups", "machines", "file_mirrors"))
    }
}

fun main() {
    TablesMocker.mockAllTable()
}