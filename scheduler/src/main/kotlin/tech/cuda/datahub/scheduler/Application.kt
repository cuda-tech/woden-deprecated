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
package tech.cuda.datahub.scheduler

import tech.cuda.datahub.config.Datahub
import tech.cuda.datahub.scheduler.tracker.ClusterTracker
import tech.cuda.datahub.scheduler.tracker.InstanceTracker
import tech.cuda.datahub.scheduler.tracker.JobTracker
import tech.cuda.datahub.scheduler.tracker.MachineTracker
import tech.cuda.datahub.service.Database

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
fun main() {
    Database.connect(Datahub.database)
    val machineTracker = MachineTracker(afterStarted = {
        ClusterTracker(it).start()
        JobTracker().start()
        InstanceTracker(it).start()
    })
    machineTracker.start()
    machineTracker.await()
}
