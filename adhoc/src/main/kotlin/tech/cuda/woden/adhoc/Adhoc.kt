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
interface Adhoc {

    val output: String

    val status: AdhocStatus

    /**
     * 异步地执行作业
     */
    fun start()

    fun beforeStart() {}
    fun afterStart() {}

    /**
     * 作业完成后的回调，常用于环境清理
     */
    fun close()

    fun beforeClose() {}
    fun afterClose() {}

    /**
     * 等待作业执行完毕，一般只用于单测
     */
    fun join()

    fun beforeJoin() {}
    fun afterJoin() {}

    /**
     * 中止执行中的作业
     */
    fun kill()

    fun beforeKill() {}
    fun afterKill() {}

    fun startAndJoin() {
        start()
        join()
    }
}


