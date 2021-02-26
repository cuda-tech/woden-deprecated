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
package tech.cuda.woden.common.service

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import me.liuwj.ktorm.schema.Table
import tech.cuda.woden.common.configuration.DataSourceMocker
import tech.cuda.woden.common.configuration.Woden

/**
 * 基于 maria 数据库的测试套件，所有测试用例执行前启动 maria 数据库
 * 并在每个测试用例创建时重新导入[tables]所依赖的数据表
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
abstract class TestWithMaria(body: StringSpec.() -> Unit = {}, private vararg val tables: Table<*> = arrayOf()) : StringSpec(body) {


    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        DataSourceMocker.mock()
        Database.connect(Woden.datasource)

    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        DataSourceMocker.unMock()
    }


    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        Database.rebuild()
        tables.forEach {
            Database.mock(it)
        }
    }
}