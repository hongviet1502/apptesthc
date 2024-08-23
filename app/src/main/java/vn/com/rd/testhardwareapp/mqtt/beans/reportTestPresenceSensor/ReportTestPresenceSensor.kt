package vn.com.rd.testhardwareapp.mqtt.beans.reportTestPresenceSensor

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestPresenceSensor:BaseMqttMessage() {
    init {
        this.cmd = "presenceSensor"
    }

    var data : ReportTestPresenceSensorData? = null
}