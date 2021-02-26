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
package tech.cuda.woden.common.service.po.dtype

import tech.cuda.woden.common.utils.Checker
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
data class ScheduleFormat(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val weekday: Int? = null, // 周一:1, 周二: 2, ..., 周日: 7
    val hour: Int? = null,
    val minute: Int = 0
) {
    /**
     * 判断格式是否满足时间和调度周期[period]的约束
     */
    fun isValid(period: SchedulePeriod): Boolean {
        if (year != null && year < 0) {
            return false
        }
        if (month != null && (month < 1 || month > 12)) {
            return false
        }
        if (day != null && (day < 1 || day > 31)) {
            return false
        }
        if (weekday != null && (weekday < 1 || weekday > 7)) {
            return false
        }
        if (hour != null && (hour < 0 || hour > 23)) {
            return false
        }
        if (minute < 0 || minute > 59) {
            return false
        }
        return when (period) {
            SchedulePeriod.HOUR -> Checker.allNull(year, month, day, weekday, hour)
            SchedulePeriod.DAY -> Checker.allNull(year, month, day, weekday) && hour != null
            SchedulePeriod.MONTH -> Checker.allNull(year, month, weekday) && Checker.allNotNull(day, hour)
            SchedulePeriod.YEAR -> Checker.allNull(year, weekday) && Checker.allNotNull(month, day, hour)
            SchedulePeriod.WEEK -> Checker.allNull(year, month, day) && Checker.allNotNull(weekday, hour)
            SchedulePeriod.ONCE -> weekday == null && Checker.allNotNull(year, month, day, hour)
        }
    }

    /**
     * 判断根据调度周期[period]判断在[date]这一天是否应该调度
     * 如果[date]没有提供，则判断当天是否应该调度
     */
    fun shouldSchedule(period: SchedulePeriod, date: LocalDateTime = LocalDateTime.now()): Boolean {
        if (!isValid(period)) return false
        return when (period) {
            SchedulePeriod.HOUR, SchedulePeriod.DAY -> true
            SchedulePeriod.MONTH -> date.dayOfMonth == day
            SchedulePeriod.ONCE -> date.year == year && date.monthValue == month && date.dayOfMonth == day
            SchedulePeriod.WEEK -> date.dayOfWeek.value == weekday
            SchedulePeriod.YEAR -> date.monthValue == month && date.dayOfMonth == day
        }
    }
}