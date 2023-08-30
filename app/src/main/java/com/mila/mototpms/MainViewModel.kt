package com.mila.mototpms

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

const val TAG_VM = "MainViewModel"

class MainViewModel : ViewModel() {

    private var frontAddress = mutableStateOf("")
    private var rearAddress = mutableStateOf("")
    private var frontTemperature = mutableStateOf("")
    private var rearTemperature = mutableStateOf("")
    private var frontPressure = mutableStateOf("")
    private var rearPressure = mutableStateOf("")
    private var frontVoltage = mutableStateOf("")
    private var rearVoltage = mutableStateOf("")
    private var frontNanos = mutableStateOf("")
    private var rearNanos = mutableStateOf("")

    fun init(dataProvider: DataProvider?) {
        frontAddress.value = dataProvider!!.getValue("frontAddress")
        rearAddress.value = dataProvider!!.getValue("rearAddress")
        frontTemperature.value = dataProvider!!.getValue("${frontAddress.value}Temperature")
        rearTemperature.value = dataProvider!!.getValue("${rearAddress.value}Temperature")
        frontPressure.value = dataProvider!!.getValue("${frontAddress.value}Pressure")
        rearPressure.value = dataProvider!!.getValue("${rearAddress.value}Pressure")
        frontVoltage.value = dataProvider!!.getValue("${frontAddress.value}Voltage")
        rearVoltage.value = dataProvider!!.getValue("${rearAddress.value}Voltage")
        frontNanos.value = dataProvider!!.getValue("${frontAddress.value}Nanos")
        rearNanos.value = dataProvider!!.getValue("${rearAddress.value}Nanos")
    }

    fun refreshData(dataProvider: DataProvider?) {
        init(dataProvider)
    }

    fun getFrontAddress(): MutableState<String> {
        return frontAddress
    }

    fun getRearAddress(): MutableState<String> {
        return rearAddress
    }

    fun getFrontTemperature(): MutableState<String> {
        return frontTemperature
    }

    fun getRearTemperature(): MutableState<String> {
        return rearTemperature
    }

    fun getFrontPressure(): MutableState<String> {
        return frontPressure
    }

    fun getRearPressure(): MutableState<String> {
        return rearPressure
    }

    fun getFrontVoltage(): MutableState<String> {
        return frontVoltage
    }

    fun getRearVoltage(): MutableState<String> {
        return rearVoltage
    }

    fun getFrontNanos(): MutableState<String> {
        return frontNanos
    }

    fun getRearNanos(): MutableState<String> {
        return rearNanos
    }

    fun clearData(dataProvider: DataProvider?) {
        dataProvider!!.saveDeviceData("frontAddress", "")
        dataProvider!!.saveDeviceData("rearAddress", "")
    }
}