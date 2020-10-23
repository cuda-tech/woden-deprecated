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
package tech.cuda.woden.webserver

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.spring.SpringListener
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import tech.cuda.woden.config.Woden
import tech.cuda.woden.service.Database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Suppress("UNCHECKED_CAST")
open class RestfulTestToolbox(private vararg val tables: String = arrayOf()) : AnnotationSpec() {
    @Autowired
    lateinit var template: TestRestTemplate
    lateinit var postman: Postman
    val mapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(JavaTimeModule().addDeserializer(LocalDateTime::class.java,
            LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        )

    override fun listeners() = listOf(SpringListener)

    @BeforeAll
    fun beforeAll() {
        val db = DB.newEmbeddedDB(DBConfigurationBuilder.newBuilder().also {
            it.port = 0
            it.baseDir = System.getProperty("java.io.tmpdir") + this.javaClass.simpleName
        }.build()).also { it.start() }
        mockkObject(Woden.database)
        every { Woden.database.properties } returns Properties().also { props ->
            props["url"] = "jdbc:mysql://localhost:${db.configuration.port}/?characterEncoding=UTF-8"
            props["username"] = "root"
        }
        Database.connect(Woden.database)
    }

    @AfterAll
    fun afterAll() {
        unmockkObject(Woden.database)
    }

    @BeforeEach
    fun beforeEach() {
        Database.rebuild()
        tables.forEach {
            Database.mock(it)
        }
        if ("users" !in tables) {
            Database.mock("users")
        }
        this.postman = Postman(template)
        this.postman.login()
    }

    val ResponseEntity<Map<String, Any>>.shouldSuccess: ResponseEntity<Map<String, Any>>
        get() {
            statusCode shouldBe HttpStatus.OK
            body?.get("status") shouldBe "success"
            return this
        }

    val ResponseEntity<Map<String, Any>>.shouldFailed: ResponseEntity<Map<String, Any>>
        get() {
            statusCode shouldBe HttpStatus.OK
            body?.get("status") shouldBe "failed"
            return this
        }


    inline fun <reified T> ResponseEntity<Map<String, Any>>.get(field: String): T {
        val data = body?.get("data") as Map<String, Any>
        return mapper.convertValue(data[field], T::class.java)
    }

    inline fun <reified T> ResponseEntity<Map<String, Any>>.getList(field: String): List<T> {
        val data = body?.get("data") as Map<String, Any>
        val list = data[field] as List<Map<String, Any>>
        return list.map {
            mapper.convertValue(it, T::class.java)
        }
    }

    fun ResponseEntity<Map<String, Any>>.withMessage(message: String) = Assertions.assertEquals(message, body?.get("message"))

    fun ResponseEntity<Map<String, Any>>.withError(error: String) = Assertions.assertEquals(error, body?.get("error"))

    fun <T> T.withExpect(block: (T) -> Unit): T {
        block(this)
        return this
    }
}