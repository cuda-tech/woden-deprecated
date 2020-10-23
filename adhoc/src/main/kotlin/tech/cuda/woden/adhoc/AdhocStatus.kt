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
package tech.cuda.woden.adhoc

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
enum class AdhocStatus {
    NOT_START {
        override val isFinish = false
    },
    RUNNING {
        override val isFinish = false
    },
    SUCCESS {
        override val isFinish = true
    },
    FAILED {
        override val isFinish = true
    },
    KILLED {
        override val isFinish = true
    };

    abstract val isFinish: Boolean
}