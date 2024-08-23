package vn.com.rd.testhardwareapp.mqtt.beans.getValueSensor

import vn.com.rd.testhardwareapp.mqtt.BaseMqttMessage

class GetValueSensorRequest : BaseMqttMessage() {
    init {
        this.cmd = "getValueSensor"
    }
    var data = {}
}