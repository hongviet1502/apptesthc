package vn.com.rd.testhardwareapp.mqtt.beans.reportTestBigScreen

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage
import vn.com.rd.testhardwareapp.mqtt.beans.reportTestHC.ReportTestHCRequestData

class ReportTestBigScreen : BaseMqttMessage() {
    var data : ReportTestBigSceenData? = null

    init {
        this.cmd = "bigScreen"
    }
}