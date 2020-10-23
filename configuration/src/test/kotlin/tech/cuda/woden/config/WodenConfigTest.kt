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
package tech.cuda.woden.config

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class WodenConfigTest : StringSpec({
    "database config" {
        Woden.database.druid.name shouldBe "WodenServiceTest"
        Woden.database.druid.initialSize shouldBe 1
        Woden.database.druid.maxActive shouldBe null
        Woden.database.druid.minIdle shouldBe null
        Woden.database.druid.maxWait shouldBe null
        Woden.database.druid.connectionInitSqls shouldBe null
        Woden.database.druid.poolPreparedStatements shouldBe null
        Woden.database.druid.maxPoolPreparedStatementPerConnectionSize shouldBe null
        Woden.database.druid.validationQuery shouldBe null
        Woden.database.druid.validationQueryTimeout shouldBe null
        Woden.database.druid.testOnBorrow shouldBe null
        Woden.database.druid.testOnReturn shouldBe null
        Woden.database.druid.testWhileIdle shouldBe null
        Woden.database.druid.timeBetweenEvictionRunsMillis shouldBe null
        Woden.database.druid.keepAlive shouldBe null
        Woden.database.druid.minEvictableIdleTimeMillis shouldBe null
        Woden.database.druid.filters shouldBe null

        Woden.database.mysql.username shouldBe "root"
        Woden.database.mysql.password shouldBe "root"
        Woden.database.mysql.host shouldBe "localhost"
        Woden.database.mysql.port shouldBe 3306
        Woden.database.mysql.encoding shouldBe "UTF-8"
        Woden.database.mysql.dbName shouldBe "woden"
    }

    "email config" {
        Woden.email.host shouldBe "localhost"
        Woden.email.sender shouldBe "root@host.com"
        Woden.email.password shouldBe "root"
        Woden.email.port shouldBe 465
    }

    "scheduler config" {
        Woden.scheduler.role shouldBe "master"
        Woden.scheduler.sparkHome shouldNotBe null
        Woden.scheduler.anacondaPath shouldNotBe null
        Woden.scheduler.bashPath shouldNotBe null
    }

})
