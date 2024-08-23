package vn.com.rd.testhardwareapp.mqtt

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ReceiveMessageQueue private constructor() {
    private val queueMap: ConcurrentHashMap<String, LinkedBlockingQueue<String>> = ConcurrentHashMap()

    fun put(key: String, value: String) {
        Log.i("QUEUE", "put msg in queue")
        queueMap.computeIfAbsent(key) { LinkedBlockingQueue() }.put(value)
    }

    fun take(key: String, timeout: Long, unit: TimeUnit): String? {
        val queue = queueMap[key] ?: return null
        return try {
            queue.poll(timeout, unit)
        } catch (e: InterruptedException) {
            null
        }
    }

    fun takeWithTimeout(key: String, timeout: Long, unit: TimeUnit): String? {
        val startTime = System.currentTimeMillis()
        var element: String? = null
        val lock = Object()

        val thread = Thread {
            synchronized(lock) {
                while (element == null && System.currentTimeMillis() - startTime < unit.toMillis(timeout)) {
                    element = queueMap[key]?.poll()
                    if (element == null) {
                        try {
                            lock.wait(100) // Kiểm tra mỗi 100 milliseconds
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            return@Thread
                        }
                    }
                }
                lock.notify() // Thức dậy luồng chính sau khi kết thúc hoặc hết thời gian timeout
            }
        }
        thread.start()

        synchronized(lock) {
            if (element == null) {
                try {
                    lock.wait(unit.toMillis(timeout)) // Chờ đợi cho đến khi có kết quả hoặc hết thời gian timeout
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return null
                }
            }
        }

        return element
    }

    fun remove(key: String): Boolean {
        return queueMap.remove(key) != null
    }

    fun logQueue(){
        for (element in queueMap) {
            println(element)
        }
    }
    companion object {
        @Volatile
        private var instance: ReceiveMessageQueue? = null

        @Synchronized
        fun getInstance(): ReceiveMessageQueue {
            return instance ?: synchronized(this) {
                instance ?: ReceiveMessageQueue().also { instance = it }
            }
        }
    }
}