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
package tech.cuda.woden.scheduler

import tech.cuda.woden.common.configuration.Woden
import tech.cuda.woden.common.service.Database
import tech.cuda.woden.scheduler.tracker.ClusterTracker
import tech.cuda.woden.scheduler.tracker.InstanceTracker
import tech.cuda.woden.scheduler.tracker.JobTracker
import tech.cuda.woden.scheduler.tracker.ContainerTracker

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
fun main() {
    Database.connect(Woden.datasource)
    val containerTracker = ContainerTracker(afterStarted = {
        ClusterTracker(it.container).start()
        JobTracker().start()
        InstanceTracker(it.container).start()
    })
    containerTracker.start()
    containerTracker.join()
}
