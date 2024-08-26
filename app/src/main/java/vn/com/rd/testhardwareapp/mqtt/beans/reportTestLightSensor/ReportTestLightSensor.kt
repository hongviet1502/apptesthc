package vn.com.rd.testhardwareapp.mqtt.beans.reportTestLightSensor

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestLightSensor : BaseMqttMessage() {
    init {
        this.cmd = "lightSensor"
    }
    var data : ReportTestLightSensorData? = null
}