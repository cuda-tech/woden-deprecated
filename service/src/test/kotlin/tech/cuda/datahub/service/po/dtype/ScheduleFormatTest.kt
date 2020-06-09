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
package tech.cuda.datahub.service.po.dtype

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class ScheduleFormatTest : StringSpec({
    "周调度测试" {
        val period = SchedulePeriod.WEEK
        ScheduleFormat(weekday = 1, hour = 1).isValid(period) shouldBe true
        ScheduleFormat(weekday = 1).isValid(period) shouldBe false
        ScheduleFormat(year = 2020).isValid(period) shouldBe false
        ScheduleFormat(month = 1).isValid(period) shouldBe false
        ScheduleFormat(day = 1).isValid(period) shouldBe false
        ScheduleFormat(hour = 1).isValid(period) shouldBe false
        // 周一
        ScheduleFormat(weekday = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe true
        // 周日
        ScheduleFormat(weekday = 7, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 7, 1, 0)) shouldBe true
        // weekday 不匹配
        ScheduleFormat(weekday = 2, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe false
        // 格式非法
        ScheduleFormat(hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe false
    }

    "日调度测试"{
        val period = SchedulePeriod.DAY
        ScheduleFormat(hour = 1).isValid(period) shouldBe true
        ScheduleFormat(weekday = 1).isValid(period) shouldBe false
        ScheduleFormat(year = 2020).isValid(period) shouldBe false
        ScheduleFormat(month = 1).isValid(period) shouldBe false
        ScheduleFormat(day = 1).isValid(period) shouldBe false

        ScheduleFormat(hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe true
        ScheduleFormat(month = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe false
    }

    "月调度测试"{
        val period = SchedulePeriod.MONTH
        ScheduleFormat(day = 1, hour = 1).isValid(period) shouldBe true
        ScheduleFormat(weekday = 1).isValid(period) shouldBe false
        ScheduleFormat(year = 2020).isValid(period) shouldBe false
        ScheduleFormat(month = 1).isValid(period) shouldBe false
        ScheduleFormat(day = 1).isValid(period) shouldBe false
        ScheduleFormat(hour = 1).isValid(period) shouldBe false

        ScheduleFormat(day = 8, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe true
        ScheduleFormat(day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe false
        ScheduleFormat(day = -1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe false
        ScheduleFormat(day = 32, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 6, 8, 1, 0)) shouldBe false
    }

    "年调度测试"{
        val period = SchedulePeriod.YEAR
        ScheduleFormat(month = 1, day = 1, hour = 1).isValid(period) shouldBe true
        ScheduleFormat(weekday = 1).isValid(period) shouldBe false
        ScheduleFormat(year = 2020).isValid(period) shouldBe false
        ScheduleFormat(month = 1).isValid(period) shouldBe false
        ScheduleFormat(day = 1).isValid(period) shouldBe false
        ScheduleFormat(hour = 1).isValid(period) shouldBe false

        ScheduleFormat(month = 1, day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 1, 1, 1, 0)) shouldBe true
        ScheduleFormat(month = 1, day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2021, 1, 1, 1, 0)) shouldBe true
        ScheduleFormat(month = 1, day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 1, 2, 1, 0)) shouldBe false
        ScheduleFormat(month = 1, day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 2, 1, 1, 0)) shouldBe false
    }

    "小时调度测试"{
        val period = SchedulePeriod.HOUR
        ScheduleFormat(minute = 1).isValid(period) shouldBe true
        ScheduleFormat(weekday = 1).isValid(period) shouldBe false
        ScheduleFormat(year = 2020).isValid(period) shouldBe false
        ScheduleFormat(month = 1).isValid(period) shouldBe false
        ScheduleFormat(day = 1).isValid(period) shouldBe false
        ScheduleFormat(hour = 1).isValid(period) shouldBe false
        ScheduleFormat(minute = 1).shouldSchedule(period, LocalDateTime.of(2020, 1, 1, 1, 0)) shouldBe true
    }

    "单次调度测试"{
        val period = SchedulePeriod.ONCE
        ScheduleFormat(year = 2020, month = 1, day = 1, hour = 1).isValid(period) shouldBe true
        ScheduleFormat(weekday = 1).isValid(period) shouldBe false
        ScheduleFormat(year = 2020).isValid(period) shouldBe false
        ScheduleFormat(month = 1).isValid(period) shouldBe false
        ScheduleFormat(day = 1).isValid(period) shouldBe false
        ScheduleFormat(hour = 1).isValid(period) shouldBe false
        ScheduleFormat(year = 2020, month = 1, day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 1, 1, 1, 0)) shouldBe true
        ScheduleFormat(year = 2020, month = 1, day = 2, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 1, 1, 1, 0)) shouldBe false
        ScheduleFormat(year = 2020, month = 2, day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 1, 1, 1, 0)) shouldBe false
        ScheduleFormat(year = 2021, month = 1, day = 1, hour = 1).shouldSchedule(period, LocalDateTime.of(2020, 1, 1, 1, 0)) shouldBe false
    }

})