package vn.com.rd.testhardwareapp.mqtt

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MessageSenderService : Service() {
    // Define the blocking queue and any other necessary variables
    private var messageQueue: SendMessageBlockingQueue? = null
    private var isRunning = false
    private var requestTopic: String? = null
    private var requestScreenTopic: String? = null
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Get a reference to the message queue
        try {
            messageQueue = SendMessageBlockingQueue.instance
            requestTopic = intent.extras!!.getString("REQUEST_TOPIC")
            requestScreenTopic = intent.extras!!.getString("REQUEST_SCREEN_TOPIC")
            // Set isRunning to true so the service knows it should keep running
            isRunning = true

            // Start a new thread that sends messages from the queue
            val thread = Thread {
                while (isRunning) {
                    try {
                        // Get the next message from the queue and send it
                        val message: String? = messageQueue?.nextMessage
                        if (message != null) {
                            sendMessage(message)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Return START_STICKY so the service keeps running even if the app is closed
        return START_STICKY
    }

    // Define a method to send a message
    private fun sendMessage(message: String) {
        // Send the message using whatever method you choose (e.g. HTTP, TCP, etc.)
        if (HCCoreMqttClient.instance?.isConnected == true) {
            HCCoreMqttClient.instance!!.publish(requestTopic, message)
        }
    }

    override fun onDestroy() {
        // Set isRunning to false so the service stops running
        isRunning = false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}

