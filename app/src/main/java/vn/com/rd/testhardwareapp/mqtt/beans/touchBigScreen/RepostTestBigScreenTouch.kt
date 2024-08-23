package vn.com.rd.testhardwareapp.mqtt.beans.touchBigScreen

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestBigScreenTouch : BaseMqttMessage() {
    init {
        this.cmd = "touchBigScreen"
    }
    var data : ReportTestBigScreenTouchData? = null
}