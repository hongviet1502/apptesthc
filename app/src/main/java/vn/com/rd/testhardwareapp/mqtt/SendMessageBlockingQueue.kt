package vn.com.rd.testhardwareapp.mqtt

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class SendMessageBlockingQueue private constructor() {
    private val queue: BlockingQueue<String>

    init {
        queue = LinkedBlockingQueue()
    }

    fun addMessage(message: String) {
        try {
            if (!HCCoreMqttClient.instance?.isConnected!!) {
                HCCoreMqttClient.instance?.connect()
            }
            queue.put(message)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            e.printStackTrace()
        }
    }

    val nextMessage: String?
        get() = try {
            queue.take()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            e.printStackTrace()
            null
        }
    val isEmpty: Boolean
        get() = queue.isEmpty()

    companion object {
        @get:Synchronized
        var instance: SendMessageBlockingQueue? = null
            get() {
                if (field == null) {
                    field = SendMessageBlockingQueue()
                }
                return field
            }
            private set
    }
}
