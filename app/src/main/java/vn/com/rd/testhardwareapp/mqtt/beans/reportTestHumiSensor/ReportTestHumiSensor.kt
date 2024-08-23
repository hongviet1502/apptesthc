package vn.com.rd.testhardwareapp.mqtt.beans.reportTestHumiSensor

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestHumiSensor : BaseMqttMessage() {
    init {
        this.cmd = "humSensor"
    }
    var data : ReportTestHumiSensorData? = null
}