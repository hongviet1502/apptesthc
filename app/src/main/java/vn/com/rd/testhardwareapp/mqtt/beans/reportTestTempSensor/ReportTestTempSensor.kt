package vn.com.rd.testhardwareapp.mqtt.beans.reportTestTempSensor

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestTempSensor : BaseMqttMessage() {
    init {
        this.cmd = "temSensor"
    }

    var data : ReportTestTempSensorData? = null
}