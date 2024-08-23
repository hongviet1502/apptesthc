package vn.com.rd.testhardwareapp.mqtt.beans.reportTestSmallScreen

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class ReportTestSmallScreen : BaseMqttMessage(){
    init {
        this.cmd = "smallScreen"
    }

    var data : ReportTestSmallScreenData? = null
}