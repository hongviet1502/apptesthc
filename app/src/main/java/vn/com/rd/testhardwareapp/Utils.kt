package vn.com.rd.testhardwareapp

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.BuildConfig
import org.koin.core.component.KoinComponent
import vn.com.rd.testhardwareapp.mqtt.OnResponseListener
import vn.com.rd.testhardwareapp.mqtt.SendMessageAsync
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestBigScreen.ReportTestBigSceenData
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestBigScreen.ReportTestBigScreen
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestHC.ReportTestHCRequest
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestHC.ReportTestHCRequestData
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestMic.ReportTestMic
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestMic.ReportTestMicData
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestPresenceSensor.ReportTestPresenceSensor
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestPresenceSensor.ReportTestPresenceSensorData
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestSmallScreen.ReportTestSmallScreen
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestSmallScreen.ReportTestSmallScreenData
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestSpeaker.ReportTestSpeaker
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestSpeaker.ReportTestSpeakerData
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestTemhumSensor.ReportTestTemhumSensor
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestTemhumSensor.ReportTestTemhumSensorData
import vn.com.rd.testhardwareapp.mqtt.beans.touchBigScreen.ReportTestBigScreenTouch
import vn.com.rd.testhardwareapp.mqtt.beans.touchBigScreen.ReportTestBigScreenTouchData
import java.net.NetworkInterface
import java.util.Collections

object Utils : KoinComponent {

    fun setFullScreen(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    fun errorNoFeatureDialog(context: Context?, isFinish: Boolean = true) {
        context?.let {
            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.ui_error)
                .setMessage(R.string.dialog_feature_na_message)
                .setCancelable(false)
                .setPositiveButton(R.string.ui_okay) { _, _ ->
                    if (isFinish) scanForActivity(context)?.finish()
                }

            if (scanForActivity(dialog.context)?.isFinishing == false)
                dialog.show()
        }
    }

    fun scanForActivity(context: Context?): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> scanForActivity(context.baseContext)
            else -> null
        }
    }

    fun logException(e: Exception) {
            e.printStackTrace()
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
                    if (res1.isNotEmpty()) {
                        res1.deleteCharAt(res1.length - 1)
                    }
                    return res1.toString()
                }
            } catch (ex: Exception) {
                //handle exception
            }
            return ""
        }

    fun sendTestBigScreenReport(success : Int){
        val message = ReportTestBigScreen()
        val data = ReportTestBigSceenData()
        data.code = success
        message.data = data
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {}
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }

    fun sendTestSmallScreenReport(success : Int){
        val message = ReportTestSmallScreen()
        val data = ReportTestSmallScreenData()
        data.code = success
        message.data = data
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {}
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }

    fun sendTestSpeakerReport(success : Int){
        val message = ReportTestSpeaker()
        val data = ReportTestSpeakerData()
        data.code = success
        message.data = data
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {}
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }

    fun sendTestMicReport(success : Int){
        val message = ReportTestMic()
        val data = ReportTestMicData()
        data.code = success
        message.data = data
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {}
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }

    fun sendTestTemhumSensorReport(success : Int){
        val message = ReportTestTemhumSensor()
        val data = ReportTestTemhumSensorData()
        data.code = success
        message.data = data
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {}
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }

    fun sendTestPresenceReport(success : Int){
        val message = ReportTestPresenceSensor()
        val data = ReportTestPresenceSensorData()
        data.code = success
        message.data = data
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {}
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }

    fun sendTestTouchBigScreen(success: Int){
        val message = ReportTestBigScreenTouch()
        val data = ReportTestBigScreenTouchData()
        data.code = success
        message.data = data
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {}
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }
}
