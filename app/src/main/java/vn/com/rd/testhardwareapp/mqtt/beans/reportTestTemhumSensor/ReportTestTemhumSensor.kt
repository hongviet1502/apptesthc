package vn.com.rd.testhardwareapp.mqtt.beans.reportTestTemhumSensor

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestTemhumSensor:BaseMqttMessage() {
    init {
        this.cmd = "temhumSensor"
    }
    var data : ReportTestTemhumSensorData? = null
}