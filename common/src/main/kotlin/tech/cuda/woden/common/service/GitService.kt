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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import tech.cuda.woden.common.configuration.Woden
import tech.cuda.woden.common.service.dto.TeamDTO
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileFilter
import java.lang.IllegalStateException
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object GitService {
    private const val gitKeep = ".gitkeep"
    private val ignore = setOf(gitKeep, ".git")

    private var git: Git = loadOrInit()

    private fun String.toAbsolutePath(): Path {
        var dir = Woden.gitPath
        this.split("/").filter { it.isNotEmpty() }.forEach {
            dir = dir.resolve(it)
        }
        return dir
    }

    private fun String.toFile(): File = this.toAbsolutePath().toFile()

    private fun String.toGitPath() = this.trimStart('/')

    /**
     * 加载 Git 仓库，如果加载失败，则初始化 Git 仓库
     */
    private fun loadOrInit(): Git {
        return try {
            Git.open(Woden.gitPath.resolve(".git").toFile())
        } catch (e: Exception) {
            Git.init().setDirectory(Woden.gitPath.toFile()).call().also {
                it.commit().setMessage("Initial commit").call()
            }
        }
    }

    /**
     * 获取[relativePath]文件的提交历史
     */
    fun listingHistory(relativePath: String): List<Triple<String, String, LocalDateTime>> {
        val path = relativePath.toAbsolutePath()
        check(path.toFile().isFile) { "$relativePath is not file" }
        return git.log().addPath(relativePath.toGitPath()).call().toList().map {
            Triple(
                it.name,
                it.fullMessage,
                LocalDateTime.ofInstant(Instant.ofEpochSecond(it.commitTime.toLong()), ZoneId.systemDefault())
            )
        }
    }

    /**
     * 为[team]创建目录，并追加[gitKeep]文件，提交到 git 仓库
     * 如果[team]已存在，则抛出 IllegalStateException
     */
    fun createDirectorForTeam(team: TeamDTO) {
        val teamPath = Woden.gitPath.resolve(team.name)
        check(!teamPath.toFile().exists()) { "dir /${team.name} exists already" }
        teamPath.toFile().mkdir()
        FileUtils.touch(teamPath.resolve(gitKeep).toFile())
        git.add().addFilepattern(team.name).call()
        git.commit().setMessage("create team ${team.name}").call()
    }

    /**
     * 列出[relativePath]下的所有文件和文件夹
     * 如果[relativePath]不是目录，则抛出 IllegalStateException
     */
    fun listingDirectory(relativePath: String): List<String> {
        val absolutePath = relativePath.toFile()
        check(absolutePath.isDirectory) { "$relativePath is not dir" }
        val dirs = absolutePath.listFiles(FileFilterUtils.directoryFileFilter() as FileFilter)
            ?.filter { !ignore.contains(it.name) }
            ?.map { "${it.name}/" }
            ?: listOf()
        val files = absolutePath.listFiles(FileFilterUtils.fileFileFilter() as FileFilter)
            ?.filter { !ignore.contains(it.name) }
            ?.map { it.name }
            ?: listOf()
        return dirs + files
    }

    /**
     * 删除[relativePath]文件夹
     */
    fun removeDirectory(relativePath: String) {
        require(relativePath.trim() != "/") { "can not remove root dir /" }
        val absolutePath = relativePath.toFile()
        check(absolutePath.isDirectory) { "$relativePath is not dir" }
        FileUtils.deleteDirectory(absolutePath)
        git.rm().addFilepattern(relativePath.toGitPath()).call()
        git.commit().setMessage("remove $relativePath").call()
    }

    /**
     * 读取位于[relativePath]的文件内容
     */
    fun readFile(relativePath: String): String {
        val file = relativePath.toFile()
        check(file.isFile) { "$relativePath is not file" }
        return FileUtils.readFileToString(file)
    }

    /**
     * 读取位于[relativePath]的文件在[commitId]时的内容
     * 如果[relativePath]不存在[commitId]的提交，则抛出 IllegalStateException
     * 如果[commitId]下文件[relativePath]不存在，则返回空字符串
     */
    fun readFile(relativePath: String, commitId: String): String {
        val path = relativePath.toAbsolutePath()
        check(path.toFile().isFile) { "$relativePath is not file" }
        RevWalk(git.repository).use { revWalk ->
            TreeWalk(git.repository).use { treeWalk ->
                val tree = try {
                    revWalk.parseCommit(git.repository.resolve(commitId)).tree
                } catch (e: Exception) {
                    throw IllegalStateException("commit id $commitId not exists")
                }
                treeWalk.addTree(tree)
                treeWalk.isRecursive = true
                treeWalk.filter = PathFilter.create(relativePath.toGitPath())
                if (!treeWalk.next()) {
                    return ""
                }
                val loader = git.repository.open(treeWalk.getObjectId(0))
                return ByteArrayOutputStream().also { loader.copyTo(it) }.toString()
            }
        }
    }

    /**
     * 将[content]写入位于[relativePath]的文件，并提交到仓库
     * 如果[relativePath]存在，且为目录，则抛出 IllegalStateException
     */
    fun writeFile(relativePath: String, content: String, message: String) {
        val file = relativePath.toFile()
        check(!file.isDirectory) { "can not overwrite directory $relativePath" }
        FileUtils.writeStringToFile(file, content)
        git.add().addFilepattern(relativePath.toGitPath()).call()
        git.commit().setMessage(message).call()
    }

    /**
     * revert 指定的[commitId]
     */
    fun rollbackTo(relativePath: String, commitId: String) {
        check(relativePath.toFile().isFile) { "$relativePath is not file" }
        git.repository.resolve(commitId)
        git.checkout().setStartPoint(commitId).addPath(relativePath.toGitPath()).call()
        git.commit().setMessage("rollback $relativePath to $commitId").call()
    }

    /**
     * 删除位于[relativePath]的文件
     */
    fun removeFile(relativePath: String) {
        val file = relativePath.toFile()
        check(file.isFile) { "$relativePath is not file" }
        file.delete()
        git.rm().addFilepattern(relativePath.toGitPath()).call()
        git.commit().setMessage("remove $relativePath").call()
    }

    /**
     * 重置 Git 仓库，只用于单测
     */
    internal fun reset() {
        this.git = loadOrInit()
    }


}