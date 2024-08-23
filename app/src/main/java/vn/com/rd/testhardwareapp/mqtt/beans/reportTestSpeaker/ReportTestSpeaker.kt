package vn.com.rd.testhardwareapp.mqtt.beans.reportTestSpeaker

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestSpeaker : BaseMqttMessage() {
    init {
        this.cmd = "speaker"
    }

    var data : ReportTestSpeakerData? = null
}