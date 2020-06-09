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
package tech.cuda.datahub.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.datahub.TestWithMaria
import tech.cuda.datahub.service.dao.*
import tech.cuda.datahub.service.exception.NotFoundException
import tech.cuda.datahub.service.exception.OperationNotAllowException
import tech.cuda.datahub.service.exception.PermissionException
import tech.cuda.datahub.service.po.dtype.ScheduleDependencyInfo
import tech.cuda.datahub.service.po.dtype.ScheduleFormat
import tech.cuda.datahub.service.po.dtype.SchedulePeriod
import tech.cuda.datahub.service.po.dtype.SchedulePriority

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class TaskServiceTest : TestWithMaria({
    "按 ID 查找" {
        val task = TaskService.findById(16)
        task shouldNotBe null
        task!!
        task.mirrorId shouldBe 38
        task.groupId shouldBe 31
        task.name shouldBe "aniudyqv"
        task.owners shouldContainExactlyInAnyOrder setOf(131, 163, 98, 108)
        task.period shouldBe SchedulePeriod.MONTH
        task.format shouldBe ScheduleFormat(day=12, hour=21, minute = 46)
        task.format.isValid(task.period) shouldBe true
        task.isSoftFail shouldBe false
        task.queue shouldBe "vfukassr"
        task.priority shouldBe SchedulePriority.HIGH
        task.pendingTimeout shouldBe 44
        task.runningTimeout shouldBe 50
        task.retries shouldBe 1
        task.retryDelay shouldBe 47
        task.isValid shouldBe true

        TaskService.findById(1) shouldBe null
        TaskService.findById(461) shouldBe null
    }

    "批量查询" {
        val validCount = 377
        val pageSize = 13
        val queryTimes = validCount / pageSize + 1
        val lastPageUserCount = validCount % pageSize
        for (page in 1..queryTimes) {
            val (tasks, count) = TaskService.listing(page, pageSize)
            count shouldBe validCount
            tasks.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
            tasks.forEach { it.format.isValid(it.period) }
        }
    }

    "模糊查询" {
        // 提供空或 null 的相似词
        var validCount = 377
        var pageSize = 13
        var queryTimes = validCount / pageSize + 1
        var lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(TaskService.listing(page, pageSize, null)) {
                val (tasks, count) = this
                tasks.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(TaskService.listing(page, pageSize, "   ")) {
                val (tasks, count) = this
                tasks.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(TaskService.listing(page, pageSize, " NULL  ")) {
                val (tasks, count) = this
                tasks.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 1 个相似词
        validCount = 101
        pageSize = 7
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(TaskService.listing(page, pageSize, "a")) {
                val (tasks, count) = this
                tasks.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(TaskService.listing(page, pageSize, "  a null")) {
                val (tasks, count) = this
                tasks.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

        // 提供 2 个相似词
        validCount = 20
        pageSize = 3
        queryTimes = validCount / pageSize + 1
        lastPageCount = validCount % pageSize
        for (page in 1..queryTimes) {
            with(TaskService.listing(page, pageSize, "a b")) {
                val (tasks, count) = this
                tasks.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }

            with(TaskService.listing(page, pageSize, " b  a null")) {
                val (tasks, count) = this
                tasks.size shouldBe if (page == queryTimes) lastPageCount else pageSize
                count shouldBe validCount
            }
        }

    }

    "按项目组分页查询" {
        val validCount = 12
        val pageSize = 3
        val queryTimes = validCount / pageSize + 1
        val lastPageUserCount = validCount % pageSize
        for (page in 1..queryTimes) {
            val (tasks, count) = TaskService.listing(page, pageSize, groupId = 10)
            count shouldBe validCount
            tasks.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
        }

        shouldThrow<NotFoundException> {
            TaskService.listing(1, 100, groupId = 7)
        }.message shouldBe "项目组 7 不存在或已被删除"
    }

    "按负责人分页查询" {
        val validCount = 5
        val pageSize = 2
        val queryTimes = validCount / pageSize + 1
        val lastPageUserCount = validCount % pageSize
        for (page in 1..queryTimes) {
            val (tasks, count) = TaskService.listing(page, pageSize, ownerId = 3)
            count shouldBe validCount
            tasks.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
        }

        shouldThrow<NotFoundException> {
            TaskService.listing(1, 100, ownerId = 4)
        }.message shouldBe "用户 4 不存在或已被删除"

    }

    "按调度周期分页查询" {
        val validCount = 82
        val pageSize = 13
        val queryTimes = validCount / pageSize + 1
        val lastPageUserCount = validCount % pageSize
        for (page in 1..queryTimes) {
            val (tasks, count) = TaskService.listing(page, pageSize, period = SchedulePeriod.DAY)
            count shouldBe validCount
            tasks.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
        }
    }

    "按执行队列分页查询" {
        val validCount = 15
        val pageSize = 7
        val queryTimes = validCount / pageSize + 1
        val lastPageUserCount = validCount % pageSize
        for (page in 1..queryTimes) {
            val (tasks, count) = TaskService.listing(page, pageSize, queue = "bigdata")
            count shouldBe validCount
            tasks.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
        }
    }

    "按任务状态分页查询" {
        val validCount = 305
        val pageSize = 111
        val queryTimes = validCount / pageSize + 1
        val lastPageUserCount = validCount % pageSize
        for (page in 1..queryTimes) {
            val (tasks, count) = TaskService.listing(page, pageSize, isValid = true)
            count shouldBe validCount
            tasks.size shouldBe if (page == queryTimes) lastPageUserCount else pageSize
        }
    }

    "复合过滤查询" {
        val (tasks, count) = TaskService.listing(
            page = 1,
            pageSize = 2,
            isValid = true,
            groupId = 3,
            period = SchedulePeriod.DAY
        )
        tasks.size shouldBe 2
        count shouldBe 3
    }

    "创建任务" {
        TaskService.findById(3)!!.children shouldNotContain 461
        TaskService.findById(4)!!.children shouldNotContain 461
        val nextId = 461
        val task = TaskService.create(
            mirrorId = 1,
            name = "test create",
            ownerIds = setOf(3, 12, 15),
            period = SchedulePeriod.DAY,
            format = ScheduleFormat(hour = 3),
            queue = "adhoc",
            parent = mapOf(
                3 to ScheduleDependencyInfo(),
                4 to ScheduleDependencyInfo()
            )
        )
        task.id shouldBe nextId
        task.groupId shouldBe 3
        task.name shouldBe "test create"
        task.owners shouldContainExactlyInAnyOrder setOf(3, 12, 15)
        task.period shouldBe SchedulePeriod.DAY
        task.format shouldBe ScheduleFormat(hour = 3, minute = 0)
        task.format.isValid(task.period) shouldBe true
        task.parent.keys shouldContainExactlyInAnyOrder setOf(3, 4)
        task.queue shouldBe "adhoc"
        task.priority shouldBe SchedulePriority.VERY_LOW
        TaskService.findById(3)!!.children shouldContain 461
        TaskService.findById(4)!!.children shouldContain 461

        // 镜像不存在
        shouldThrow<NotFoundException> {
            TaskService.create(
                mirrorId = 5,
                name = "test create",
                ownerIds = setOf(3, 12, 15),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(hour = 3),
                queue = "adhoc",
                parent = mapOf(
                    3 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "文件镜像 5 不存在或已被删除"

        // 文件不存在
        shouldThrow<NotFoundException> {
            TaskService.create(
                mirrorId = 23,
                name = "test create",
                ownerIds = setOf(3, 12, 15),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(hour = 3),
                queue = "adhoc",
                parent = mapOf(
                    3 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "文件节点 16 不存在或已被删除"

        // 用户无权限
        shouldThrow<PermissionException> {
            TaskService.create(
                mirrorId = 1,
                name = "test create",
                ownerIds = setOf(3, 12, 15, 27),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(hour = 3),
                queue = "adhoc",
                parent = mapOf(
                    3 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "用户 27 不归属于 项目组 3"

        // 用户不存在
        shouldThrow<NotFoundException> {
            TaskService.create(
                mirrorId = 1,
                name = "test create",
                ownerIds = setOf(3, 12, 15, 4),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(hour = 3),
                queue = "adhoc",
                parent = mapOf(
                    3 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "用户 4 不存在或已被删除"

        // 父任务不存在
        shouldThrow<NotFoundException> {
            TaskService.create(
                mirrorId = 1,
                name = "test create",
                ownerIds = setOf(3, 12, 15),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(hour = 3),
                queue = "adhoc",
                parent = mapOf(
                    2 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "父任务 2 不存在或已被删除"

        // 父任务失效
        shouldThrow<OperationNotAllowException> {
            TaskService.create(
                mirrorId = 1,
                name = "test create",
                ownerIds = setOf(3, 12, 15),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(hour = 3),
                queue = "adhoc",
                parent = mapOf(
                    6 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "父任务 6 已失效 , 禁止依赖"

        // 调度格式非法
        shouldThrow<OperationNotAllowException> {
            TaskService.create(
                mirrorId = 1,
                name = "test create",
                ownerIds = setOf(3, 12, 15),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(hour = 24),
                queue = "adhoc",
                parent = mapOf(
                    3 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "调度时间格式 非法"

        shouldThrow<OperationNotAllowException> {
            TaskService.create(
                mirrorId = 1,
                name = "test create",
                ownerIds = setOf(3, 12, 15),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(year = 2020, hour = 3),
                queue = "adhoc",
                parent = mapOf(
                    3 to ScheduleDependencyInfo(),
                    4 to ScheduleDependencyInfo()
                )
            )
        }.message shouldBe "调度时间格式 非法"


    }

    "更新任务" {
        TaskService.findById(30)!!.children shouldContain 35
        TaskService.findById(34)!!.children shouldContain 35
        TaskService.findById(4)!!.children shouldNotContain 35
        TaskService.findById(8)!!.children shouldNotContain 35
        TaskService.update(
            id = 35,
            mirrorId = 203,
            name = "test update",
            ownerIds = setOf(14, 16, 17),
            period = SchedulePeriod.DAY,
            format = ScheduleFormat(hour = 3),
            queue = "adhoc",
            priority = SchedulePriority.HIGH,
            parent = mapOf(
                4 to ScheduleDependencyInfo(),
                8 to ScheduleDependencyInfo()
            ),
            isValid = false
        )
        val task = TaskService.findById(35)!!
        task.mirrorId shouldBe 203
        task.name shouldBe "test update"
        task.owners shouldContainExactlyInAnyOrder setOf(14, 16, 17)
        task.period shouldBe SchedulePeriod.DAY
        task.queue shouldBe "adhoc"
        task.priority shouldBe SchedulePriority.HIGH
        task.parent.keys shouldContainExactlyInAnyOrder setOf(4, 8)
        task.isValid shouldBe false
        TaskService.findById(4)!!.children shouldContain 35
        TaskService.findById(8)!!.children shouldContain 35
        TaskService.findById(30)!!.children shouldNotContain 35
        TaskService.findById(34)!!.children shouldNotContain 35

        // 任务不存在
        shouldThrow<NotFoundException> {
            TaskService.update(
                id = 461,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                period = SchedulePeriod.DAY,
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "调度任务 461 不存在或已被删除"

        // 镜像不存在
        shouldThrow<NotFoundException> {
            TaskService.update(
                id = 35,
                mirrorId = 301,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                period = SchedulePeriod.DAY,
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "文件镜像 301 不存在或已被删除"

        // 试图更新调度周期而不提供调度时间格式
        shouldThrow<OperationNotAllowException> {
            TaskService.update(
                id = 35,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                period = SchedulePeriod.DAY,
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "调度时间格式 缺失"

        // 试图更新调度周期而调度时间格式不匹配
        shouldThrow<OperationNotAllowException> {
            TaskService.update(
                id = 35,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                period = SchedulePeriod.DAY,
                format = ScheduleFormat(year=2020, hour = 3),
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "调度时间格式 非法"

        // 试图更新非法的调度时间格式
        shouldThrow<OperationNotAllowException> {
            TaskService.update(
                id = 35,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                format = ScheduleFormat(year=2020, hour = 3),
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "调度时间格式 非法"

        // 文件不存在
        shouldThrow<NotFoundException> {
            TaskService.update(
                id = 43,
                mirrorId = 111,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                period = SchedulePeriod.DAY,
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "文件节点 5 不存在或已被删除"

        // 跨文件更新镜像
        shouldThrow<OperationNotAllowException> {
            TaskService.update(
                id = 35,
                mirrorId = 204,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                period = SchedulePeriod.DAY,
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "禁止跨文件更新镜像"

        // 用户不存在
        shouldThrow<NotFoundException> {
            TaskService.update(
                id = 35,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17, 180),
                period = SchedulePeriod.DAY,
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }

        // 用户无权限
        shouldThrow<PermissionException> {
            TaskService.update(
                id = 35,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17, 46),
                period = SchedulePeriod.DAY,
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "用户 46 不归属于 项目组 4"

        // 父任务不存在
        shouldThrow<NotFoundException> {
            TaskService.update(
                id = 35,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    12 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "父任务 12 不存在或已被删除"

        // 父任务失效
        shouldThrow<OperationNotAllowException> {
            TaskService.update(
                id = 35,
                mirrorId = 203,
                name = "test update",
                ownerIds = setOf(14, 16, 17),
                queue = "adhoc",
                priority = SchedulePriority.HIGH,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    37 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "父任务 37 已失效 , 禁止依赖"

        // 子任务未失效，并检查事务
        TaskService.findById(4)!!.children shouldContain 3
        shouldThrow<OperationNotAllowException> {
            TaskService.update(
                id = 3,
                parent = mapOf(
                    4 to ScheduleDependencyInfo(),
                    8 to ScheduleDependencyInfo()
                ),
                isValid = false
            )
        }.message shouldBe "子任务 5 未失效 , 父任务 禁止失效"
        TaskService.findById(4)!!.children shouldContain 3

        TaskService.update(
            id = 3,
            parent = mapOf(
                8 to ScheduleDependencyInfo()
            )
        )
        TaskService.findById(4)!!.children shouldNotContain 3
    }

    "删除任务" {
        TaskService.findById(6) shouldNotBe null
        TaskService.remove(6)
        TaskService.findById(6) shouldBe null

        // 不存在
        shouldThrow<NotFoundException> {
            TaskService.remove(461)
        }.message shouldBe "调度任务 461 不存在或已被删除"

        // 已删除
        shouldThrow<NotFoundException> {
            TaskService.remove(1)
        }.message shouldBe "调度任务 1 不存在或已被删除"

        shouldThrow<OperationNotAllowException> {
            TaskService.remove(33)
        }.message shouldBe "子任务 未失效 , 禁止删除"
    }

}, TaskDAO, GroupDAO, UserDAO, FileMirrorDAO, FileDAO)
