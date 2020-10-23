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
package tech.cuda.woden.service.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class DatetimeFunctionTest : StringSpec({
    "test yesterday" {
        LocalDateTime.of(2020, 10, 17, 0, 0, 0)
            .yesterday.toLocalDate() shouldBe LocalDate.of(2020, 10, 16)

        LocalDateTime.of(2020, 1, 1, 0, 0, 0)
            .yesterday.toLocalDate() shouldBe LocalDate.of(2019, 12, 31)

        LocalDateTime.of(2020, 1, 1, 0, 0, 0)
            .yesterday.toLocalDate() shouldBe LocalDate.of(2019, 12, 31)

        LocalDateTime.of(2020, 3, 1, 0, 0, 0)
            .yesterday.toLocalDate() shouldBe LocalDate.of(2020, 2, 29)

        LocalDateTime.of(2019, 3, 1, 0, 0, 0)
            .yesterday.toLocalDate() shouldBe LocalDate.of(2019, 2, 28)
    }

    "test monday"{
        LocalDateTime.of(2020, 10, 5, 0, 0, 0)
            .monday.toLocalDate() shouldBe LocalDate.of(2020, 10, 5)

        LocalDateTime.of(2020, 10, 6, 0, 0, 0)
            .monday.toLocalDate() shouldBe LocalDate.of(2020, 10, 5)

        LocalDateTime.of(2020, 10, 4, 0, 0, 0)
            .monday.toLocalDate() shouldBe LocalDate.of(2020, 9, 28)
    }

    "test newYearDay"{
        LocalDateTime.of(2020, 1, 1, 0, 0, 0)
            .newYearDay.toLocalDate() shouldBe LocalDate.of(2020, 1, 1)

        LocalDateTime.of(2020, 1, 2, 0, 0, 0)
            .newYearDay.toLocalDate() shouldBe LocalDate.of(2020, 1, 1)

        LocalDateTime.of(2019, 12, 31, 0, 0, 0)
            .newYearDay.toLocalDate() shouldBe LocalDate.of(2019, 1, 1)
    }

    "test monthStartDay"{
        LocalDateTime.of(2020, 3, 1, 0, 0, 0)
            .monthStartDay.toLocalDate() shouldBe LocalDate.of(2020, 3, 1)

        LocalDateTime.of(2020, 3, 2, 0, 0, 0)
            .monthStartDay.toLocalDate() shouldBe LocalDate.of(2020, 3, 1)

        LocalDateTime.of(2020, 2, 29, 0, 0, 0)
            .monthStartDay.toLocalDate() shouldBe LocalDate.of(2020, 2, 1)
    }

})