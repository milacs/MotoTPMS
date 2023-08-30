package com.mila.mototpms

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel

class PairDevicesViewModel : ViewModel() {
    companion object {
        private var devicesList = mutableStateListOf<BluetoothDeviceItem>()
    }

    fun getData(): SnapshotStateList<BluetoothDeviceItem> {
        return devicesList
    }

    fun addItem(device: BluetoothDeviceItem) {
        var deviceFound = false
        devicesList.forEach { d ->
            if (d.address == device.address) {
                deviceFound = true
                return@forEach
            }
        }
        if (!deviceFound) {
            devicesList.add(device)
            devicesList.sortByDescending { it.rssi }
        }
    }

    fun clearData() {
        devicesList.clear()
    }
}