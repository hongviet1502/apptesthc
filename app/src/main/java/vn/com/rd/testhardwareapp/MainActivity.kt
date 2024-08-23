package vn.com.rd.testhardwareapp

import android.content.Intent
import android.hardware.Sensor
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import vn.com.rd.testhardwareapp.databinding.ActivityMainBinding
import vn.com.rd.testhardwareapp.mqtt.HCCoreMqttClient
import vn.com.rd.testhardwareapp.mqtt.MessageSenderService
import vn.com.rd.testhardwareapp.mqtt.OnResponseListener
import vn.com.rd.testhardwareapp.mqtt.SendMessageAsync
import vn.com.rd.testhardwareapp.mqtt.beans.getValueSensor.GetValueSensorRequest
import vn.com.rd.testhardwareapp.ui.activity.MicrophoneActivity
import vn.com.rd.testhardwareapp.ui.activity.MiniScreenActivity
import vn.com.rd.testhardwareapp.ui.activity.ScreenActivity
import vn.com.rd.testhardwareapp.ui.activity.SensorActivity
import vn.com.rd.testhardwareapp.ui.activity.SoundActivity
import vn.com.rd.testhardwareapp.ui.activity.TouchActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val macAddr: String = Utils.macAddr
        Log.i(TAG, "MAC HC: $macAddr")
        val intent1 = Intent(this, MessageSenderService::class.java)
        intent1.putExtra(
            "REQUEST_TOPIC",
            "/v2/mobile/F072212302019000995/hc/" + macAddr.lowercase(Locale.getDefault()) + "/json_req"
        )
        intent1.putExtra("REQUEST_SCREEN_TOPIC", "/hc/lcd")
        startService(intent1)
        val hcCoreMqttClient: HCCoreMqttClient? = HCCoreMqttClient.instance
        if (hcCoreMqttClient != null) {
            hcCoreMqttClient.setContext(this)
            hcCoreMqttClient.brokerUrl = "tcp://localhost:1883"
            hcCoreMqttClient.clientId = "idtestapp"
            hcCoreMqttClient.subscribe("/v2/hc/" + macAddr.lowercase(Locale.getDefault()) + "/mobile/+/json_resp")
            hcCoreMqttClient.subscribe("/v2/hc/" + macAddr.lowercase(Locale.getDefault()) + "/mobile/all/json_req")
            hcCoreMqttClient.subscribe("/v2/hc/" + macAddr.lowercase(Locale.getDefault()) + "/server/json_req")
            hcCoreMqttClient.subscribe("/lcd/hc")
        }

        getAverageSensorValue()
        // Truy xuất string array từ resources
        val testCategoryArray = resources.getStringArray(R.array.test_category)

        // Chuyển đổi thành List
        val testCategoryList = testCategoryArray.toList()
        mainAdapter = MainAdapter(testCategoryList)
        binding.rvCategory.setHasFixedSize(true)
        binding.rvCategory.layoutManager =
            GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        binding.rvCategory.itemAnimator = DefaultItemAnimator()
        binding.rvCategory.adapter = mainAdapter
        mainAdapter.setOnItemClickListener(object : MainAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val category = testCategoryArray[position]
                when (category) {
                    getString(R.string.category_screen) -> {
                        val intent = Intent(this@MainActivity, ScreenActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.category_mini_screen) -> {
                        val intent = Intent(this@MainActivity, MiniScreenActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.category_sound) -> {
                        val intent = Intent(this@MainActivity, SoundActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.category_microphone) -> {
                        val intent = Intent(this@MainActivity, MicrophoneActivity::class.java)
                        startActivity(intent)
                    }

                    getString(R.string.category_proximity) -> {
                        val intent =
                            SensorActivity.newInstance(this@MainActivity, Sensor.TYPE_PROXIMITY)
                        startActivity(intent)
                    }

                    getString(R.string.category_light) -> {
                        val intent =
                            SensorActivity.newInstance(this@MainActivity, Sensor.TYPE_LIGHT)
                        startActivity(intent)
                    }

                    getString(R.string.category_accelerometer) -> {
                        val intent =
                            SensorActivity.newInstance(this@MainActivity, Sensor.TYPE_ACCELEROMETER)
                        startActivity(intent)
                    }

                    getString(R.string.category_humidity) -> {
                        val intent = SensorActivity.newInstance(
                            this@MainActivity,
                            2804
                        )
                        startActivity(intent)
                    }

                    getString(R.string.category_temperature) -> {
                        val intent = SensorActivity.newInstance(
                            this@MainActivity,
                            2805
                        )
                        startActivity(intent)
                    }
                    getString(R.string.category_touch) -> {
                        val intent = Intent(this@MainActivity, TouchActivity::class.java)
                        startActivity(intent)
                    }
                    else -> {

                    }
                }
            }
        })

    }

    fun getAverageSensorValue() {
        val message = GetValueSensorRequest()
        val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
            override fun onResponse(result: String?) {

            }
        }, addToQueue = true)
        sendMessageAsync.execute(message)
    }

    companion object {
        var average_temp = 0
        var average_humi = 0
    }
}