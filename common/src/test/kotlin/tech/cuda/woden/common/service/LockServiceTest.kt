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
package tech.cuda.woden.common.service

import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import me.liuwj.ktorm.global.deleteAll
import tech.cuda.woden.common.service.dao.GlobalLockDAO
import tech.cuda.woden.common.service.dto.LockDTO

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 0.1.0
 */
class LockServiceTest : TestWithMaria({

    "顺序获取锁" {
        val threadCount = 100
        val expireSecond = 100

        // 每个线程获取后释放
        GlobalLockDAO.deleteAll()
        var seqLocks = (1..threadCount).map {
            val lock = LockService.lock("seq lock", message = "thread-$it", expire = expireSecond)
            LockService.unlock(lock) shouldBe true
            lock
        }
        seqLocks.size shouldBe threadCount
        seqLocks.zipWithNext { current, next ->
            current shouldNotBe null
            next shouldNotBe null
            current!!
            next!!
            current.expireTime - current.lockTime shouldBe expireSecond
            current.lockTime shouldBeLessThanOrEqual next.lockTime
        }


        // 第一个线程获取后不释放
        GlobalLockDAO.deleteAll()
        seqLocks = (1..threadCount).map {
            LockService.lock("seq lock", message = "thread-$it", expire = expireSecond)
        }
        seqLocks.size shouldBe threadCount
        seqLocks[0] shouldNotBe null
        for (i in 1 until threadCount) {
            seqLocks[i] shouldBe null
        }

        // 第 50 个线程获取后不释放，等到第 60 个线程获取后释放
        GlobalLockDAO.deleteAll()
        var lock50: LockDTO? = null
        seqLocks = (0 until threadCount).map {
            val lock = LockService.lock("seq lock", message = "thread-$it", expire = expireSecond)
            when {
                it < 50 -> LockService.unlock(lock) shouldBe true
                it == 50 -> lock50 = lock
                it < 60 -> LockService.unlock(lock) shouldBe false
                it == 60 -> LockService.unlock(lock50) shouldBe true
                else -> LockService.unlock(lock) shouldBe true
            }
            lock
        }
        seqLocks.filterIndexed { index, _ -> index <= 50 || index > 60 }
            .zipWithNext { current, next ->
                current shouldNotBe null
                next shouldNotBe null
                current!!
                next!!
                current.expireTime - current.lockTime shouldBe expireSecond
                current.lockTime shouldBeLessThanOrEqual next.lockTime
            }

        seqLocks.filterIndexed { index, _ -> index in 51..60 }
            .forEach { it shouldBe null }
    }

    "并发获取锁" {
        GlobalLockDAO.deleteAll()
        val locks = (1..100).map {
            GlobalScope.async {
                LockService.lock("concurrency lock", message = "thread-$it", expire = 10)
            }
        }.map { it.await() }
        locks.size shouldBe 100
        locks.filterNotNull().count() shouldBe 1
    }

    "锁的排他性测试" {
        GlobalLockDAO.deleteAll()

        // 一个需要耗时 2 秒的协程获得了锁
        GlobalScope.async {
            val lock = LockService.lock("concurrency lock", message = "thread", expire = 10)
            lock shouldNotBe null
            Thread.sleep(2000)
            LockService.unlock(lock) shouldBe true
        }

        // 两秒内的协程无法获取锁
        Thread.sleep(1000)
        val threadIn2Sec = (1..10).map {
            GlobalScope.async {
                LockService.lock("concurrency lock", message = "thread-$it", expire = 10)
            }
        }

        // 两秒后的协程可以获取锁
        Thread.sleep(3000)
        val threadAfter2Sec = (1..10).map {
            GlobalScope.async {
                LockService.lock("concurrency lock", message = "thread-$it", expire = 10)
            }
        }

        val locksIn2Sec = threadIn2Sec.map { it.await() }
        locksIn2Sec.size shouldBe 10
        locksIn2Sec.filterNotNull().count() shouldBe 0

        val locksAfter2Sec = threadAfter2Sec.map { it.await() }
        locksAfter2Sec.size shouldBe 10
        locksAfter2Sec.filterNotNull().count() shouldBe 1
    }

    "锁超时抢夺" {
        GlobalLockDAO.deleteAll()

        // 需要执行 5 秒的协程获取了一把 2 秒后超时的锁
        val thread1 = GlobalScope.async {
            val lock = LockService.lock("concurrency lock", message = "thread1", expire = 2)
            lock shouldNotBe null
            Thread.sleep(5000)
            LockService.unlock(lock) shouldBe false // 因为被 3 秒后的协程抢夺了锁，此时释放锁失败
        }

        // 2 秒内的协程无法抢夺锁
        Thread.sleep(1000)
        val thread2 = GlobalScope.async {
            val lock = LockService.lock("concurrency lock", message = "thread2", expire = 2)
            lock shouldBe null
        }

        // 3 秒后来了一个新协程，抢夺了锁
        Thread.sleep(3000)
        val thread3 = GlobalScope.async {
            val lock = LockService.lock("concurrency lock", message = "thread3", expire = 2)
            lock shouldNotBe null
            LockService.unlock(lock) shouldBe true
        }

        thread1.await()
        thread2.await()
        thread3.await()
    }

})
