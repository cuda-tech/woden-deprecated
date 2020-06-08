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

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class ScheduleFormatTest : StringSpec({
    "周调度测试" {
        val period = SchedulePeriod.WEEK
        ScheduleFormat(weekday = 1, hour = 1).valid(period) shouldBe true
        ScheduleFormat(weekday = 1).valid(period) shouldBe false
        ScheduleFormat(year = 2020).valid(period) shouldBe false
        ScheduleFormat(month = 1).valid(period) shouldBe false
        ScheduleFormat(day = 1).valid(period) shouldBe false
        ScheduleFormat(hour = 1).valid(period) shouldBe false
    }

    "日调度测试"{
        val period = SchedulePeriod.DAY
        ScheduleFormat(hour = 1).valid(period) shouldBe true
        ScheduleFormat(weekday = 1).valid(period) shouldBe false
        ScheduleFormat(year = 2020).valid(period) shouldBe false
        ScheduleFormat(month = 1).valid(period) shouldBe false
        ScheduleFormat(day = 1).valid(period) shouldBe false
    }

    "月调度测试"{
        val period = SchedulePeriod.MONTH
        ScheduleFormat(day = 1, hour = 1).valid(period) shouldBe true
        ScheduleFormat(weekday = 1).valid(period) shouldBe false
        ScheduleFormat(year = 2020).valid(period) shouldBe false
        ScheduleFormat(month = 1).valid(period) shouldBe false
        ScheduleFormat(day = 1).valid(period) shouldBe false
        ScheduleFormat(hour = 1).valid(period) shouldBe false
    }

    "年调度测试"{
        val period = SchedulePeriod.YEAR
        ScheduleFormat(month = 1, day = 1, hour = 1).valid(period) shouldBe true
        ScheduleFormat(weekday = 1).valid(period) shouldBe false
        ScheduleFormat(year = 2020).valid(period) shouldBe false
        ScheduleFormat(month = 1).valid(period) shouldBe false
        ScheduleFormat(day = 1).valid(period) shouldBe false
        ScheduleFormat(hour = 1).valid(period) shouldBe false
    }

    "小时调度测试"{
        val period = SchedulePeriod.HOUR
        ScheduleFormat(minute = 1).valid(period) shouldBe true
        ScheduleFormat(weekday = 1).valid(period) shouldBe false
        ScheduleFormat(year = 2020).valid(period) shouldBe false
        ScheduleFormat(month = 1).valid(period) shouldBe false
        ScheduleFormat(day = 1).valid(period) shouldBe false
        ScheduleFormat(hour = 1).valid(period) shouldBe false
    }

    "单次调度测试"{
        val period = SchedulePeriod.ONCE
        ScheduleFormat(year = 2020, month = 1, day = 1, hour = 1).valid(period) shouldBe true
        ScheduleFormat(weekday = 1).valid(period) shouldBe false
        ScheduleFormat(year = 2020).valid(period) shouldBe false
        ScheduleFormat(month = 1).valid(period) shouldBe false
        ScheduleFormat(day = 1).valid(period) shouldBe false
        ScheduleFormat(hour = 1).valid(period) shouldBe false
    }

})