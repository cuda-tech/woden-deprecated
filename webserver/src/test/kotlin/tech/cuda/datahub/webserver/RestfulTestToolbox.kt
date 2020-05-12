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
package tech.cuda.datahub.webserver

import ch.vorburger.mariadb4j.DB
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.spring.SpringListener
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import tech.cuda.datahub.service.config.DatabaseConfig
import tech.cuda.datahub.service.utils.Schema

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Suppress("UNCHECKED_CAST")
open class RestfulTestToolbox(private vararg val tables: String = arrayOf()) : AnnotationSpec() {
    @Autowired
    lateinit var template: TestRestTemplate
    lateinit var postman: Postman
    private lateinit var schema: Schema

    override fun listeners() = listOf(SpringListener)

    @BeforeAll
    fun beforeAll() {
        val db = DB.newEmbeddedDB(0).also { it.start() }
        schema = Schema(DatabaseConfig(port = db.configuration.port))
//        schema = Schema(DatabaseConfig(port = 3306))
    }

    @BeforeEach
    fun beforeEach() {
        schema.rebuildDB()
        tables.forEach {
            schema.mockTable(it)
        }
        if ("users" !in tables) {
            schema.mockTable("users")
        }
        this.postman = Postman(template)
        this.postman.login()
    }

    val ResponseEntity<Map<String, Any>>.shouldSuccess: ResponseEntity<Map<String, Any>>
        get() {
            Assertions.assertEquals(HttpStatus.OK, statusCode)
            Assertions.assertEquals("success", body?.get("status"))
            return this
        }

    val ResponseEntity<Map<String, Any>>.shouldFailed: ResponseEntity<Map<String, Any>>
        get() {
            Assertions.assertEquals(HttpStatus.OK, statusCode)
            Assertions.assertEquals("failed", body?.get("status"))
            return this
        }

    fun ResponseEntity<Map<String, Any>>.withMessage(message: String) = Assertions.assertEquals(message, body?.get("message"))


    fun ResponseEntity<Map<String, Any>>.withError(error: String) = Assertions.assertEquals(error, body?.get("error"))

    fun ResponseEntity<Map<String, Any>>.withIllegalArgumentError(error: String) = Assertions.assertEquals("illegal argument: $error", body?.get("error"))

    fun ResponseEntity<Map<String, Any>>.withNotFoundError(error: String) = Assertions.assertEquals("$error not found", body?.get("error"))

    val ResponseEntity<Map<String, Any>>.thenGetData: Map<String, Any>
        get() = body?.get("data") as Map<String, Any>

    fun Map<String, Any>.andCheckCount(count: Int): Map<String, Any> {
        Assertions.assertEquals(count, get("count"))
        return this
    }

    fun Map<String, Any>.thenGetListOf(field: String): List<LinkedHashMap<String, Any>> {
        return get(field) as List<LinkedHashMap<String, Any>>
    }

    fun List<LinkedHashMap<String, Any>>.andCheckSize(size: Int): List<LinkedHashMap<String, Any>> {
        Assertions.assertEquals(size, this.size)
        return this
    }

    fun Map<String, Any>.thenGetItem(field: String): LinkedHashMap<String, Any> {
        return get(field) as LinkedHashMap<String, Any>
    }

    fun LinkedHashMap<String, Any>.withExpect(block: (LinkedHashMap<String, Any>) -> Unit): LinkedHashMap<String, Any> {
        block(this)
        return this
    }

    infix fun Any?.shouldBe(expect: Any?) = Assertions.assertEquals(expect, this)

    infix fun Any?.shouldNotBe(expect: Any?) = Assertions.assertNotEquals(expect, this)

    infix fun Any?.shouldSameElemWith(another: Any?): Boolean {
        this as Collection<Any>
        another as Collection<Any>
        return this.all { another.contains(it) } && another.all { this.contains(it) }
    }

    infix fun LinkedHashMap<String, Any>.shouldNotContain(key: String) = Assertions.assertFalse(this.keys.contains(key))

}