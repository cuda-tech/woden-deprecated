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

import com.icegreen.greenmail.util.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import tech.cuda.datahub.service.exception.OperationNotAllowException


/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@DoNotParallelize
class MailOperatorTest : AnnotationSpec() {

    private val greenMail = GreenMail(ServerSetup.SMTPS)

    @BeforeEach
    fun startMailServer() {
        greenMail.start()
        greenMail.setUser("admin@datahub", "admin@datahub", "password")
    }

    @AfterEach
    fun stopMailServer() {
        greenMail.stop()
    }

    @Test
    fun send() {
        val receivers = listOf("user1@test1", "user2@test2", "user3@test3")
        val op = MailOperator(to = receivers, title = "test email", content = "this is a test email")
        op.start()
        op.isFinish shouldBe false
        op.isSuccess shouldBe false
        op.join()
        op.isFinish shouldBe true
        op.isSuccess shouldBe true

        val mails = greenMail.receivedMessages
        mails.size shouldBe 3
        mails.forEach {
            it.subject shouldBe "test email"
            GreenMailUtil.getBody(it) shouldBe "this is a test email"
            it.allRecipients.map { addr -> addr.toString() } shouldContainInOrder receivers
            it.from.size shouldBe 1
            it.from.first().toString() shouldBe "admin@datahub"
        }
    }

    @Test
    fun kill() {
        val receivers = listOf("user1@test1", "user2@test2", "user3@test3")
        val op = MailOperator(to = receivers, title = "test email", content = "this is a test email")
        op.start()
        shouldThrow<OperationNotAllowException> {
            op.kill()
        }
    }

}