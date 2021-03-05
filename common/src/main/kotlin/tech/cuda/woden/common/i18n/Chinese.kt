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
package tech.cuda.woden.common.i18n

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
data class Chinese(
    override val user: String = "用户",
    override val team: String = "项目组",
    override val notExistsOrHasBeenRemove: String = "不存在或已被删除",
    override val container: String = "调度容器",
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
    override val isNot: String = "不是",
    override val businessSolution: String = "解决方案",
    override val exists: String = "存在",
    override val fileType: String = "文件类型",
    override val updateNotAllow: String = "禁止更新",
    override val existsAlready: String = "已存在",
    override val hasBeenRemove: String = "已被删除",
    override val illegal: String = "非法",
    override val scheduleFormat: String = "调度时间格式",
    override val missing: String = "缺失",
    override val createInstanceNotAllow: String = "禁止创建调度实例",
    override val status: String = "状态",
    override val canNotUpdateTo: String = "禁止更新为",
    override val hostname: String = "主机名"
) : Language
