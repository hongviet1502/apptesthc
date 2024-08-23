package vn.com.rd.testhardwareapp.mqtt.beans.reportTestMic

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestMic:BaseMqttMessage() {
    init {
        this.cmd = "mic"
    }
    var data:ReportTestMicData? = null
}