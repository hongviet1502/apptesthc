package vn.com.rd.testhardwareapp.mqtt.beans.reportTestHC

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestHCRequest : BaseMqttMessage() {
    var data : ReportTestHCRequestData? = null

    init {
        this.cmd = "reportTestHC"
    }
}