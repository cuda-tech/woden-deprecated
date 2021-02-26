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
package tech.cuda.woden.scheduler.runner

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import tech.cuda.woden.common.configuration.Woden
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class MailAdhoc(private val to: List<String>, private val title: String, private val content: String) : Adhoc {

    private var hasException = false
    private lateinit var job: Deferred<Unit>

    override val output: String
        get() = ""

    override val status: AdhocStatus
        get() = when {
            !this::job.isInitialized -> AdhocStatus.NOT_START
            !this.job.isCompleted -> AdhocStatus.RUNNING
            job.isCompleted -> if (hasException) AdhocStatus.FAILED else AdhocStatus.SUCCESS
            else -> AdhocStatus.FAILED
        }

    override fun start() {
        this.job = GlobalScope.async {
            try {
                val message = MimeMessage(Session.getInstance(Woden.email.properties, Woden.email.auth))
                message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to.joinToString(",")))
                message.setFrom()
                message.subject = title
                message.setText(content)
                Transport.send(message)
            } catch (e: Throwable) {
                hasException = true
                e.printStackTrace()
            }
        }
    }

    override fun join() {
        runBlocking {
            if (this@MailAdhoc::job.isInitialized) {
                job.await()
            }
        }
    }

    override fun close() {}

    override fun kill() {}
}