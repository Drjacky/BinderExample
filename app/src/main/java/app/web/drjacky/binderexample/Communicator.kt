package app.web.drjacky.binderexample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class Communicator(private val thread: ExecutorCoroutineDispatcher) {

    private val lowLevelBinder = LowLevelBinder()
    private val responseFlow = MutableStateFlow<String>("")

    fun start(message: String) {
        println("Start called")
        CoroutineScope(thread).launch {
            var c = 1
            do {
                val response = lowLevelBinder.read(message, c)
                emitResponse(response)
                c += 1
            } while (c <= 5)
        }

    }

    private suspend fun emitResponse(response: String) {
        responseFlow.emit(response)
    }

    fun processResponses(): Flow<String> {
        responseFlow.onStart { delay(1000) }
        return responseFlow.asStateFlow()
    }

    fun shutdown() {
        thread.close()
    }

}