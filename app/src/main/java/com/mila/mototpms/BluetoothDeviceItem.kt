package com.mila.mototpms

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

class BluetoothDeviceItem {
    var name = "NO NAME"
    var address = ""
    var rssi = 0
    var data: ByteArray? = null
    var nanos: Long = 0

    @SuppressLint("MissingPermission")
    constructor(result: ScanResult) {
        if (result.device.name != null) {
            name = result.device.name
        }
        address = result.device.address
        rssi = result.rssi
        data = result.scanRecord!!.bytes
        nanos = result.timestampNanos
    }

    @SuppressLint("MissingPermission")
    constructor(device: BluetoothDevice, _rssi: Int) {
        if (device.name != null) {
            name = device.name
        }
        address = device.address
        rssi = _rssi
    }
}