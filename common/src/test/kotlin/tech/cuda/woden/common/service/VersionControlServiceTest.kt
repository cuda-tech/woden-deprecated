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

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tech.cuda.woden.common.service.dao.CommitmentDAO
import tech.cuda.woden.common.service.dao.WorkingTreeDAO
import tech.cuda.woden.common.service.exception.NotFoundException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class VersionControlServiceTest : TestWithMaria({
    "创建 WorkingTree" {
        val workingTree = VersionControlService.createWorkingTree()
        workingTree.id shouldBe 252
        workingTree.name shouldBe "root"
        workingTree.parentId shouldBe null
        workingTree.stage shouldBe null
        workingTree.commitId shouldBe null
    }

    "创建节点" {
        // 目录节点
        VersionControlService.listingChildren(3).map { it.name } shouldNotContain "some_directory"
        val directoryNode = VersionControlService.createWorkingNode("some_directory", 3)
        VersionControlService.listingChildren(3).map { it.name } shouldContain "some_directory"

        // 非目录节点
        VersionControlService.listingChildren(3).map { it.name } shouldNotContain "some_script.py"
        val scriptNode = VersionControlService.createWorkingNode("some_script.py", 3)
        VersionControlService.listingChildren(3).map { it.name } shouldContain "some_script.py"

        // parent 节点不存在
        shouldThrow<NotFoundException> {
            VersionControlService.createWorkingNode("parent_not_found", 300)
        }

        // parent 节点已删除
        shouldThrow<NotFoundException> {
            VersionControlService.createWorkingNode("parent_not_found", 26)
        }

        // parent 为目录
        shouldNotThrowAny {
            VersionControlService.createWorkingNode("parent_is_directory", directoryNode.id)
        }

        // parent 不为目录
        shouldThrow<IllegalArgumentException> {
            VersionControlService.createWorkingNode("parent_is_not_directory", scriptNode.id)
        }
    }

    "获取直属节点" {
        val children = VersionControlService.listingChildren(3)
        children.size shouldBe 11
        children.map { it.name } shouldContainExactlyInAnyOrder listOf(
            "ktaoxmyy.sql", "ytgfyyph", "aivdhviw.py", "wxrukuui.sh", "doxpwoqv", "bjwhlfkk.sql",
            "yqyaussz.py", "noratlqg.sql", "iowxyysf.sh", "vzpzrwni", "fydbduda"
        )

        // 非目录节点
        shouldThrow<IllegalArgumentException> { VersionControlService.listingChildren(1) }

        // 已删除节点
        shouldThrow<NotFoundException> { VersionControlService.listingChildren(26) }

        // 不存在节点
        shouldThrow<NotFoundException> { VersionControlService.listingChildren(300) }
    }

    "删除节点" {
        // 目录节点
        VersionControlService.selectWorkingTreeById(42) shouldNotBe null
        VersionControlService.remove(42)
        VersionControlService.selectWorkingTreeById(42) shouldBe null

        // 非目录节点
        VersionControlService.selectWorkingTreeById(27) shouldNotBe null
        VersionControlService.remove(27)
        VersionControlService.selectWorkingTreeById(27) shouldBe null

        // 根节点
        shouldThrow<IllegalArgumentException> { VersionControlService.remove(3) }

        // 已删除节点
        shouldThrow<NotFoundException> { VersionControlService.remove(26) }

        // 不存在节点
        shouldThrow<NotFoundException> { VersionControlService.remove(300) }

    }

    "写入 Staged" {
        VersionControlService.selectWorkingTreeById(1)!!.stage shouldBe "jtnpnjtf"
        VersionControlService.writeStage(1, "something")
        VersionControlService.selectWorkingTreeById(1)!!.stage shouldBe "something"

        // 目录节点
        shouldThrow<IllegalArgumentException> { VersionControlService.writeStage(3, "directory node") }

        // 已删除节点
        shouldThrow<NotFoundException> { VersionControlService.writeStage(26, "remove node") }

        // 不存在节点
        shouldThrow<NotFoundException> { VersionControlService.writeStage(300, "node not exists") }
    }

    "获取当前 Commitment" {
        val commitment = VersionControlService.getCurrentCommit(1)
        commitment.id shouldBe 4
        commitment.nodeId shouldBe 1
        commitment.content shouldBe "yxtjlpaandogotcw"
        commitment.message shouldBe "dkrcboyx"

        // 没有 commit
        shouldThrow<IllegalArgumentException> { VersionControlService.getCurrentCommit(2) }

        // 目录节点
        shouldThrow<IllegalArgumentException> { VersionControlService.getCurrentCommit(3) }

        // 已删除节点
        shouldThrow<NotFoundException> { VersionControlService.getCurrentCommit(26) }

        // 不存在节点
        shouldThrow<NotFoundException> { VersionControlService.getCurrentCommit(300) }

    }

    "获取 commit 历史" {
        val commitHistory = VersionControlService.listingCommitHistory(1)
        commitHistory.size shouldBe 7
        commitHistory.map { it.id } shouldContainInOrder listOf(195, 4, 79, 109, 160, 177, 142)

        // 目录节点
        shouldThrow<IllegalArgumentException> { VersionControlService.listingCommitHistory(3) }

        // 已删除节点
        shouldThrow<NotFoundException> { VersionControlService.listingCommitHistory(26) }

        // 不存在节点
        shouldThrow<NotFoundException> { VersionControlService.listingCommitHistory(300) }
    }

    "提交 commit" {
        val currentCommit = VersionControlService.getCurrentCommit(1)
        currentCommit.id shouldBe 4
        currentCommit.nodeId shouldBe 1
        currentCommit.content shouldBe "yxtjlpaandogotcw"
        currentCommit.message shouldBe "dkrcboyx"
        VersionControlService.listingCommitHistory(1).map { it.id } shouldNotContain 201

        VersionControlService.commit(1, "new commit")
        val newCommit = VersionControlService.getCurrentCommit(1)
        newCommit.id shouldBe 201
        newCommit.nodeId shouldBe 1
        newCommit.content shouldBe "jtnpnjtf"
        newCommit.message shouldBe "new commit"
        VersionControlService.listingCommitHistory(1).map { it.id } shouldContain 201

        // 缓冲区为 null 的节点
        shouldThrow<IllegalArgumentException> { VersionControlService.commit(4, "stage is null") }

        // 目录节点
        shouldThrow<IllegalArgumentException> { VersionControlService.commit(3, "directory node") }

        // 已删除节点
        shouldThrow<NotFoundException> { VersionControlService.commit(26, "node has been remove") }

        // 不存在节点
        shouldThrow<NotFoundException> { VersionControlService.commit(300, "node not exists") }

    }

    "回滚" {
        val currentCommit = VersionControlService.getCurrentCommit(1)
        currentCommit.id shouldBe 4
        currentCommit.nodeId shouldBe 1
        currentCommit.content shouldBe "yxtjlpaandogotcw"
        currentCommit.message shouldBe "dkrcboyx"

        VersionControlService.rollback(1, 195)

        val rollbackCommit = VersionControlService.getCurrentCommit(1)
        rollbackCommit.id shouldBe 195
        rollbackCommit.nodeId shouldBe 1
        rollbackCommit.content shouldBe "ycbeopkjbsgkzfdm"
        rollbackCommit.message shouldBe "eotlccag"

        // 越权回滚
        shouldThrow<IllegalStateException> { VersionControlService.rollback(1, 1) }

        // 目录节点
        shouldThrow<IllegalArgumentException> { VersionControlService.rollback(3, 195) }

        // 已删除节点
        shouldThrow<NotFoundException> { VersionControlService.rollback(26, 195) }

        // 不存在节点
        shouldThrow<NotFoundException> { VersionControlService.rollback(300, 195) }

    }
}, WorkingTreeDAO, CommitmentDAO)