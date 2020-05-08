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
package tech.cuda.datahub.service.i18n

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
interface Language {
    val user: String
    val group: String
    val machine: String
    val file: String
    val fileMirror: String
    val task: String
    val job: String
    val instance: String
    val notExistsOrHasBeenRemove: String
    val operationNotAllow: String
    val notBelongTo: String
    val parentTask: String
    val childrenTask: String
    val invalid: String
    val dependencyNotAllow: String
    val removeNotAllow: String
    val isValid: String
    val crossFileUpdateMirrorNotAllow: String
    val invalidNotAllow: String
}


