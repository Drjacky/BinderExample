package app.web.drjacky.binderexample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class Communicator(private val thread: ExecutorCoroutineDispatcher) {
    private val lowLevelBinder = LowLevelBinder()
    private val responseChannel = Channel<String>(Channel.BUFFERED)

    fun start(message: String) {
        println("Start called")
        CoroutineScope(thread).launch {
            var c = 1
            do {
                val response = lowLevelBinder.read(message, c)
                sendResponse(response)
                c += 1
            } while (c <= 3)
        }

    }

    private suspend fun sendResponse(response: String) {
        responseChannel.send(response)
    }

    fun processResponses(): Flow<String> {
        return responseChannel.receiveAsFlow().onStart { delay(300) }
    }

    fun shutdown() {
        thread.close()
    }

}