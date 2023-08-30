package com.mila.mototpms

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.NumberFormat
import java.util.Locale

private const val TAG_DATA_PROVIDER = "DataProvider"

class DataProvider(context: Context, name: String, mode: Int) {

    companion object {
        private lateinit var sharedPrefs: SharedPreferences
        private lateinit var editor: SharedPreferences.Editor
    }

    init {
        sharedPrefs = context.getSharedPreferences(name, mode)
        editor = sharedPrefs.edit()
    }


    fun savePairedDevice(sensorPosition: String?, address: String?, processedData: HashMap<String, Any>?) {
        Log.i(TAG_PAIR, "Saving $address for ${sensorPosition}Address")
        saveDeviceData(address, processedData)
        editor.putString("${sensorPosition}Address", address)
        val commit = editor.commit()

        if (!commit) {
            Log.e(TAG_PAIR, "Could not save address for ${sensorPosition}Address")
        }
    }

    fun saveDeviceData(key: String?, value: String?) {
        Log.i(TAG_DATA_PROVIDER, "Saving $key = $value")
        val editor = sharedPrefs.edit()
        editor?.putString(key, value)
        val commit = editor?.commit()

        if (!commit!!) {
            Log.e(TAG_DATA_PROVIDER, "Could not save data for $key = $value")
        }
    }

    fun saveDeviceData(address: String?, processedData: HashMap<String, Any>?) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        numberFormat.maximumFractionDigits = 1

        saveDeviceData("${address}Temperature", processedData?.get("temperature").toString())
        saveDeviceData("${address}Pressure", numberFormat.format(processedData?.get("pressure")))
        saveDeviceData("${address}Voltage", numberFormat.format(processedData?.get("voltage")))
        saveDeviceData("${address}ST", processedData?.get("st").toString())
        saveDeviceData("${address}Nanos", processedData?.get("nanos").toString())
    }

    fun getValue(key: String): String {
        return sharedPrefs.getString(key, "").toString()
    }
}