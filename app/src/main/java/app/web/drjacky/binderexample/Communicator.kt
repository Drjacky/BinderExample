package app.web.drjacky.binderexample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class Communicator(private val thread: ExecutorCoroutineDispatcher) {
    private val lowLevelBinder = LowLevelBinder()
    private val responseChannel = Channel<String>(capacity = Channel.BUFFERED)
    private val responseFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.SUSPEND
    )


    fun start(message: String) {
        println("Start called")
        CoroutineScope(thread).launch {
            var c = 1
            do {
                val response = lowLevelBinder.read(message, c)
                sendResponse("channelResponse: $response")
                emitResponse("flowResponse: $response")
                c += 1
            } while (c <= 3)
        }

    }

    private suspend fun sendResponse(response: String) {
        responseChannel.send(response)
    }

    private suspend fun emitResponse(response: String) {
        responseFlow.emit(response)
    }

    fun processResponsesChannel(): Flow<String> {
        return responseChannel.receiveAsFlow().onStart { delay(300) }
    }

    fun processResponsesFlow(): Flow<String> {
        return responseFlow.onStart { delay(300) }
    }

    fun shutdown() {
        thread.close()
    }

}