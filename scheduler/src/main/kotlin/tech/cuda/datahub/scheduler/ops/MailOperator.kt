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

import tech.cuda.datahub.config.DatahubConfig
import tech.cuda.datahub.service.exception.OperationNotAllowException
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class MailOperator(
    private val to: List<String>,
    private val title: String,
    private val content: String
) : Operator() {
    override val isFinish: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val isSuccess: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val output: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.



    override fun start() {
        val message = MimeMessage(Session.getInstance(DatahubConfig.email.properties, DatahubConfig.email.auth))
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to.joinToString(",")))
        message.setFrom()
        message.subject = title
        message.setText(content)
        Transport.send(message)
    }

    override fun kill() = throw OperationNotAllowException("Sending Email could not be stip")
}
