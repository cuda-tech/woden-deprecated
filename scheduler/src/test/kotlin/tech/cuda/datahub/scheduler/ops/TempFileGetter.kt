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
package tech.cuda.datahub.scheduler.ops

import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
interface TempFileGetter {

    fun getTempFile(prefix: String = "", suffix: String = ".temp"): File {
        val files = File(System.getProperty("java.io.tmpdir")).listFiles()?.filter {
            it.name.startsWith(prefix)
                && it.name.endsWith(suffix)
                && Duration.between(LocalDateTime.now(), LocalDateTime.ofInstant(
                Files.readAttributes(it.toPath(), BasicFileAttributes::class.java).creationTime().toInstant(),
                ZoneId.systemDefault()
            )).seconds < 5
        } ?: listOf()
        files.size shouldBe 1
        return files.first()!!
    }
}