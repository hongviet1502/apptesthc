package vn.com.rd.testhardwareapp.mqtt

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import vn.com.rd.testhardwareapp.MainActivity
import vn.com.rd.testhardwareapp.mqtt.beans.getValueSensor.GetValueSensorResponse
import vn.com.rd.testhardwareapp.mqtt.beans.getValueSensor.GetValueSensorResponseData
import vn.com.rd.testhardwareapp.mqtt.beans.miniScreen.GetMiniScreenRequest
import vn.com.rd.testhardwareapp.mqtt.beans.miniScreen.GetMiniScreenRequestData
import vn.com.rd.testhardwareapp.ui.activity.MiniScreenActivity
import java.net.NetworkInterface
import java.util.Base64
import java.util.Collections
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class HCCoreMqttClient private constructor() : MqttCallback {
    private val TAG = HCCoreMqttClient::class.java.name
    var brokerUrl = ""
    var clientId = ""
    private var mqttClient: MqttClient? = null
    private var context: Context? = null

    fun setContext(context: Context?) {
        this.context = context
    }

    fun connect() {
        try {
            if (brokerUrl == "") {
                brokerUrl = "tcp://default-broker-url.com:1883"
            }
            var password = ""
            password = base64ToHex(encrypt(RD_HC_KEY, RD_HC_PREFIX)).toUpperCase(Locale.ROOT)
            Log.d("info", "HCCoreMqttClient | connect, password: $password")
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            mqttClient!!.setCallback(this)
            val connOpts = MqttConnectOptions()
            connOpts.userName = "RD"
            connOpts.password = password.toCharArray()
            connOpts.isCleanSession = true
            mqttClient!!.connect(connOpts)
            Log.d("MQTT Client", "Connected to broker: $brokerUrl")
        } catch (me: MqttException) {
            logMqttException(me)
        }
    }

    fun disconnect() {
        try {
            mqttClient!!.disconnect()
            Log.d("MQTT Client", "Disconnected from broker: $brokerUrl")
        } catch (me: MqttException) {
            logMqttException(me)
        }
    }

    fun subscribe(topic: String) {
        try {
            if (!isConnected) {
                connect()
            }
            mqttClient!!.subscribe(topic)
            Log.d("MQTT Client", "Subscribed to topic: $topic")
        } catch (me: MqttException) {
            logMqttException(me)
        }
    }

    fun publish(topic: String?, message: String) {
        try {
            if (!isConnected) {
                connect()
            }
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient!!.publish(topic, mqttMessage)
            Log.d("MQTT Client", "Published message: $message to topic: $topic")
        } catch (me: MqttException) {
            logMqttException(me)
        }
    }

    val isConnected: Boolean
        get() = mqttClient != null && mqttClient!!.isConnected

    override fun connectionLost(cause: Throwable) {
        if (!isConnected) {
            connect()
        }
        Log.d("MQTT Client", "Connection to broker lost!$cause")
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        val content = String(message.payload)
        Log.d("MQTT Client", "Message received: $content on topic: $topic")
        if (!content.contains("cmd")) {
            return
        }
        val baseMqttMessage = GSON.fromJson(
            content,
            BaseMqttMessage::class.java
        )
        if (baseMqttMessage.cmd.equals("ChangeScreen")) {
            try {
                val changeScreen = GSON.fromJson(content, GetMiniScreenRequest::class.java)
                val getMiniScreenRequestData: GetMiniScreenRequestData? = changeScreen.data
                if (getMiniScreenRequestData != null) {
                    Log.i(TAG, "mini screen index: " + getMiniScreenRequestData.index)
                    MiniScreenActivity.getInstance()?.updateMiniScreen(getMiniScreenRequestData.index)

                }
            } catch (e:Exception){
                e.printStackTrace()
            }
        } else if (baseMqttMessage.cmd.equals("getValueSensorRsp")) {
            val getValueSensorResponse = GSON.fromJson(content, GetValueSensorResponse::class.java)
            val getValueSensorResponseData: GetValueSensorResponseData? = getValueSensorResponse.data
            if (getValueSensorResponseData != null) {
                MainActivity.average_temp = getValueSensorResponseData.temp
                MainActivity.average_humi = getValueSensorResponseData.humi
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

    private fun logMqttException(me: MqttException) {
        Log.e("MQTT Client", "Reason: " + me.reasonCode)
        Log.e("MQTT Client", "Message: " + me.message)
        Log.e("MQTT Client", "Local message: " + me.localizedMessage)
        Log.e("MQTT Client", "Cause: " + me.cause)
        Log.e("MQTT Client", "Exception: $me")
    }

    @Throws(java.lang.Exception::class)
    fun encrypt(key: String, prefix: String): String {
        Log.d("info", "HCCoreMqttClient | encrypt, macAddress: $macAddr")
        var plaintext = prefix + macAddr.replace(":", "")
        plaintext = plaintext.lowercase(Locale.getDefault())
        Log.d("info", "HCCoreMqttClient | encrypt, plaintext: $plaintext")
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray())
        var encryptedString: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encryptedString = Base64.getEncoder().encodeToString(encryptedBytes)
        }
        Log.d("info", "HCCoreMqttClient | encrypt, encryptedString: $encryptedString")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(encryptedBytes)
        } else ""
    }

    fun base64ToHex(base64: String?): String {
        var bytes = ByteArray(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bytes = Base64.getDecoder().decode(base64)
        }
        val hex = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            hex.append(String.format("%02x", b))
        }
        return hex.toString()
    }

    val macAddr: String
        get() {
            try {
                val all: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (nif in all) {
                    Log.d("info", "dcMac: " + nif.name)
                    if (!nif.name.equals("eth0", ignoreCase = true)) continue
                    val macBytes = nif.hardwareAddress
                    Log.d("info", "HardwareAddress: $macBytes")
                    if (macBytes == null) {
                        return ""
                    }
                    val res1 = StringBuilder()
                    for (b in macBytes) {
                        // res1.append(Integer.toHexString(b & 0xFF) + ":");
                        res1.append(String.format("%02X:", b))
                    }
                    if (res1.length > 0) {
                        res1.deleteCharAt(res1.length - 1)
                    }
                    return res1.toString()
                }
            } catch (ex: Exception) {
                //handle exception
            }
            return ""
        }

    companion object {
        private const val RD_HC_KEY = "RANGDONGRALSMART"
        private const val RD_HC_PREFIX = "2804"

        private val GSON = Gson()
        private var hcCoreMqttClient: HCCoreMqttClient? = null
        val instance: HCCoreMqttClient?
            get() {
                if (hcCoreMqttClient == null) hcCoreMqttClient = HCCoreMqttClient()
                return hcCoreMqttClient
            }
    }
}
