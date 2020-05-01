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
package tech.cuda.datahub.service.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class EncoderTest : StringSpec({
    "md5" {
        Encoder.md5("root") shouldBe "63a9f0ea7bb98050796b649e85481845"
        Encoder.md5("guest") shouldBe "084e0343a0486ff05530df6c705c8bb4"
    }

})