package vn.com.rd.testhardwareapp.mqtt

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class SendMessageAsync(private val onResponseListener: OnResponseListener, private val addToQueue: Boolean) :
    AsyncTask<BaseMqttMessage?, Void?, String?>() {

    companion object {
        const val CLIENT_SEND = "CLIENTSEND"
        private const val TIME_OUT: Long = 3
        private val GSON = Gson()
    }

    override fun doInBackground(vararg baseMqttMessages: BaseMqttMessage?): String? {
        val baseMqttMessage = baseMqttMessages.firstOrNull()
        if (!addToQueue) {
            Log.d("info", "doInBackground: not add to queue")
            SendMessageBlockingQueue.instance?.addMessage(GSON.toJson(baseMqttMessage))
            return null
        }
        val rqi = CLIENT_SEND + System.currentTimeMillis()
        baseMqttMessage?.rqi = rqi
        SendMessageBlockingQueue.instance?.addMessage(GSON.toJson(baseMqttMessage))
        val response : String? = ReceiveMessageQueue.getInstance().takeWithTimeout(rqi, TIME_OUT, TimeUnit.SECONDS)
        ReceiveMessageQueue.getInstance().remove(rqi)
        return response
    }

    override fun onPostExecute(s: String?) {
        super.onPostExecute(s)
        onResponseListener.onResponse(s)
    }
}

