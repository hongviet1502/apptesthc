package vn.com.rd.testhardwareapp.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import vn.com.rd.testhardwareapp.MainActivity
import vn.com.rd.testhardwareapp.R
import vn.com.rd.testhardwareapp.SensorUtils
import vn.com.rd.testhardwareapp.SensorUtils.getAccelerometerSensorData
import vn.com.rd.testhardwareapp.SensorUtils.getGravitySensorData
import vn.com.rd.testhardwareapp.SensorUtils.getHumiditySensorData
import vn.com.rd.testhardwareapp.SensorUtils.getLightSensorData
import vn.com.rd.testhardwareapp.SensorUtils.getPressureSensorData
import vn.com.rd.testhardwareapp.SensorUtils.getProximitySensorData
import vn.com.rd.testhardwareapp.SensorUtils.getStepCounterSensorData
import vn.com.rd.testhardwareapp.SensorUtils.getTemperatureCounterSensorData
import vn.com.rd.testhardwareapp.Utils
import vn.com.rd.testhardwareapp.Utils.logException
import vn.com.rd.testhardwareapp.databinding.ActivitySensorBinding
import vn.com.rd.testhardwareapp.model.InfoItem
import vn.com.rd.testhardwareapp.mqtt.OnResponseListener
import vn.com.rd.testhardwareapp.mqtt.SendMessageAsync
import vn.com.rd.testhardwareapp.mqtt.beans.getValueSensor.GetValueSensorRequest
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestHumiSensor.ReportTestHumiSensor
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestHumiSensor.ReportTestHumiSensorData
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestTempSensor.ReportTestTempSensor
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestTempSensor.ReportTestTempSensorData
import vn.com.rd.testhardwareapp.ui.adapter.InfoItemAdapter
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import java.io.BufferedReader
import java.io.InputStreamReader

class SensorActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySensorBinding

    private lateinit var mSensorManager: SensorManager
    private var mSensor: Sensor? = null
    private var secondSensor: Sensor? = null
    private var sensorEventListener: SensorEventListener? = null
    private var sensorType = 0
    private var reading = ""
    private var initReading = 0f
    private var maxGraphY = 0.0
    private var minGraphY = 0.0
    private var series = LineGraphSeries(arrayOf<DataPoint>())
    private var series2 = LineGraphSeries(arrayOf<DataPoint>())
    private var series3 = LineGraphSeries(arrayOf<DataPoint>())
    private var lastXValue = 0.0
    private var adapter: InfoItemAdapter? = null
    private lateinit var list: MutableList<InfoItem>
    private var count = 0;
