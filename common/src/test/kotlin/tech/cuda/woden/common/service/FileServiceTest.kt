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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.common.service.dto.UserDTO
import tech.cuda.woden.common.service.exception.DuplicateException
import tech.cuda.woden.common.service.dao.FileDAO
import tech.cuda.woden.common.service.dao.GroupDAO
import tech.cuda.woden.common.service.dao.UserDAO
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.exception.OperationNotAllowException
import tech.cuda.woden.common.service.exception.PermissionException
import tech.cuda.woden.common.service.po.dtype.FileType
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class FileServiceTest : TestWithMaria({
    "模糊查询" {
        with(FileService.search("a b c ")) {
            val (files, count) = this
            files.size shouldBe 2
            count shouldBe 2
            files.map { it.name } shouldContainInOrder listOf("bcmawkte", "lwbaccod")
        }

        with(FileService.search("null b a  c ")) {
            val (files, count) = this
            files.size shouldBe 2
            count shouldBe 2
            files.map { it.name } shouldContainInOrder listOf("bcmawkte", "lwbaccod")
        }

        with(FileService.search("null")) {
            val (files, count) = this
            files.size shouldBe 0
            count shouldBe 0
        }

        with(FileService.search("hh")) {
            val (files, count) = this
            files.size shouldBe 2
            count shouldBe 2
            files.map { it.name } shouldContainInOrder listOf("hhkjnqwc", "ghxwtphh")
        }
    }

    "根据 ID 查找" {
        FileService.findById(37) shouldNotBe null
        FileService.findById(38) shouldBe null
        FileService.findById(70) shouldBe null
    }

    "查找所有直属子节点" {
        with(FileService.listChildren(1)) {
            val (files, count) = this
            files.size shouldBe 5
            count shouldBe 5
            files.map { it.type } shouldContainInOrder listOf(FileType.DIR, FileType.SPARK_SQL, FileType.SPARK_SQL, FileType.SPARK_SQL, FileType.SPARK_SHELL)
            files.map { it.name } shouldContainInOrder listOf("zwgjydgn", "kniovyqn", "ladlehnr", "yoglnkyc", "jldwzlys")
        }

        with(FileService.listChildren(4)) {
            val (files, count) = this
            files.size shouldBe 3
            count shouldBe 3
            files.map { it.type } shouldContainInOrder listOf(FileType.DIR, FileType.SPARK_SQL, FileType.SPARK_SHELL)
            files.map { it.name } shouldContainInOrder listOf("zvdjsdhz", "yijlstlq", "yzhamcqc")
        }
    }

    "查找所有父节点" {
        // 三层父节点
        with(FileService.listParent(28)) {
            val (parent, count) = this
            parent.size shouldBe 3
            count shouldBe 3
            parent.map { it.name } shouldContainInOrder listOf("root_project", "zwgjydgn", "zvdjsdhz")
        }

        // 两层父节点
        with(FileService.listParent(42)) {
            val (parent, count) = this
            parent.size shouldBe 2
            count shouldBe 2
            parent.map { it.name } shouldContainInOrder listOf("root_project", "zwgjydgn")
        }
        with(FileService.listParent(27)) {
            val (parent, count) = this
            parent.size shouldBe 2
            count shouldBe 2
            parent.map { it.name } shouldContainInOrder listOf("root_project", "zwgjydgn")
        }

        // 一层父节点
        with(FileService.listParent(6)) {
            val (parent, count) = this
            parent.size shouldBe 1
            count shouldBe 1
            parent.map { it.name } shouldContainInOrder listOf("root_project")
        }
        with(FileService.listParent(4)) {
            val (parent, count) = this
            parent.size shouldBe 1
            count shouldBe 1
            parent.map { it.name } shouldContainInOrder listOf("root_project")
        }

        with(FileService.listParent(1)) {
            val (parent, count) = this
            parent.size shouldBe 0
            count shouldBe 0
        }

        // 已删除的文件
        shouldThrow<NotFoundException> {
            FileService.listParent(5)
        }.message shouldBe "文件节点 5 不存在或已被删除"

        // 不存在的文件
        shouldThrow<NotFoundException> {
            FileService.listParent(70)
        }.message shouldBe "文件节点 70 不存在或已被删除"
    }

    "查找项目组的根节点" {
        val root = FileService.findRootByGroupId(6)
        root.name shouldBe "hhkjnqwc"
        root.ownerId shouldBe 124
        root.type shouldBe FileType.DIR
        root.groupId shouldBe 6
        root.parentId shouldBe null

        shouldThrow<NotFoundException> {
            FileService.findRootByGroupId(7)
        }.message shouldBe "项目组 7 根目录 不存在或已被删除"

        shouldThrow<NotFoundException> {
            FileService.findRootByGroupId(40)
        }.message shouldBe "项目组 40 根目录 不存在或已被删除"
    }

    "获取文件内容" {
        FileService.getContent(2).content shouldBe "xwuocwyldfswbdwbnkpizvuhokfhhbwrmykqlgtpqkrzuatixnavciilmbkyxnuw"

        // 已删除的文件
        shouldThrow<NotFoundException> {
            FileService.getContent(5)
        }.message shouldBe "文件节点 5 不存在或已被删除"

        // 不存在的文件
        shouldThrow<NotFoundException> {
            FileService.getContent(70)
        }.message shouldBe "文件节点 70 不存在或已被删除"

        // 文件夹
        shouldThrow<OperationNotAllowException> {
            FileService.getContent(4)
        }.message shouldBe "文件夹 不允许 获取 内容"
    }

    "新建文件节点" {
        val sqlFile = FileService.create(
            groupId = 1,
            user = UserService.findById(1)!!,
            name = "test create",
            type = FileType.SPARK_SQL,
            parentId = 1
        )
        sqlFile.ownerId shouldBe 1
        sqlFile.id shouldBe 70
        sqlFile.name shouldBe "test create"
        sqlFile.type shouldBe FileType.SPARK_SQL
        sqlFile.parentId shouldBe 1

        // 创建同目录下同名但不同类型的文件
        val sparkFile = FileService.create(
            groupId = 1,
            user = UserService.findById(2)!!,
            name = "test create",
            type = FileType.SPARK_SHELL,
            parentId = 1
        )
        sparkFile.ownerId shouldBe 2
        sparkFile.id shouldBe 71
        sparkFile.name shouldBe "test create"
        sparkFile.type shouldBe FileType.SPARK_SHELL
        sparkFile.parentId shouldBe 1

        // 父节点不存在
        shouldThrow<NotFoundException> {
            FileService.create(
                groupId = 1,
                user = UserService.findById(1)!!,
                name = "test create",
                type = FileType.SPARK_SQL,
                parentId = 21
            )
        }.message shouldBe "父节点 21 不存在或已被删除"

        // 父节点不是文件夹
        shouldThrow<OperationNotAllowException> {
            FileService.create(
                groupId = 1,
                user = UserService.findById(1)!!,
                name = "test create",
                type = FileType.SPARK_SQL,
                parentId = 2
            )
        }.message shouldBe "父节点 2 不是 文件夹"

        // 项目组不存在
        shouldThrow<NotFoundException> {
            FileService.create(
                groupId = 7,
                user = UserService.findById(1)!!,
                name = "test create",
                type = FileType.SPARK_SQL,
                parentId = 1
            )
        }.message shouldBe "项目组 7 不存在或已被删除"

        // 归属用户已被删除
        shouldThrow<NotFoundException> {
            FileService.create(
                groupId = 1,
                user = UserDTO(
                    id = 4,
                    groups = setOf(3, 6, 5, 4, 7, 2),
                    name = "NCiUmXrvkC",
                    email = "NCiUmXrvkC@sina.cn",
                    createTime = LocalDateTime.now(),
                    updateTime = LocalDateTime.now()
                ),
                name = "test create",
                type = FileType.SPARK_SQL,
                parentId = 1
            )
        }.message shouldBe "用户 4 不存在或已被删除"

        // 归属用户没权限
        shouldThrow<PermissionException> {
            FileService.create(
                groupId = 1,
                user = UserService.findById(3)!!,
                name = "test create",
                type = FileType.SPARK_SQL,
                parentId = 1
            )
        }.message shouldBe "用户 3 不归属于 项目组 1"

        // 同目录下的同类型同名文件
        shouldThrow<DuplicateException> {
            FileService.create(
                groupId = 1,
                user = UserService.findById(1)!!,
                name = "test create",
                type = FileType.SPARK_SQL,
                parentId = 1
            )
        }.message shouldBe "文件夹 1 存在 文件类型 SPARK_SQL 文件节点 test create"
    }

    "更新文件节点" {
        FileService.update(
            id = 3,
            ownerId = 2,
            name = "test update",
            content = "anything",
            parentId = 4
        )
        val file = FileService.findById(3)
        FileService.getContent(3).content shouldBe "anything"
        file shouldNotBe null
        file!!
        file.name shouldBe "test update"
        file.parentId shouldBe 4

        // 已删除节点
        shouldThrow<NotFoundException> {
            FileService.update(11, ownerId = 1)
        }.message shouldBe "文件节点 11 不存在或已被删除"

        // 不存在节点
        shouldThrow<NotFoundException> {
            FileService.update(70, ownerId = 1)
        }.message shouldBe "文件节点 70 不存在或已被删除"

        // 根节点
        shouldThrow<OperationNotAllowException> {
            FileService.update(56, ownerId = 1)
        }.message shouldBe "根目录 禁止更新"

        // 已被删除的用户
        shouldThrow<NotFoundException> {
            FileService.update(3, ownerId = 4)
        }.message shouldBe "用户 4 不存在或已被删除"

        // 没权限的用户
        shouldThrow<PermissionException> {
            FileService.update(3, ownerId = 12)
        }.message shouldBe "用户 12 不归属于 项目组 1"

        // 同目录下同类型同名
        shouldThrow<DuplicateException> {
            FileService.update(3, name = "yijlstlq")
        }.message shouldBe "文件夹 3 存在 文件类型 SPARK_SQL 文件节点 yijlstlq"

        // 更新文件夹 content
        shouldThrow<OperationNotAllowException> {
            FileService.update(4, content = "anything")
        }.message shouldBe "文件夹 内容 禁止更新"

        // 父节点已被删除
        shouldThrow<NotFoundException> {
            FileService.update(3, parentId = 80)
        }.message shouldBe "父节点 80 不存在或已被删除"

        // 父节点不是文件夹
        shouldThrow<OperationNotAllowException> {
            FileService.update(3, parentId = 6)
        }.message shouldBe "父节点 6 不是 文件夹"

        // 跨项目组
        shouldThrow<PermissionException> {
            FileService.update(3, parentId = 20)
        }.message shouldBe "父节点 20 不归属于 项目组 1"
    }

    "逻辑删除文件节点" {
        FileService.findById(42) shouldNotBe null
        FileService.listChildren(4).first.map { it.id } shouldContain 42
        FileService.remove(42)
        FileService.findById(42) shouldBe null
        FileService.listChildren(4).first.map { it.id } shouldNotContain 42

        FileService.findById(55) shouldNotBe null
        FileService.listChildren(4).first.map { it.id } shouldContain 55
        FileService.remove(55)
        FileService.findById(55) shouldBe null
        FileService.listChildren(4).first.map { it.id } shouldNotContain 55

        // 文件夹
        FileService.listChildren(4).second shouldBeGreaterThan 0
        FileService.listChildren(27).second shouldBeGreaterThan 0
        FileService.listChildren(1).second shouldBeGreaterThan 1
        FileService.remove(4)
        FileService.listChildren(4).second shouldBe 0 // 子节点应被删除
        FileService.listChildren(27).second shouldBe 0 // 孙节点应被删除
        FileService.listChildren(1).second shouldBeGreaterThan 0 // 兄弟节点不应被删除

        shouldThrow<NotFoundException> {
            FileService.remove(11)
        }.message shouldBe "文件节点 11 不存在或已被删除"

        shouldThrow<NotFoundException> {
            FileService.remove(70)
        }.message shouldBe "文件节点 70 不存在或已被删除"

        shouldThrow<OperationNotAllowException> {
            FileService.remove(56)
        }.message shouldBe "根目录 禁止删除"
    }

}, FileDAO, GroupDAO, UserDAO)