package app.web.drjacky.binderexample

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.delayFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class Communicator(private val thread: ExecutorCoroutineDispatcher) {

    private val lowLevelBinder = LowLevelBinder()

    //    private val responseQueue = ArrayBlockingQueue<String>(Int.MAX_VALUE / 100)
    private val responseFlow = MutableStateFlow<String>("")

//    private val backgroundThread = MyThread()
//    private val myHandler = Handler(Looper.myLooper()!!)
    /*private val backgroundThread = thread(start = false) {
        var c = 1
        do {
            val response = lowLevelBinder.read(c)
            responseQueue.put(response) // Add the response to the queue
            c += 1
        } while (c <= 5)
    }*/

    /*    init {
            if (backgroundThread.isAlive.not())
                backgroundThread.start()
        }*/

    fun start(message: String) {
        println("Start called")
//        myHandler.post {
//        backgroundThread.backgroundHandler.post {
//        thread.executor.execute {
        /*CoroutineScope(thread).launch {
            var c = 1
            do {
                val response = lowLevelBinder.read(c)
                responseQueue.put(response) // Add the response to the queue
                c += 1
            } while (c <= 5)
        }*/

        CoroutineScope(thread).launch {
            var c = 1
            do {
                val response = lowLevelBinder.read(message, c)
                emitResponse(response) // Add the response to the queue
                c += 1
            } while (c <= 5)
        }

    }

    private suspend fun emitResponse(response: String) {
        // Emit the response to the flow
//        responseFlow.value = response
        responseFlow.emit(response)
    }

    /*    fun start() {
            if (backgroundThread.isAlive.not())
                backgroundThread.start()
        }*/

    private val responseLock = Object() // Lock for synchronization
    private var processingResponse = false // Indicates if a response is currently being processed

    /*    fun processResponses(callback: (String) -> Unit) {
            // CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val response = responseQueue.take() // Get the response from the queue (blocks if empty)
                val result = processResponse(response)
                callback(result)
            }
            // }
        }*/

    fun processResponses(): Flow<String> {
        responseFlow.onStart { delay(1000) }
        return responseFlow.asStateFlow()
    }

    private fun processResponse(response: String): String {
        synchronized(responseLock) {
            while (processingResponse) {
                responseLock.wait() // Wait until the previous response is processed
            }

            processingResponse = true
            Thread.sleep(1000)
            println("1000 waited")
            //println("A:response: $response")
            processingResponse = false
            responseLock.notifyAll() // Notify waiting threads that processing is done
            return response
        }
    }

    fun shutdown() {
        thread.close()
    }

}

class MyThread : Thread() {
    lateinit var backgroundHandler: Handler

    override fun run() {
        Looper.prepare()
        backgroundHandler = Handler(Looper.myLooper()!!)
        Looper.loop()
    }
}