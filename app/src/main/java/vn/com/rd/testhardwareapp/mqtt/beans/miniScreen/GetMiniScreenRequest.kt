package vn.com.rd.testhardwareapp.mqtt.beans.miniScreen

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage


class GetMiniScreenRequest : BaseMqttMessage() {
    var data: GetMiniScreenRequestData? = null

    init {
        this.cmd = "ChangeScreen"
    }
}
