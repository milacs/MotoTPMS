package com.mila.mototpms

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    companion object {
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
    }

    fun init() {
        frontAddress.value = MotoTPMS.dataProvider!!.getValue("frontAddress")
        rearAddress.value = MotoTPMS.dataProvider!!.getValue("rearAddress")
        frontTemperature.value = MotoTPMS.dataProvider!!.getValue("${frontAddress.value}Temperature")
        rearTemperature.value = MotoTPMS.dataProvider!!.getValue("${rearAddress.value}Temperature")
        frontPressure.value = MotoTPMS.dataProvider!!.getValue("${frontAddress.value}Pressure")
        rearPressure.value = MotoTPMS.dataProvider!!.getValue("${rearAddress.value}Pressure")
        frontVoltage.value = MotoTPMS.dataProvider!!.getValue("${frontAddress.value}Voltage")
        rearVoltage.value = MotoTPMS.dataProvider!!.getValue("${rearAddress.value}Voltage")
        frontNanos.value = MotoTPMS.dataProvider!!.getValue("${frontAddress.value}Nanos")
        rearNanos.value = MotoTPMS.dataProvider!!.getValue("${rearAddress.value}Nanos")
    }

    fun refreshData() {
        init()
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

    fun clearData() {
        MotoTPMS.dataProvider!!.saveValue("frontAddress", "")
        MotoTPMS.dataProvider!!.saveValue("rearAddress", "")
    }

    fun swapSensors() {
        val dataProvider = MotoTPMS.dataProvider

        val temp = dataProvider?.getValue("frontAddress")
        dataProvider?.saveValue("frontAddress", dataProvider.getValue("rearAddress"))
        dataProvider?.saveValue("rearAddress", temp)

        refreshData()
    }
}