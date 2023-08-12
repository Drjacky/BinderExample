package app.web.drjacky.binderexample

import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class Communicator {

    private val lowLevelBinder = LowLevelBinder()
    private val responseQueue = ArrayBlockingQueue<String>(Int.MAX_VALUE)

    // private val backgroundThread = Thread()
    // private val myHandler = Handler(Looper.myLooper()!!)
    private val backgroundThread = thread(start = false) {
        var c = 1
        do {
            val response = lowLevelBinder.read(c)
            responseQueue.put(response) // Add the response to the queue
            c += 1
        } while (c <= 10)
    }

    /*    init {
            backgroundThread.start()
        }*/

    /*fun start() {
        myHandler.post {
            val response = b.read()
            responseQueue.put(response) // Add the response to the queue
        }
    }*/
    fun start() {
        if (backgroundThread.isAlive.not())
            backgroundThread.start()
    }

    private val responseLock = Object() // Lock for synchronization
    private var processingResponse = false // Indicates if a response is currently being processed

    fun processResponses(callback: (String) -> Unit): String {
        // CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            val response = responseQueue.take() // Get the response from the queue (blocks if empty)
            val result = processResponse(response)
            callback(result)
        }
        // }
    }

    private fun processResponse(response: String): String {
        synchronized(responseLock) {
            while (processingResponse) {
                responseLock.wait() // Wait until the previous response is processed
            }

            processingResponse = true
            Thread.sleep((50L..500L).random())
            //println("A:response: $response")
            processingResponse = false
            responseLock.notifyAll() // Notify waiting threads that processing is done
            return response
        }
    }

}