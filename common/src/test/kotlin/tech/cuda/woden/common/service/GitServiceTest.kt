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
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import org.apache.commons.io.FileUtils
import tech.cuda.woden.common.configuration.Woden
import tech.cuda.woden.common.service.dao.PersonDAO
import tech.cuda.woden.common.service.dao.PersonTeamMappingDAO
import tech.cuda.woden.common.service.dao.TeamDAO
import tech.cuda.woden.common.service.dto.TeamDTO
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.LocalDateTime


/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class GitServiceTest : TestWithMaria({
    "获取目录下所有内容" {
        GitService.listingDirectory("/root") shouldContainExactlyInAnyOrder listOf(
            "dir1/",
            "dir2/",
            "file1.txt",
            "file2.txt"
        )
        GitService.listingDirectory("/root/dir1") shouldContainExactlyInAnyOrder listOf("dir3/", "file3.txt")
        GitService.listingDirectory("/root/dir2") shouldContainExactlyInAnyOrder listOf("file4.txt")
        GitService.listingDirectory("/root/dir1/dir3") shouldContainExactlyInAnyOrder listOf("file5.txt")

        shouldThrow<IllegalStateException> {
            GitService.listingDirectory("/root/file2.txt")
        }.message shouldBe "/root/file2.txt is not dir"

        shouldThrow<IllegalStateException> {
            GitService.listingDirectory("/root/dir3")
        }.message shouldBe "/root/dir3 is not dir"
    }

    "读取文件历史" {
        GitService.listingHistory("/root/file1.txt")
            .map { it.second } shouldContainExactly listOf(
            "update file1 to version5",
            "update file1 to version4",
            "update file1 to version3",
            "update file1 to version2",
            "create file1"
        )

        GitService.listingHistory("/root/file2.txt")
            .map { it.second } shouldContainExactly listOf(
            "update file2 to version4",
            "update file2 to version3",
            "update file2 to version2",
            "create file2"
        )


        GitService.listingHistory("/root/dir1/file3.txt")
            .map { it.second } shouldContainExactly listOf(
            "update file3 to version3",
            "update file3 to version2",
            "create file3"
        )

        GitService.listingHistory("/root/dir2/file4.txt")
            .map { it.second } shouldContainExactly listOf(
            "update file4 to version2",
            "create file4"
        )

        GitService.listingHistory("/root/dir1/dir3/file5.txt")
            .map { it.second } shouldContainExactly listOf("create file5")

        shouldThrow<IllegalStateException> {
            GitService.listingHistory("/root/dir1")
        }.message shouldBe "/root/dir1 is not file"

        shouldThrow<IllegalStateException> {
            GitService.listingHistory("/root/file3.txt")
        }.message shouldBe "/root/file3.txt is not file"
    }

    "读取文件内容" {
        GitService.readFile("/root/file1.txt") shouldBe "file1 in version5"
        GitService.readFile("/root/file2.txt") shouldBe "file2 in version4"
        GitService.readFile("/root/dir1/file3.txt") shouldBe "file3 in version3"
        GitService.readFile("/root/dir2/file4.txt") shouldBe "file4 in version2"
        GitService.readFile("/root/dir1/dir3/file5.txt") shouldBe "file5 in version1"

        shouldThrow<IllegalStateException> {
            GitService.readFile("/root/file3.txt")
        }.message shouldBe "/root/file3.txt is not file"

        shouldThrow<IllegalStateException> {
            GitService.readFile("/root/dir1")
        }.message shouldBe "/root/dir1 is not file"
    }

    "读取指定版本的文件内容" {
        GitService.listingHistory("/root/file1.txt").map { (commitId, _, _) ->
            GitService.readFile("/root/file1.txt", commitId)
        } shouldContainInOrder listOf(
            "file1 in version5",
            "file1 in version4",
            "file1 in version3",
            "file1 in version2",
            "file1 in version1",
        )

        shouldThrow<IllegalStateException> {
            GitService.readFile("/root/file1.txt", "xxxxxxxx")
        }

        GitService.listingHistory("/root/file2.txt").map { (commitId, _, _) ->
            GitService.readFile("/root/file2.txt", commitId)
        } shouldContainInOrder listOf(
            "file2 in version4",
            "file2 in version3",
            "file2 in version2",
            "file2 in version1",
        )

        GitService.listingHistory("/root/dir1/file3.txt").map { (commitId, _, _) ->
            GitService.readFile("/root/dir1/file3.txt", commitId)
        } shouldContainInOrder listOf(
            "file3 in version3",
            "file3 in version2",
            "file3 in version1",
        )

        GitService.listingHistory("/root/dir2/file4.txt").map { (commitId, _, _) ->
            GitService.readFile("/root/dir2/file4.txt", commitId)
        } shouldContainInOrder listOf(
            "file4 in version2",
            "file4 in version1",
        )

        GitService.listingHistory("/root/dir1/dir3/file5.txt").map { (commitId, _, _) ->
            GitService.readFile("/root/dir1/dir3/file5.txt", commitId)
        } shouldContainInOrder listOf("file5 in version1")

        shouldThrow<IllegalStateException> {
            GitService.listingHistory("/root/dir1")
        }.message shouldBe "/root/dir1 is not file"

        shouldThrow<IllegalStateException> {
            GitService.listingHistory("/root/file3.txt")
        }.message shouldBe "/root/file3.txt is not file"
    }

    "写入文件" {
        // 更新已有文件
        GitService.listingHistory("/root/file1.txt").map { (commitId, _, _) ->
            GitService.readFile("/root/file1.txt", commitId)
        } shouldContainInOrder listOf(
            "file1 in version5",
            "file1 in version4",
            "file1 in version3",
            "file1 in version2",
            "file1 in version1",
        )
        GitService.writeFile("/root/file1.txt", "file1 in version6", "update file1 to version6")
        GitService.readFile("/root/file1.txt") shouldBe "file1 in version6"
        val file1History = GitService.listingHistory("/root/file1.txt").map { (commitId, message, _) ->
            GitService.readFile("/root/file1.txt", commitId) to message
        }
        file1History.map { it.first } shouldContainInOrder listOf(
            "file1 in version6",
            "file1 in version5",
            "file1 in version4",
            "file1 in version3",
            "file1 in version2",
            "file1 in version1",
        )
        file1History.map { it.second } shouldContainInOrder listOf(
            "update file1 to version6",
            "update file1 to version5",
            "update file1 to version4",
            "update file1 to version3",
            "update file1 to version2",
            "create file1"
        )

        // 新增文件
        GitService.listingDirectory("/root") shouldNotContain "file6.txt"
        GitService.writeFile("/root/file6.txt", "file6 in version1", "create file6")
        GitService.listingDirectory("/root") shouldContain "file6.txt"
        GitService.readFile("/root/file6.txt") shouldBe "file6 in version1"
        val file6History = GitService.listingHistory("/root/file6.txt").map { (commitId, message) ->
            GitService.readFile("/root/file6.txt", commitId) to message
        }
        file6History.map { it.first } shouldContainInOrder listOf("file6 in version1")
        file6History.map { it.second } shouldContainInOrder listOf("create file6")

        // 递归新增文件&文件夹
        GitService.listingDirectory("/root") shouldNotContain "dir7/"
        GitService.writeFile("/root/dir7/dir8/file78.txt", "file78 in version1", "create file78")
        GitService.listingDirectory("/root") shouldContain "dir7/"
        GitService.listingDirectory("/root/dir7") shouldContainInOrder listOf("dir8/")
        GitService.listingDirectory("/root/dir7/dir8") shouldContainInOrder listOf("file78.txt")
        GitService.readFile("/root/dir7/dir8/file78.txt") shouldBe "file78 in version1"
        val file7History = GitService.listingHistory("/root/dir7/dir8/file78.txt").map { (commitId, message) ->
            GitService.readFile("/root/dir7/dir8/file78.txt", commitId) to message
        }
        file7History.map { it.first } shouldContainInOrder listOf("file78 in version1")
        file7History.map { it.second } shouldContainInOrder listOf("create file78")

        // 覆盖目录
        shouldThrow<IllegalStateException> {
            GitService.writeFile("/root/dir1", "content", "message")
        }.message shouldBe "can not overwrite directory /root/dir1"
    }

    "回滚文件到指定版本号"{
        val file1HistoryCommitIds = GitService.listingHistory("/root/file1.txt").map { (commitId, _, _) ->
            commitId to GitService.readFile("/root/file1.txt", commitId)
        }

        file1HistoryCommitIds.size shouldBe 5
        GitService.readFile("/root/file1.txt") shouldBe file1HistoryCommitIds[0].second

        GitService.rollbackTo("/root/file1.txt", file1HistoryCommitIds[0].first)
        GitService.listingHistory("/root/file1.txt").size shouldBe 5
        GitService.readFile("/root/file1.txt") shouldBe file1HistoryCommitIds[0].second

        GitService.rollbackTo("/root/file1.txt", file1HistoryCommitIds[1].first)
        GitService.listingHistory("/root/file1.txt").size shouldBe 6
        GitService.readFile("/root/file1.txt") shouldBe file1HistoryCommitIds[1].second

        GitService.rollbackTo("/root/file1.txt", file1HistoryCommitIds[2].first)
        GitService.listingHistory("/root/file1.txt").size shouldBe 7
        GitService.readFile("/root/file1.txt") shouldBe file1HistoryCommitIds[2].second

        GitService.rollbackTo("/root/file1.txt", file1HistoryCommitIds[3].first)
        GitService.listingHistory("/root/file1.txt").size shouldBe 8
        GitService.readFile("/root/file1.txt") shouldBe file1HistoryCommitIds[3].second

        GitService.rollbackTo("/root/file1.txt", file1HistoryCommitIds[4].first)
        GitService.listingHistory("/root/file1.txt").size shouldBe 9
        GitService.readFile("/root/file1.txt") shouldBe file1HistoryCommitIds[4].second

        GitService.rollbackTo("/root/file1.txt", file1HistoryCommitIds[0].first)
        GitService.listingHistory("/root/file1.txt").size shouldBe 10
        GitService.readFile("/root/file1.txt") shouldBe file1HistoryCommitIds[0].second

        shouldThrow<IllegalStateException> {
            GitService.rollbackTo("/root/file3.txt", "xxx")
        }.message shouldBe "/root/file3.txt is not file"

        shouldThrow<IllegalStateException> {
            GitService.rollbackTo("/root/dir1", "xxx")
        }.message shouldBe "/root/dir1 is not file"
    }

    "删除文件" {
        GitService.listingDirectory("/root") shouldContain "file1.txt"
        GitService.readFile("/root/file1.txt") shouldBe "file1 in version5"
        GitService.listingHistory("/root/file1.txt").size shouldBe 5

        GitService.removeFile("/root/file1.txt")

        GitService.listingDirectory("/root") shouldNotContain "file1.txt"
        shouldThrow<IllegalStateException> {
            GitService.readFile("/root/file1.txt")
        }.message shouldBe "/root/file1.txt is not file"
        shouldThrow<IllegalStateException> {
            GitService.listingHistory("/root/file1.txt")
        }.message shouldBe "/root/file1.txt is not file"

        GitService.writeFile("/root/file1.txt", "file1 in version6", "create file1 again")
        GitService.readFile("/root/file1.txt") shouldBe "file1 in version6"
        val file1History = GitService.listingHistory("/root/file1.txt").map { (commitId, msg, _) ->
            GitService.readFile("/root/file1.txt", commitId) to msg
        }
        file1History.map { it.first } shouldContainInOrder listOf(
            "file1 in version6",
            "",
            "file1 in version5",
            "file1 in version4",
            "file1 in version3",
            "file1 in version2",
            "file1 in version1",
        )
        file1History.map { it.second } shouldContainInOrder listOf(
            "create file1 again",
            "remove /root/file1.txt",
            "update file1 to version5",
            "update file1 to version4",
            "update file1 to version3",
            "update file1 to version2",
            "create file1",
        )

        shouldThrow<IllegalStateException> {
            GitService.removeFile("/root/file3.txt")
        }.message shouldBe "/root/file3.txt is not file"

        shouldThrow<IllegalStateException> {
            GitService.removeFile("/root/dir1")
        }.message shouldBe "/root/dir1 is not file"
    }

    "删除文件夹" {
        GitService.listingDirectory("/root") shouldContain "dir1/"
        GitService.listingHistory("/root/dir1/file3.txt").size shouldBe 3
        GitService.readFile("/root/dir1/file3.txt") shouldBe "file3 in version3"

        GitService.removeDirectory("/root/dir1")

        GitService.listingDirectory("/root") shouldNotContain "dir1/"
        shouldThrow<IllegalStateException> {
            GitService.listingHistory("/root/dir1/file3.txt")
        }.message shouldBe "/root/dir1/file3.txt is not file"
        shouldThrow<IllegalStateException> {
            GitService.readFile("/root/dir1/file3.txt")
        }.message shouldBe "/root/dir1/file3.txt is not file"

        GitService.listingDirectory("/") shouldContain "anksepij/"
        GitService.removeDirectory("/anksepij")
        GitService.listingDirectory("/") shouldNotContain "anksepij/"

        shouldThrow<IllegalStateException> {
            GitService.removeDirectory("/root/dir3")
        }.message shouldBe "/root/dir3 is not dir"

        shouldThrow<IllegalStateException> {
            GitService.removeDirectory("/root/file1.txt")
        }.message shouldBe "/root/file1.txt is not dir"

        shouldThrow<IllegalArgumentException> {
            GitService.removeDirectory("/")
        }.message shouldBe "can not remove root dir /"
    }

    "创建项目组目录" {
        GitService.listingDirectory("/") shouldNotContain "new_team"

        val newTeam = TeamDTO(1, "new_team", LocalDateTime.now(), LocalDateTime.now())
        GitService.createDirectorForTeam(newTeam)

        GitService.listingDirectory("/") shouldContain "new_team/"
        GitService.listingHistory("/new_team/.gitkeep").size shouldBe 1

        shouldThrow<IllegalStateException> {
            GitService.createDirectorForTeam(newTeam)
        }.message shouldBe "dir /new_team exists already"
    }

}, PersonDAO, TeamDAO, PersonTeamMappingDAO) {

    override fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        FileUtils.deleteDirectory(Woden.gitPath.toFile())
        GitService.reset()
        GitService.createDirectorForTeam(TeamDTO(1, "root", LocalDateTime.now(), LocalDateTime.now()))
        GitService.createDirectorForTeam(TeamDTO(2, "anksepij", LocalDateTime.now(), LocalDateTime.now()))
        /** mock 仓库下的 root 目录
         * /root
         * |-- dir1
         * |   |-- dir3
         * |   |   +-- file5(v1)
         * |   +-- file3.txt(v3)
         * |-- dir2
         * |   +-- file4.txt(v2)
         * |-- file1.txt(v5)
         * +-- file2.txt(v4)
         */
        GitService.writeFile("/root/file1.txt", "file1 in version1", "create file1")
        GitService.writeFile("/root/file1.txt", "file1 in version2", "update file1 to version2")
        GitService.writeFile("/root/file1.txt", "file1 in version3", "update file1 to version3")
        GitService.writeFile("/root/file1.txt", "file1 in version4", "update file1 to version4")
        GitService.writeFile("/root/file1.txt", "file1 in version5", "update file1 to version5")

        GitService.writeFile("/root/file2.txt", "file2 in version1", "create file2")
        GitService.writeFile("/root/file2.txt", "file2 in version2", "update file2 to version2")
        GitService.writeFile("/root/file2.txt", "file2 in version3", "update file2 to version3")
        GitService.writeFile("/root/file2.txt", "file2 in version4", "update file2 to version4")

        GitService.writeFile("/root/dir1/file3.txt", "file3 in version1", "create file3")
        GitService.writeFile("/root/dir1/file3.txt", "file3 in version2", "update file3 to version2")
        GitService.writeFile("/root/dir1/file3.txt", "file3 in version3", "update file3 to version3")

        GitService.writeFile("/root/dir2/file4.txt", "file4 in version1", "create file4")
        GitService.writeFile("/root/dir2/file4.txt", "file4 in version2", "update file4 to version2")

        GitService.writeFile("/root/dir1/dir3/file5.txt", "file5 in version1", "create file5")
    }

    override fun afterEach(testCase: TestCase, result: TestResult) {
        super.afterEach(testCase, result)
        FileUtils.deleteDirectory(Woden.gitPath.toFile())
    }

}