//    private lateinit var formatter: CompassDirectionsFormatter


    companion object {
        const val ARG_SENSOR_TYPE = "sensorType"

        /*
         Meaning of the constants
         M: Mass constant
         TN: Temperature constant
         TA: Temperature constant
         A: Pressure constant in hP
         K: Temperature constant for converting to kelvin
         */
        const val TN = 243.12f
        const val MASS = 17.62f
        const val K = 273.15f
        const val A = 6.112f
        const val Ta = 216.7f
        fun newInstance(context: Context, sensorType: Int): Intent {
            val intent = Intent(context, SensorActivity::class.java)
            intent.putExtra(ARG_SENSOR_TYPE, sensorType)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySensorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sensorType = intent.getIntExtra(ARG_SENSOR_TYPE, -1)
        init()
        when (sensorType) {
            Sensor.TYPE_LIGHT -> {
                binding.tbSensor.title = getString(R.string.category_light)
            }

            Sensor.TYPE_PROXIMITY -> {
                binding.tbSensor.title = getString(R.string.category_proximity)
            }

            Sensor.TYPE_ACCELEROMETER -> {
                binding.tbSensor.title = getString(R.string.category_accelerometer)
            }

            2804 -> {
                binding.tbSensor.title = getString(R.string.category_humidity)
                Log.i("SenSorActivity", "checkSensorValues: ${checkSensorValues(sensorType)}")
            }
            2805 -> {
                binding.tbSensor.title = getString(R.string.category_temperature)
                Log.i("SenSorActivity", "checkSensorValues: ${checkSensorValues(sensorType)}")
            }
            else -> {
                binding.tbSensor.title = "Cảm biến không hỗ trợ"
            }
        }
        val backArrowDrawable = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        setSupportActionBar(binding.tbSensor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(backArrowDrawable)
    }

    override fun onPause() {
        super.onPause()
        if (mSensor != null && sensorEventListener != null) {
            mSensorManager.unregisterListener(sensorEventListener)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mSensor != null && sensorEventListener != null) {
            mSensorManager.registerListener(
                sensorEventListener, mSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (secondSensor != null) {
                mSensorManager.registerListener(
                    sensorEventListener, secondSensor,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }
    }


    private fun init() {
        // Lấy SensorManager từ hệ thống
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Lấy danh sách các cảm biến có sẵn
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

        // Chuyển đổi danh sách cảm biến thành danh sách tên cảm biến
        val sensorNames = deviceSensors.map { it.name }
        Log.i("TAG", "sensorType: $sensorType")
        for (name in sensorNames) {
            Log.i("TAG", "sensor support: $name")
        }
        binding.let { viewBinding ->
            this@SensorActivity.let { context ->
                list = ArrayList()
                mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                try {
                    mSensor = mSensorManager.getDefaultSensor(sensorType)
                    if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                        secondSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    }
                    if (sensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
                        secondSensor =
                            mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
                    }
                    Log.i("TAG", "mSensor: $mSensor")
                    Log.i("TAG", "secondSensor: $secondSensor")
                    if (mSensor == null && secondSensor == null && sensorType != 2804 && sensorType != 2805) {
                        Utils.errorNoFeatureDialog(this@SensorActivity)
                        return
                    }
                } catch (e: Exception) {
                    logException(e)
                    Utils.errorNoFeatureDialog(this@SensorActivity)
                    return
                }

                initSensor()

                if (sensorType != Sensor.TYPE_MAGNETIC_FIELD) {
                    initGraphView(context, viewBinding)
                    viewBinding.graphView.visibility = View.VISIBLE
                }

                initRecyclerview(viewBinding)
            }
        }
    }

    private fun initSensor() {
        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GRAVITY -> sensorEventListener = accelerometerListener
            Sensor.TYPE_LIGHT -> sensorEventListener = lightListener
            Sensor.TYPE_PRESSURE -> sensorEventListener = pressureListener
            Sensor.TYPE_PROXIMITY -> sensorEventListener = proximityListener
            Sensor.TYPE_STEP_COUNTER -> sensorEventListener = stepListener
            Sensor.TYPE_AMBIENT_TEMPERATURE -> sensorEventListener = temperatureListener
            Sensor.TYPE_RELATIVE_HUMIDITY -> sensorEventListener = humidityListener
        }
    }

    private fun initGraphView(context: Context, viewBinding: ActivitySensorBinding) {
        var isGraph2 = false
        var isGraph3 = false
        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> {
                isGraph3 = true
                isGraph2 = true
                mSensor?.maximumRange?.toDouble()?.let {
                    viewBinding.graphView.viewport.isYAxisBoundsManual = true
                    viewBinding.graphView.viewport.setMinY(-it)
                    viewBinding.graphView.viewport.setMaxY(it)
                    maxGraphY = it
                    minGraphY = -it
                }
            }

            Sensor.TYPE_GRAVITY -> {
                isGraph3 = true
                isGraph2 = true
                mSensor?.maximumRange?.toDouble()?.let {
                    viewBinding.graphView.viewport.isYAxisBoundsManual = true
                    viewBinding.graphView.viewport.setMinY(-it)
                    viewBinding.graphView.viewport.setMaxY(it)
                    maxGraphY = it
                    minGraphY = -it
                }
            }

            Sensor.TYPE_PRESSURE -> {
                mSensor?.maximumRange?.toDouble()?.let {
                    viewBinding.graphView.viewport.isYAxisBoundsManual = true
                    viewBinding.graphView.viewport.setMinY(0.0)
                    viewBinding.graphView.viewport.setMaxY(it)
                    maxGraphY = it
                    minGraphY = 0.0
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                mSensor?.maximumRange?.toDouble()?.let {
                    viewBinding.graphView.viewport.isYAxisBoundsManual = true
                    viewBinding.graphView.viewport.setMinY(0.0)
                    viewBinding.graphView.viewport.setMaxY(it)
                    maxGraphY = it
                    minGraphY = 0.0
                }
            }
        }
        series.color = ContextCompat.getColor(context, R.color.lineColor3)
        series.thickness = context.convertDpToPx(4)
        viewBinding.graphView.addSeries(series)
        if (isGraph2) {
            series2.color = ContextCompat.getColor(context, R.color.lineColor1)
            series2.thickness = context.convertDpToPx(4)
            viewBinding.graphView.addSeries(series2)
        }
        if (isGraph3) {
            series3.color = ContextCompat.getColor(context, R.color.lineColor4)
            series3.thickness = context.convertDpToPx(4)
            viewBinding.graphView.addSeries(series3)
        }
        viewBinding.graphView.gridLabelRenderer.gridColor = Color.GRAY
        viewBinding.graphView.gridLabelRenderer.isHighlightZeroLines = false
        viewBinding.graphView.gridLabelRenderer.isHorizontalLabelsVisible = false
        viewBinding.graphView.gridLabelRenderer.padding = context.convertDpToPx(10)
        viewBinding.graphView.gridLabelRenderer.labelVerticalWidth = context.convertDpToPx(32)
        viewBinding.graphView.gridLabelRenderer.gridStyle =
            GridLabelRenderer.GridStyle.HORIZONTAL
        viewBinding.graphView.viewport.isXAxisBoundsManual = true
        viewBinding.graphView.viewport.setMinX(0.0)
        viewBinding.graphView.viewport.setMaxX(36.0)
        viewBinding.graphView.viewport.isScrollable = false
        viewBinding.graphView.viewport.isScalable = false
    }

    private fun initRecyclerview(viewBinding: ActivitySensorBinding) {
        val stringArray = resources.getStringArray(R.array.test_sensor_string_array)
        for (i in stringArray.indices) {
            var infoItem: InfoItem = try {
                when (sensorType) {
                    Sensor.TYPE_ACCELEROMETER -> InfoItem(
                        stringArray[i],
                        getAccelerometerSensorData(i, stringArray.size, reading, mSensor!!)
                    )

                    Sensor.TYPE_GRAVITY -> InfoItem(
                        stringArray[i],
                        getGravitySensorData(i, stringArray.size, reading, mSensor!!)
                    )

                    Sensor.TYPE_LIGHT -> InfoItem(
                        stringArray[i],
                        getLightSensorData(i, stringArray.size, reading, mSensor!!)
                    )

                    Sensor.TYPE_PRESSURE -> InfoItem(
                        stringArray[i],
                        getPressureSensorData(i, stringArray.size, reading, mSensor!!)
                    )

                    Sensor.TYPE_PROXIMITY -> InfoItem(
                        stringArray[i],
                        getProximitySensorData(i, stringArray.size, reading, mSensor!!)
                    )

                    Sensor.TYPE_STEP_COUNTER -> InfoItem(
                        stringArray[i],
                        getStepCounterSensorData(i, stringArray.size, reading, mSensor!!)
                    )

                    Sensor.TYPE_AMBIENT_TEMPERATURE -> InfoItem(
                        stringArray[i],
                        getTemperatureCounterSensorData(
                            i,
                            stringArray.size,
                            reading,
                            mSensor!!
                        )
                    )

                    Sensor.TYPE_RELATIVE_HUMIDITY -> InfoItem(
                        stringArray[i],
                        getHumiditySensorData(i, stringArray.size, reading, mSensor!!)
                    )

                    else -> InfoItem(stringArray[i], getString(R.string.ui_not_support))
                }
            } catch (e: Exception) {
                InfoItem(stringArray[i], getString(R.string.ui_not_support))
            }
            if(sensorType == 2804 && i == 0) {
                val humidity = readHumiSensor()
                infoItem = InfoItem("Độ ẩm", humidity)
            }
            if(sensorType == 2805 && i == 0) {
                val temperature = readTempSensor()
                infoItem = InfoItem("Nhiệt độ", temperature)
            }
            list.add(infoItem)
            if(sensorType == 2804) {
                val handler = Handler()
                val taskSensor: Runnable = object : Runnable {
                    override fun run() {
                        val humi = readHumiSensor()
                        list[0].contentText = humi
                        adapter?.notifyDataSetChanged()
                        // Post the task again after 10 seconds
                        handler.postDelayed(this, 1000) // 10000 milliseconds = 10 seconds
                    }
                }
                handler.postDelayed(taskSensor, 1000)
            }
            if(sensorType == 2805) {
                val handler = Handler()
                val taskSensor: Runnable = object : Runnable {
                    override fun run() {
                        val temp = readTempSensor()
                        list[0].contentText = temp
                        adapter?.notifyDataSetChanged()
                        // Post the task again after 10 seconds
                        handler.postDelayed(this, 1000) // 10000 milliseconds = 10 seconds
                    }
                }
                handler.postDelayed(taskSensor, 1000)
            }
        }
        adapter = InfoItemAdapter().apply {
            setData(list)
        }
        viewBinding.rvlist.adapter = adapter
        viewBinding.rvlist.disableChangeAnimation()
    }

    private val accelerometerListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            val value0 = event.values[0]
            val value1 = event.values[1]
            val value2 = event.values[2]
            updateGraphY(value0)
            updateGraphY(value1)
            updateGraphY(value2)

            reading = (
                    "X: " + String.format("%1.4f", value0) + " m/s²\nY: " +
                            String.format("%1.4f", value1) + " m/s²\nZ: " +
                            String.format("%1.4f", value2) + " m/s²"
                    )
            lastXValue += 1.0
            series.appendData(
                DataPoint(lastXValue, value0.toDouble()),
                true, 100
            )
            series2.appendData(
                DataPoint(lastXValue, value1.toDouble()),
                true, 100
            )
            series3.appendData(
                DataPoint(lastXValue, value2.toDouble()),
                true, 100
            )
            binding.graphView.viewport?.scrollToEnd()
            list[0].contentText = reading
            adapter?.notifyItemChanged(0)
        }
    }
    private val lightListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            val value = event.values[0]
            updateGraphY(value)
            reading = "$value lux"
            series.appendData(DataPoint(lastXValue, value.toDouble()), true, 36)
            binding.graphView.viewport?.scrollToEnd()
            lastXValue += 1.0
            list[0].contentText = reading
            adapter?.notifyItemChanged(0)
        }
    }
    private val pressureListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            val value = event.values[0]
            updateGraphY(value)
            reading = "$value hPa"
            series.appendData(DataPoint(lastXValue, value.toDouble()), true, 36)
            binding.graphView.viewport?.scrollToEnd()
            lastXValue += 1.0
            list[0].contentText = reading
            adapter?.notifyItemChanged(0)
        }
    }
    private val proximityListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            val value = event.values[0]
            updateGraphY(value)
            reading = String.format("%1.2f", value) + " cm"
            lastXValue += 1.0
            list[0].contentText = reading
            series.appendData(DataPoint(lastXValue, value.toDouble()), true, 36)
            binding.graphView.viewport?.scrollToEnd()
            adapter?.notifyItemChanged(0)
        }
    }
    private val stepListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            var value = event.values[0]
            if (initReading == 0f) {
                initReading = value
            }
            updateGraphY(value + 100)
            value -= initReading
            reading = "$value Steps"
            lastXValue += 1.0
            list[0].contentText = reading
            series.appendData(DataPoint(lastXValue, value.toDouble()), true, 36)
            binding.graphView.viewport?.scrollToEnd()
            adapter?.notifyItemChanged(0)
        }
    }
    private val temperatureListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            val valueC = event.values[0]
            val valueF = valueC * 1.8 + 32
            reading =
                String.format("%1.2f", valueC) + " °C\n" + String.format("%1.2f", valueF) + " °F"
            updateGraphY(valueC)
            series.appendData(DataPoint(lastXValue, valueC.toDouble()), true, 36)
            binding.graphView.viewport?.scrollToEnd()
            lastXValue += 1.0
            list[0].contentText = reading
            adapter?.notifyItemChanged(0)
        }
    }

    private var mLastKnownRelativeHumidity = 0f
    private var mLastKnownTemperature = 0f
    private var mLastKnownAbsoluteHumidity = 0f
    private var mLastKnownDewPoint = 0f
    private val humidityListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            val value = event.values[0]
            updateGraphY(value)
            if (event.sensor.type == Sensor.TYPE_RELATIVE_HUMIDITY) {
                mLastKnownRelativeHumidity = value
            } else if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                if (mLastKnownRelativeHumidity != 0f) {
                    mLastKnownTemperature = value
                    mLastKnownAbsoluteHumidity =
                        calculateAbsoluteHumidity(mLastKnownTemperature, mLastKnownRelativeHumidity)
                    mLastKnownDewPoint =
                        calculateDewPoint(mLastKnownTemperature, mLastKnownRelativeHumidity)
                }
            }
            reading = getString(R.string.ui_relative_humidity) + mLastKnownRelativeHumidity + "%"
            reading += getString(R.string.ui_absolute_humidity) + mLastKnownTemperature + "°C/" + mLastKnownAbsoluteHumidity + "%"
            reading += getString(R.string.ui_dew_point) + mLastKnownTemperature + "°C/" + mLastKnownDewPoint
        }
    }

    fun calculateAbsoluteHumidity(temperature: Float, relativeHumidity: Float): Float {
        return (Ta * (relativeHumidity / 100) * A * exp(MASS * temperature / (TN + temperature)) / (K + temperature))
    }

    fun calculateDewPoint(temperature: Float, relativeHumidity: Float): Float {
        return (
                TN * (
                        (ln(relativeHumidity / 100) + MASS * temperature / (TN + temperature)) /
                                (MASS - (ln(relativeHumidity / 100) + MASS * temperature / (TN + temperature)))
                        )
                )
    }

    private fun updateGraphY(value: Float) {
        binding.let { binding ->
            if (value > maxGraphY) {
                maxGraphY = value.toDouble()
                binding.graphView.viewport.setMaxY(maxGraphY)
            }
            if (value < minGraphY) {
                minGraphY = value.toDouble()
                binding.graphView.viewport.setMinY(minGraphY)
            }
        }
    }

    private fun Context.convertDpToPx(dp: Int): Int {
        return (dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    private fun RecyclerView?.disableChangeAnimation() {
        if (this == null) return
        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    fun readSensorData(filePath: String): String {
        try {
            // Tạo tiến trình để thực thi lệnh cat
            val process = ProcessBuilder("su", "-c", "cat $filePath").start()
            // Đọc kết quả đầu ra từ tiến trình
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            // Đọc từng dòng và thêm vào StringBuilder
            while (reader.readLine().also { line = it } != null) {
                output.append(line)
            }

            // Đóng reader
            reader.close()

            // Trả về kết quả đầu ra dưới dạng chuỗi
            return output.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: ${e.message}"
        }
    }


    fun checkSensorValues(typeSensor : Int) {
        val handler = Handler()
        val sensorTask = Runnable {
            var allPass = true
            runBlocking {
                repeat(3) { iteration ->
                    if(typeSensor == 2804){
                        val sensorValue = readHumiSensor()
                        Log.i("TAG", "checkSensorValues: $sensorValue")
                        if (!SensorUtils.isWithinErrorRange(MainActivity.average_humi.toDouble(), sensorValue.toDoubleOrNull()
                                ?.div(1000)
                                ?: 0.0)) {
                            allPass = false
                        }
                    }
                    if(typeSensor == 2805){
                        val sensorValue = readTempSensor()
                        Log.i("TAG", "checkSensorValues: $sensorValue")
                        if (!SensorUtils.isWithinErrorRange(MainActivity.average_temp.toDouble(), sensorValue.toDoubleOrNull()
                                ?.div(1000)
                                ?: 0.0)) {
                            allPass = false
                        }
                    }
                    if (iteration < 2) { // Đợi 3 giây trừ lần lặp cuối
                        delay(3000)
                    }
                }
            }
            Log.i("SensorActivity", "allPass: $allPass")
            if(allPass){
                if(typeSensor == 2804){
                    val message = ReportTestHumiSensor()
                    val data = ReportTestHumiSensorData()
                    data.code = 0
                    message.data = data
                    val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
                        override fun onResponse(result: String?) {

                        }
                    }, addToQueue = true)
                    sendMessageAsync.execute(message)
                }
                if(typeSensor == 2805){
                    val message = ReportTestTempSensor()
                    val data = ReportTestTempSensorData()
                    data.code = 0
                    message.data = data
                    val sendMessageAsync = SendMessageAsync(object : OnResponseListener {
                        override fun onResponse(result: String?) {
                        }
                    }, addToQueue = true)
                    sendMessageAsync.execute(message)
                }
            }
        }
        handler.postDelayed(sensorTask, 1000)

    }

    fun readTempSensor(): String {
        val temperaturePath = "/sys/devices/platform/twi.1/i2c-1/1-0040/temp1_input"
        return readSensorData(temperaturePath)
    }

    fun readHumiSensor(): String {
        val humidityPath = "/sys/devices/platform/twi.1/i2c-1/1-0040/humidity1_input"
        return readSensorData(humidityPath)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // Xử lý khi nhấn nút back
        return true
    }
}