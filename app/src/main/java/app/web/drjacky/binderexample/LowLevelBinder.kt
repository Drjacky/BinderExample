package app.web.drjacky.binderexample

import android.os.Binder
import android.os.IBinder
import android.os.Parcel

class LowLevelBinder {

    fun read(message: String, counter: Int): String {
        val binder = Binder()
        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        // Simulate writing data to the Parcel (e.g., method identifier)
        data.writeInt(REQUEST_READ)

        // Simulate transact call
        binder.transact(TRANSACTION_READ, data, reply, 0)

        // Simulate a delay in the response
        val delay = randomDelay()
        Thread.sleep(delay)

        // Simulate reading response from the Parcel
        val response = reply.readString()
        println("Binder read is done")

        // Clean up
        data.recycle()
        reply.recycle()

        return "$message-binder-$counter | delayed: ${delay}ms"
    }

    private fun randomDelay(): Long {
        return (1000..6000).random().toLong()
    }

    companion object {
        private const val REQUEST_READ = 1
        private const val TRANSACTION_READ = IBinder.FIRST_CALL_TRANSACTION + 1
    }
}