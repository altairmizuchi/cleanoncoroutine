package org.learn.domain.interactor

import junit.framework.Assert.fail
import kotlinx.coroutines.*
import org.junit.Test
import java.lang.Thread.sleep
import kotlin.coroutines.EmptyCoroutineContext

class CoroutineTest {

    suspend fun thinker(ind: Int) {
        coroutineScope {
            var counter = 0
            while (isActive) {
                println("Thinker #$ind thinks!")
                delay(2000)
                println("Thinker #$ind next is $counter")
                counter++
            }
        }
    }

    @Test
    fun `scope cancels all job cancels`() {
        val executeOnScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        for (i in 1..10) executeOnScope.launch {
            thinker(i)
        }
        sleep(10000)
        executeOnScope.cancel()
        println("canceled")
        sleep(5000)
    }

    @Test
    fun `cancel of children context`() = runBlocking {
        val executeOnScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val reportOnContext = EmptyCoroutineContext + Dispatchers.Default

        val job = executeOnScope.launch {
            withContext(reportOnContext) {
                thinker(0)
            }
        }
        sleep(6000)
        job.cancel()
        println("canceled")
        sleep(4000)
    }

    suspend fun failure(ind: Int) {
        coroutineScope {
            while (isActive) {
                repeat(3) {
                    println("Wait for fail on #$ind $it")
                    delay(1000)
                }
                fail("Time to fail on #$ind!")
            }
        }
    }

    @Test
    fun `non supervizoer job fail`() = runBlocking {
        val executeOnScope = CoroutineScope(/*Supervisor*/Job() + Dispatchers.IO)
        var lastJob: Job = Job()
        for (i in 1..10) {
            lastJob = executeOnScope.launch {
                failure(i)
            }
            delay(2000)
        }
        lastJob.join()
        sleep(1000)
        println("end!")
        //executeOnScope.cancel()
        //println("canceled")
        //sleep(5000)
    }
}