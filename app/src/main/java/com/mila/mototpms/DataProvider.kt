package com.mila.mototpms

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
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
        saveValues(address, processedData)
        editor.putString("${sensorPosition}Address", address)
        val commit = editor.commit()

        if (!commit) {
            Log.e(TAG_PAIR, "Could not save address for ${sensorPosition}Address")
        }
    }

    fun saveValue(key: String?, value: String?) {
        Log.i(TAG_DATA_PROVIDER, "Saving $key = $value")
        val editor = sharedPrefs.edit()
        editor?.putString(key, value)
        val commit = editor?.commit()

        if (!commit!!) {
            Log.e(TAG_DATA_PROVIDER, "Could not save data for $key = $value")
        }
    }

    fun saveValues(address: String?, processedData: HashMap<String, Any>?) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        numberFormat.maximumFractionDigits = 1

        saveValue("${address}Temperature", processedData?.get("temperature").toString())
        saveValue("${address}Pressure", numberFormat.format(processedData?.get("pressure")))
        saveValue("${address}Voltage", numberFormat.format(processedData?.get("voltage")))
        saveValue("${address}ST", processedData?.get("st").toString())

        val timestamp = getResultTimestamp(processedData?.get("nanos").toString())
        saveValue("${address}Nanos", timestamp)
    }

    private fun getResultTimestamp(nanos: String): String {
        if (nanos == "") return R.string.not_synced_yet.toString()

        val timestampMilliseconds = System.currentTimeMillis() -
                SystemClock.elapsedRealtime() +
                nanos.toLong() / 1000000

        return SimpleDateFormat("E dd/MM/yy HH:mm:ss").format(Date(timestampMilliseconds))
    }

    fun getValue(key: String): String {
        return sharedPrefs.getString(key, "").toString()
    }

    fun isPaired(address: String): String {
        val frontAddress = getValue("frontAddress")
        val rearAddress = getValue("rearAddress")

        if (address == frontAddress)
            return "front"
        else if (address == rearAddress)
            return "rear"
        return ""
    }

    fun saveMacAddress(sensorPosition: String?, macInput: String) {
        Log.i(TAG_PAIR, "Saving $macInput for ${sensorPosition}Address")
        editor.putString("${sensorPosition}Address", macInput)
        val commit = editor.commit()

        if (!commit) {
            Log.e(TAG_PAIR, "Could not save address for ${sensorPosition}Address")
        }
    }
}