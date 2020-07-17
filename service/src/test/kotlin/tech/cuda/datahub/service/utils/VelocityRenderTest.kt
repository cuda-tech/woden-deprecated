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
import java.time.LocalDate

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class VelocityRenderTest : StringSpec({

    fun String.replaceDollar() = this.trimIndent().replace("%", "$")

    "注释" {
        VelocityRender.render("""
            ## this is a comment
            hello world
        """.replaceDollar()) shouldBe "hello world"

        VelocityRender.render("""
            #*
              this is a multi line comment
            *#
            hello world
        """.replaceDollar()) shouldBe "\nhello world"
    }

    "变量定义" {
        VelocityRender.render("""
            #set(%name = "Velocity" )
            hello %name
        """.replaceDollar()) shouldBe "hello Velocity"

        VelocityRender.render("""
            #set(%number = 123 )
            number is %number
        """.replaceDollar()) shouldBe "number is 123"

        VelocityRender.render("""
            #set(%list = [1, 2, 3] )
            first is %list[0]
            second is %list[1]
            third is %list[2]
        """.replaceDollar()) shouldBe """
            first is 1
            second is 2
            third is 3
        """.trimIndent()

        VelocityRender.render("""
            #set(%map = {'x': 1, 'y': 2, 'z': 3} )
            x is %map.x
            y is %map.y
            z is %map.z
        """.replaceDollar()) shouldBe """
            x is 1
            y is 2
            z is 3
        """.trimIndent()
    }

    "内置变量" {
        VelocityRender.render("""
            today is %bizdate
        """.replaceDollar(), LocalDate.of(2020, 1, 1)) shouldBe "today is 2020-01-01"
    }

    "函数调用" {
        VelocityRender.render("""
            #set(%list = [1, 2, 3] )
            size = %list.size()
        """.replaceDollar()) shouldBe "size = 3"

        VelocityRender.render("""
            #set(%map = {'x': 1, 'y': 2, 'z': 3} )
            contain x: %map.containsKey("x")
            contain t: %map.containsKey("t")
        """.replaceDollar()) shouldBe """
            contain x: true
            contain t: false
        """.trimIndent()
    }

    "跳转语句" {
        VelocityRender.render("""
            #set(%foo = true)
            #if (%foo) foo can be seen #end
        """.replaceDollar()) shouldBe " foo can be seen "

        VelocityRender.render("""
            #set(%foo = false)
            #if (%foo) foo can be seen #else foo can not be seen #end
        """.replaceDollar()) shouldBe " foo can not be seen "

        VelocityRender.render("""
            #set(%foo = false)
            #set(%bar = true)
            #if (%foo || %bar ) foo can be seen #else foo can not be seen #end
        """.replaceDollar()) shouldBe " foo can be seen "

        VelocityRender.render("""
            #set(%foo = false)
            #set(%bar = true)
            #if (%foo && %bar ) foo can be seen #else foo can not be seen #end
        """.replaceDollar()) shouldBe " foo can not be seen "
    }

    "循环语句" {
        VelocityRender.render("""
            #foreach( %foo in [1..3] )
            value is %foo
            #end
        """.replaceDollar()).trimIndent() shouldBe """
            value is 1
            value is 2
            value is 3
        """.trimIndent()

        VelocityRender.render("""
            #set(%map = {'x': 1, 'y': 2, 'z': 3} )
            #foreach(%foo in %map.entrySet())
            %foo.key is %foo.value
            #end
        """.replaceDollar()).trimIndent() shouldBe """
            x is 1
            y is 2
            z is 3
        """.trimIndent()
    }

    "宏定义" {
        VelocityRender.render("""
            #macro(print)
            hello world
            #end
            #print()
        """.replaceDollar()) shouldBe "hello world\n"

        VelocityRender.render("""
            #macro(print %s)
            %s
            #end
            #set( %str = 'hello world')
            #print(%str)
        """.replaceDollar()) shouldBe "hello world\n"
    }

    "转义" {
        VelocityRender.render("$1.23") shouldBe "$1.23"
        VelocityRender.render("$.abc") shouldBe "$.abc"
        VelocityRender.render("$[0]") shouldBe "$[0]"
        VelocityRender.render("""%abc""".replaceDollar()) shouldBe """%abc""".replaceDollar()
        VelocityRender.render("""
            #set(%abc = "hello world")
            %abc
            \%abc
        """.replaceDollar()) shouldBe """
            hello world
            %abc
        """.replaceDollar()
    }
})



