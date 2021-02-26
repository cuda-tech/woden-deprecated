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
package tech.cuda.woden.common.configuration

import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class WodenConfigTest : StringSpec({
    "datasource config" {
        DataSourceMocker.mock()
        val datasource = Woden.datasource as HikariDataSource
        datasource.jdbcUrl shouldStartWith "jdbc:mysql://localhost:"
        datasource.jdbcUrl shouldEndWith "/test?characterEncoding=UTF-8"
        datasource.username shouldBe "root"
        datasource.password shouldBe null
        datasource.isAutoCommit shouldBe true
        datasource.connectionTimeout shouldBe 30000
        datasource.idleTimeout shouldBe 600000
        datasource.maxLifetime shouldBe 1800000
        datasource.connectionTestQuery shouldBe null
        datasource.minimumIdle shouldBe 10
        datasource.maximumPoolSize shouldBe 20
        datasource.metricRegistry shouldBe null
        datasource.healthCheckRegistry shouldBe null
        datasource.initializationFailTimeout shouldBe 1
        datasource.isIsolateInternalQueries shouldBe false
        datasource.isAllowPoolSuspension shouldBe false
        datasource.isReadOnly shouldBe false
        datasource.isRegisterMbeans shouldBe false
        datasource.catalog shouldBe null
        datasource.connectionInitSql shouldBe null
        datasource.driverClassName shouldBe null
        datasource.validationTimeout shouldBe 5000
        datasource.leakDetectionThreshold shouldBe 0
        datasource.threadFactory shouldBe null
        datasource.scheduledExecutor shouldBe null
        DataSourceMocker.unMock()
    }

    "email config" {
        Woden.email.host shouldBe "localhost"
        Woden.email.sender shouldBe "root@host.com"
        Woden.email.password shouldBe "root"
        Woden.email.port shouldBe 3465
    }

    "scheduler config" {
        Woden.scheduler.role shouldBe "master"
        Woden.scheduler.sparkHome shouldNotBe null
        Woden.scheduler.anacondaPath shouldNotBe null
        Woden.scheduler.bashPath shouldNotBe null
    }

})
