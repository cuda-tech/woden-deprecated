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
package tech.cuda.datahub.scheduler.livy

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
sealed class LivyResponse

data class ListingSessionResponse(val from: Int, val total: Int, val sessions: List<Session>) : LivyResponse()
data class SessionStateResponse(val id: Int, val state: SessionState) : LivyResponse()
data class SessionLogResponse(val id: Int, val from: Int, val size: Int, val log: List<String>) : LivyResponse()
data class MessageResponse(val msg: String) : LivyResponse()

