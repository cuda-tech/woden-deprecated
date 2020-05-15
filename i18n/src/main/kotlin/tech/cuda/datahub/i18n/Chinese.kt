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
data class Chinese(
    override val user: String = "用户",
    override val group: String = "项目组",
    override val notExistsOrHasBeenRemove: String = "不存在或已被删除",
    override val machine: String = "调度服务器",
    override val file: String = "文件节点",
    override val fileMirror: String = "文件镜像",
    override val task: String = "调度任务",
    override val job: String = "调度作业",
    override val instance: String = "调度实例",
    override val operationNotAllow: String = "非法操作",
    override val notBelongTo: String = "不归属于",
    override val parentTask: String = "父任务",
    override val invalid: String = "已失效",
    override val dependencyNotAllow: String = "禁止依赖",
    override val removeNotAllow: String = "禁止删除",
    override val isValid: String = "未失效",
    override val childrenTask: String = "子任务",
    override val crossFileUpdateMirrorNotAllow: String = "禁止跨文件更新镜像",
    override val invalidNotAllow: String = "禁止失效",
    override val dir: String = "文件夹",
    override val createMirrorNotAllow: String = "禁止创建镜像",
    override val parentNode: String = "父节点",
    override val rootDir: String = "根目录",
    override val mustBe: String = "必须是",
    override val canNot: String = "不允许",
    override val get: String = "获取",
    override val content: String = "内容",
    override val isNot: String = "不是"
) : Language
