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
package tech.cuda.datahub.config

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class DatahubConfigTest : StringSpec({
    "database config" {
        Datahub.database.druid.name shouldBe "DataHubServiceTest"
        Datahub.database.druid.initialSize shouldBe 1
        Datahub.database.druid.maxActive shouldBe null
        Datahub.database.druid.minIdle shouldBe null
        Datahub.database.druid.maxWait shouldBe null
        Datahub.database.druid.connectionInitSqls shouldBe null
        Datahub.database.druid.poolPreparedStatements shouldBe null
        Datahub.database.druid.maxPoolPreparedStatementPerConnectionSize shouldBe null
        Datahub.database.druid.validationQuery shouldBe null
        Datahub.database.druid.validationQueryTimeout shouldBe null
        Datahub.database.druid.testOnBorrow shouldBe null
        Datahub.database.druid.testOnReturn shouldBe null
        Datahub.database.druid.testWhileIdle shouldBe null
        Datahub.database.druid.timeBetweenEvictionRunsMillis shouldBe null
        Datahub.database.druid.keepAlive shouldBe null
        Datahub.database.druid.minEvictableIdleTimeMillis shouldBe null
        Datahub.database.druid.filters shouldBe null

        Datahub.database.mysql.username shouldBe "root"
        Datahub.database.mysql.password shouldBe "root"
        Datahub.database.mysql.host shouldBe "localhost"
        Datahub.database.mysql.port shouldBe 3306
        Datahub.database.mysql.encoding shouldBe "UTF-8"
        Datahub.database.mysql.dbName shouldBe "datahub"
    }

    "email config" {
        Datahub.email.host shouldBe "localhost"
        Datahub.email.sender shouldBe "root@host.com"
        Datahub.email.password shouldBe "root"
        Datahub.email.port shouldBe 465
    }

    "livy config"{
        Datahub.livy.host shouldBe "localhost"
        Datahub.livy.port shouldBe 8998
        Datahub.livy.baseUrl shouldBe "http://localhost:8998"
    }

    "scheduler config" {
        Datahub.scheduler.role shouldBe "master"
        Datahub.scheduler.sparkHome shouldNotBe null
    }


})
