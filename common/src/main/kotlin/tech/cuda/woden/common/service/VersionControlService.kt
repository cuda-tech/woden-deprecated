/* * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at
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

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.global.addEntity
import me.liuwj.ktorm.global.global
import me.liuwj.ktorm.global.select
import me.liuwj.ktorm.global.update
import tech.cuda.woden.common.service.dao.CommitmentDAO
import tech.cuda.woden.common.service.dao.WorkingTreeDAO
import tech.cuda.woden.common.service.dto.CommitmentDTO
import tech.cuda.woden.common.service.dto.WorkingTreeDTO
import tech.cuda.woden.common.service.dto.toCommitmentDTO
import tech.cuda.woden.common.service.dto.toWorkingTreeDTO
import tech.cuda.woden.common.service.exception.NotFoundException
import tech.cuda.woden.common.service.po.CommitmentPO
import tech.cuda.woden.common.service.po.WorkingTreePO
import java.time.LocalDateTime

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
object VersionControlService {

    private fun requireDirectory(node: WorkingTreePO) = require(node.name.split(".").size == 1) {
        "Tree Node ${node.name} must be a directory"
    }


    private fun requireNonDirectory(node: WorkingTreePO) = require(node.name.split(".").size > 1) {
        "Tree Node ${node.name} can not be a directory"
    }

    internal fun selectWorkingTreeById(nodeId: Int) = WorkingTreeDAO.select()
        .where { (WorkingTreeDAO.isRemove eq false) and (WorkingTreeDAO.id eq nodeId) }
        .map { WorkingTreeDAO.createEntity(it) }
        .firstOrNull()


    internal fun selectCommitmentById(commitId: Int) = CommitmentDAO.select()
        .where { (CommitmentDAO.isRemove eq false) and (CommitmentDAO.id eq commitId) }
        .map { CommitmentDAO.createEntity(it) }
        .firstOrNull()

    /**
     * 创建一个 workingTree 的根节点并返回
     */
    fun createWorkingTree(): WorkingTreeDTO {
        Database.global.useTransaction {
            val now = LocalDateTime.now()
            val workingTree = WorkingTreePO {
                this.name = "root"
                this.parentId = null
                this.stage = null
                this.commitId = null
                this.isRemove = false
                this.createTime = now
                this.updateTime = now
            }.also { WorkingTreeDAO.addEntity(it) }
            return workingTree.toWorkingTreeDTO()
        }
    }

    /**
     * 创建节点名称为[name]，父节点为[parentId]的 Working Tree 节点
     * 如果[parentId]对应的节点不存在或已被删除，则抛出[NotFoundException]
     * 如果[parentId]不为目录节点，则抛出[IllegalArgumentException]
     */
    fun createWorkingNode(name: String, parentId: Int): WorkingTreeDTO = Database.global.useTransaction {
        val parent = selectWorkingTreeById(parentId) ?: throw NotFoundException()
        requireDirectory(parent)
        val now = LocalDateTime.now()
        val workingTree = WorkingTreePO {
            this.name = name
            this.parentId = parent.id
            this.stage = null
            this.commitId = null
            this.isRemove = false
            this.createTime = now
            this.updateTime = now
        }.also { WorkingTreeDAO.addEntity(it) }
        return workingTree.toWorkingTreeDTO()
    }

    /**
     * 查找 working Tree 中节点 ID 为 [nodeId] 的发布历史
     * 如果[nodeId]节点不存在或已被删除，则抛出[NotFoundException]
     * 如果[nodeId]节点为目录节点，则抛出[IllegalArgumentException]
     */
    fun listingCommitHistory(nodeId: Int): List<CommitmentDTO> {
        val node = selectWorkingTreeById(nodeId) ?: throw NotFoundException()
        requireNonDirectory(node)
        return CommitmentDAO.select()
            .where { (CommitmentDAO.nodeId eq nodeId) and (CommitmentDAO.isRemove eq false) }
            .orderBy(CommitmentDAO.createTime.desc())
            .map { CommitmentDAO.createEntity(it).toCommitmentDTO() }
    }

    /**
     * 列出 WorkingTree 中节点 ID 为 [nodeId] 的所有子节点
     * 如果[nodeId]节点不存在或已被删除，则抛出[NotFoundException]
     * 如果[nodeId]节点不为目录节点，则抛出[IllegalArgumentException]
     */
    fun listingChildren(nodeId: Int): List<WorkingTreeDTO> {
        val node = selectWorkingTreeById(nodeId) ?: throw NotFoundException()
        requireDirectory(node)
        return WorkingTreeDAO.select()
            .where { (WorkingTreeDAO.isRemove eq false) and (WorkingTreeDAO.parentId eq node.id) }
            .map { WorkingTreeDAO.createEntity(it).toWorkingTreeDTO() }
    }


    /**
     * 删除 workingTree 中的节点 ID 为 [nodeId] 的节点
     * 如果[nodeId]节点不存在或已被删除，则抛出[NotFoundException]
     * 如果[nodeId]节点为根节点，则抛出[IllegalArgumentException]
     */
    fun remove(nodeId: Int) = Database.global.useTransaction {
        val node = selectWorkingTreeById(nodeId) ?: throw NotFoundException()
        require(node.parentId != null) { "can not remove root dir /" }
        node.isRemove = true
        node.updateTime = LocalDateTime.now()
        node.flushChanges()
    }

    /**
     * 获取节点 ID 为 [nodeId] 的当前 commit
     * 如果[nodeId]节点不存在或已被删除，则抛出[NotFoundException]
     * 如果[nodeId]节点为目录节点，则抛出[IllegalArgumentException]
     * 如果[nodeId]不存在已提交的 commit，则抛出[IllegalArgumentException]
     */
    fun getCurrentCommit(nodeId: Int): CommitmentDTO {
        val node = selectWorkingTreeById(nodeId) ?: throw NotFoundException()
        requireNonDirectory(node)
        require(node.commitId != null) { "current has no committed" }
        val commitment = selectCommitmentById(node.commitId!!) ?: throw NotFoundException()
        return commitment.toCommitmentDTO()
    }

    /**
     * 将 [content] 写入 ID 为 [nodeId] 的节点缓存区
     * 如果[nodeId]节点不存在或已被删除，则抛出[NotFoundException]
     * 如果[nodeId]节点为目录节点，则抛出[IllegalArgumentException]
     */
    fun writeStage(nodeId: Int, content: String) = Database.global.useTransaction {
        val node = selectWorkingTreeById(nodeId) ?: throw NotFoundException()
        requireNonDirectory(node)
        node.stage = content
        node.updateTime = LocalDateTime.now()
        node.flushChanges()
    }

    /**
     * 发布 ID 为 [nodeId] 的节点中的缓存区
     * 如果[nodeId]节点不存在或已被删除，则抛出[NotFoundException]
     * 如果[nodeId]为目录节点，则抛出[IllegalArgumentException]
     * 如果[nodeId]的缓存区为 null，则抛出[IllegalArgumentException]
     */
    fun commit(nodeId: Int, message: String): CommitmentDTO = Database.global.useTransaction {
        val node = selectWorkingTreeById(nodeId) ?: throw NotFoundException()
        requireNonDirectory(node)
        require(node.stage != null) { "Stage of tree node ${node.name} is null" }
        val now = LocalDateTime.now()
        val commitment = CommitmentPO {
            this.nodeId = node.id
            this.content = node.stage!!
            this.message = message
            this.isRemove = false
            this.createTime = now
            this.updateTime = now
        }.also { CommitmentDAO.addEntity(it) }
        WorkingTreeDAO.update {
            set(it.commitId, commitment.id)
            set(it.updateTime, now)
            where { it.id eq node.id }
        }
        return commitment.toCommitmentDTO()
    }

    /**
     * 将 ID 为 [nodeId] 的节点回滚为 ID 为 [commitID] 的 commitment
     * 如果[nodeId]节点或已被删除，则抛出[NotFoundException]
     * 如果[commitID]提交不存在或已被删除，则抛出[NotFoundException]
     * 如果[nodeId]为目录节点，则抛出[IllegalArgumentException]
     * 如果[commitID]不归属与 [nodeId]，则抛出[IllegalStateException]
     */
    fun rollback(nodeId: Int, commitID: Int) = Database.global.useTransaction {
        val node = selectWorkingTreeById(nodeId) ?: throw NotFoundException()
        val commitment = selectCommitmentById(commitID) ?: throw NotFoundException()
        requireNonDirectory(node)
        check(commitment.nodeId == node.id) { "commitment ${commitment.id} do not belong to node ${node.id}" }
        node.stage = commitment.content
        node.commitId = commitment.id
        node.updateTime = LocalDateTime.now()
        node.flushChanges()
    }


}