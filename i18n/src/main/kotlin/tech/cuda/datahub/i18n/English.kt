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
package tech.cuda.datahub.i18n

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
data class English(
    override val user: String = "user",
    override val group: String = "group",
    override val notExistsOrHasBeenRemove: String = "does not exists or has been remove",
    override val machine: String = "scheduler machine",
    override val file: String = "file node",
    override val fileMirror: String = "file mirror",
    override val task: String = "schedule task",
    override val job: String = "schedule job",
    override val instance: String = "schedule instance",
    override val operationNotAllow: String = "operation not allow",
    override val notBelongTo: String = "not belong to",
    override val parentTask: String = "parent task",
    override val invalid: String = "is invalid",
    override val dependencyNotAllow: String = "dependency not allow",
    override val removeNotAllow: String = "remove not allow",
    override val isValid: String = "is valid",
    override val childrenTask: String = "children task",
    override val crossFileUpdateMirrorNotAllow: String = "cross file update mirror not allow",
    override val invalidNotAllow: String = "invalid not allow",
    override val dir: String = "directory",
    override val createMirrorNotAllow: String = "create mirror not allow",
    override val parentNode: String = "parent node",
    override val rootDir: String = "root directory",
    override val mustBe: String = "must be",
    override val canNot: String = "can not",
    override val get: String = "get",
    override val content: String = "content",
    override val isNot: String = "is not",
    override val businessSolution: String = "business solution",
    override val exists: String = "exists",
    override val fileType: String = "file type",
    override val updateNotAllow: String = "update not allow",
    override val ipAddress: String = "ip address",
    override val existsAlready: String = "exists already"
) : Language
