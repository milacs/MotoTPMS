package com.mila.mototpms

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import androidx.activity.result.ActivityResultLauncher
import kotlin.math.abs

private const val TAG_BT_COMM = "BluetoothConnectionManager"

class BluetoothConnectionManager(context : Context) {

    private var mBluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var bluetoothLeScanner : BluetoothLeScanner? = mBluetoothAdapter?.bluetoothLeScanner
    private var context = context
    private var pairViewModel = PairDevicesViewModel()

    companion object {
        private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
        private const val USER_MASK = 65535
        private var auchCRCHi = byteArrayOf(
            -115,
            113,
            -89,
            0,
            -43,
            118,
            54,
            -74,
            108,
            126,
            94,
            -15,
            86,
            91,
            -123,
            -75,
            -72,
            -92,
            1,
            -59,
            53,
            -20,
            -90,
            10,
            -22,
            107,
            117,
            -90,
            79,
            -106,
            82,
            -57,
            -73,
            43,
            -103,
            -84,
            -43,
            -36,
            -66,
            66,
            39,
            -29,
            -107,
            -22,
            -72,
            -50,
            -75,
            -93,
            -106,
            -124,
            114,
            Byte.MAX_VALUE,
            64,
            111,
            -110,
            -78,
            -118,
            -85,
            -108,
            54,
            45,
            111,
            -80,
            123,
            38,
            -66,
            -110,
            -122,
            -32,
            -123,
            -64,
            66,
            22,
            -75,
            -40,
            -22,
            109,
            -11,
            -38,
            -108,
            117,
            -7,
            43,
            -105,
            15,
            23,
            85,
            97,
            -94,
            89,
            64,
            94,
            47,
            -76,
            116,
            -119,
            115,
            15,
            -41,
            15,
            -41,
            -18,
            56,
            36,
            87,
            75,
            -57,
            -31,
            47,
            -25,
            -125,
            -112,
            -28,
            -83,
            -74,
            6,
            -100,
            77,
            Byte.MAX_VALUE,
            -105,
            -65,
            52,
            48,
            26,
            48,
            -24,
            -64,
            -15,
            -48,
            -80,
            -88,
            69,
            53,
            -37,
            31,
            -87,
            91,
            -63,
            70,
            -98,
            106,
            -44,
            15,
            8,
            119,
            -84,
            100,
            -28,
            99,
            -29,
            -44,
            62,
            8,
            105,
            -116,
            -51,
            46,
            -8,
            -9,
            122,
            -8,
            101,
            26,
            -107,
            -90,
            77,
            122,
            -17,
            111,
            121,
            40,
            18,
            -19,
            44,
            -108,
            58,
            27,
            -112,
            -115,
            -72,
            124,
            -25,
            9,
            -102,
            -44,
            -124,
            97,
            99,
            -78,
            28,
            -19,
            -60,
            92,
            -33,
            10,
            -25,
            1,
            53,
            87,
            -77,
            3,
            -127,
            52,
            97,
            6,
            -6,
            6,
            -83,
            45,
            -26,
            83,
            -102,
            111,
            -106,
            25,
            48,
            -92,
            -64,
            73,
            24,
            -85,
            104,
            -110,
            -50,
            94,
            -87,
            2,
            -79,
            54,
            20,
            -45,
            -86,
            -52,
            7,
            -83,
            120,
            5,
            -84,
            -57,
            21,
            Byte.MAX_VALUE,
            -92,
            -33,
            10,
            -32,
            8,
            76,
            -53,
            -93,
            76,
            99,
            32,
            -115,
            -64,
            -102,
            59
        )
        private var auchCRCLo = byteArrayOf(
            32,
            -64,
            -63,
            17,
            -61,
            3,
            2,
            -62,
            -58,
            126,
            Byte.MAX_VALUE,
            -65,
            125,
            -67,
            -68,
            124,
            -52,
            12,
            13,
            -51,
            15,
            -49,
            -50,
            14,
            10,
            -54,
            -53,
            11,
            -55,
            9,
            8,
            -56,
            -100,
            92,
            93,
            -99,
            95,
            -97,
            -98,
            94,
            90,
            -102,
            -101,
            91,
            -103,
            89,
            88,
            -104,
            -40,
            24,
            25,
            -39,
            27,
            -37,
            -38,
            26,
            30,
            -34,
            -33,
            31,
            -35,
            29,
            28,
            -36,
            20,
            -44,
            -43,
            21,
            -41,
            23,
            22,
            -42,
            -46,
            18,
            19,
            -45,
            17,
            -47,
            -48,
            16,
            -16,
            48,
            49,
            -15,
            51,
            -13,
            -14,
            50,
            54,
            -10,
            -9,
            55,
            -11,
            53,
            52,
            -12,
            60,
            -4,
            -3,
            61,
            -1,
            63,
            62,
            -2,
            -6,
            58,
            59,
            -5,
            57,
            -7,
            -8,
            56,
            40,
            -24,
            -23,
            41,
            -21,
            43,
            42,
            -22,
            -18,
            46,
            47,
            -17,
            45,
            -19,
            -20,
            44,
            -28,
            36,
            37,
            -27,
            39,
            -25,
            -26,
            38,
            34,
            -30,
            -29,
            35,
            -31,
            33,
            32,
            -32,
            -120,
            72,
            73,
            -119,
            75,
            -117,
            -118,
            74,
            78,
            -114,
            -113,
            79,
            -115,
            77,
            76,
            -116,
            -96,
            96,
            97,
            -95,
            99,
            -93,
            -94,
            98,
            102,
            -90,
            -89,
            103,
            -91,
            101,
            100,
            -92,
            108,
            -84,
            -83,
            109,
            -81,
            111,
            110,
            -82,
            -86,
            106,
            107,
            -85,
            105,
            -87,
            -88,
            104,
            120,
            -72,
            -71,
            121,
            -69,
            123,
            122,
            -70,
            -66,
            126,
            Byte.MAX_VALUE,
            -58,
            6,
            7,
            -57,
            5,
            -76,
            116,
            117,
            -75,
            119,
            -73,
            -74,
            118,
            114,
            -78,
            -77,
            115,
            -79,
            113,
            112,
            -80,
            80,
            -112,
            -109,
            81,
            -109,
            83,
            82,
            -110,
            -106,
            86,
            87,
            -105,
            85,
            -107,
            -108,
            84,
            68,
            -124,
            -123,
            69,
            -121,
            71,
            70,
            -122,
            -126,
            66,
            67,
            -125,
            65,
            -127,
            Byte.MIN_VALUE,
            64
        )

        fun processData(bytes: ByteArray, nanos: Long): HashMap<String, Any>? {
            Log.i(TAG_BT_COMM, "BYTES: $bytes")

            val processedBytes = ByteArray(11)
            if (bytes[1].toInt() == 3) {
                val processingFactors =
                    byteArrayOf(3, 3, 39, -91, 3, 8, 76, 68, 10, -1, 0, 24, 43, 0, -28)
                var start = 0
                var end = 15
                while (start < end) {
                    processingFactors[start] = bytes[start]
                    start++
                    end = 15
                }
                Log.e("BT Data Processing",
                    "BR crcsrc:" + bytesToHex(processingFactors)
                )
                val lda: Int = CRC(
                    32773,
                    USER_MASK,
                    processingFactors,
                    0,
                    15,
                    ref_in = false,
                    ref_out = false,
                    xor_out = 0
                )
                Log.e("BT Data Processing", "BR crc:" + String.format(
                    "%04X--crccal%02X%02X-%02X%02X",
                    Integer.valueOf(lda),
                    java.lang.Byte.valueOf(
                        auchCRCHi[lda shr 8 and 255]
                    ),
                    java.lang.Byte.valueOf(auchCRCLo[lda and 255]),
                    java.lang.Byte.valueOf(
                        bytes[15]
                    ),
                    java.lang.Byte.valueOf(bytes[16])
                )
                )
                if (auchCRCHi[lda shr 8 and 255] != bytes[15] || auchCRCLo[lda and 255] != bytes[16]
                ) return null
                for (i5 in 0..10) {
                    processedBytes[i5] = bytes[i5 + 4]
                }
                Log.e("BT Data Processing", "BR new type")
            } else {
                for (i6 in 0..10) {
                    processedBytes[i6] = bytes[i6]
                }
            }

            val volt = processedBytes[7].toInt()
            var temp = processedBytes[8].toInt()
            val prevTemp = 0
            var psi = processedBytes[9]
                .toInt() shl 8 and MotionEvent.ACTION_POINTER_INDEX_MASK or (processedBytes[10].toInt() and 255)
            if (psi < 148) {
                psi = 146
            }
            val st = processedBytes[6].toInt()
            if (temp <= 40 && abs(temp - prevTemp) <= 6) {
                val tt: Int = (temp + prevTemp) / 2
                temp = tt
            }

            val pressure = (psi / 10.0) - 14.6
            val voltage = volt / 10.0


            Log.i(TAG_BT_COMM, "Temperature: $temp")
            Log.i(TAG_BT_COMM, "Pressure: $pressure")
            Log.i(TAG_BT_COMM, "Voltage: $voltage")
            Log.i(TAG_BT_COMM, "ST: $st")
            Log.i(TAG_BT_COMM, "Nanoseconds: $nanos")

            val data = HashMap<String, Any>()
            data["temperature"] = temp
            data["pressure"] = pressure
            data["voltage"] = voltage
            data["st"] = st
            data["nanos"] = nanos
            return data
        }

        private fun CRC(
            poly: Int,
            init: Int,
            data: ByteArray,
            offset: Int,
            length: Int,
            ref_in: Boolean,
            ref_out: Boolean,
            xor_out: Int,
        ): Int {
            var crc: Int = init

            var i: Int = offset
            while (i < offset + length && i < data.size) {
                val b: Byte = data[i]
                for (j in 0..7) {
                    val k = if (ref_in) 7 - j else j
                    val bit = b.toInt() shr 7 - k and 1 == 1
                    val c15 = crc shr 15 and 1 == 1
                    crc = crc shl 1
                    if (c15 xor bit) {
                        crc = crc xor poly
                    }
                }
                i++
            }

            return if (ref_out) {
                Integer.reverse(crc) ushr 16 xor xor_out
            } else crc xor xor_out and USER_MASK
        }

        private fun bytesToHex(bytes: ByteArray): Any {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 255
                val cArr: CharArray = HEX_ARRAY
                hexChars[j * 2] = cArr[v ushr 4]
                hexChars[j * 2 + 1] = cArr[v and 15]
            }
            return hexChars.toString()
        }

    }

    fun isBluetoothEnabled(): Boolean? {
        return mBluetoothAdapter?.isEnabled
    }

    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )
            when(intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(TAG_BT_COMM,"Bluetooth discovery started")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(TAG_BT_COMM, "Bluetooth discovery finished: ${pairViewModel.getData().size} devices")
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Log.i(TAG_BT_COMM, "Bluetooth state changed")

                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            SensorCommServ.stopService(context)
                        }
                        BluetoothAdapter.STATE_ON -> {
                            SensorCommServ.startService(context)
                        }
                    }

                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    val deviceRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE) // RSSI

                    Log.i("BT Device", "$deviceName): $deviceHardwareAddress")

                    if (device != null) {
                        pairViewModel.addItem(BluetoothDeviceItem(device, deviceRSSI.toInt()))
                    }

                }
            }
        }
    }

    init {
        val listOfBluetoothIntents = listOf(
            BluetoothAdapter.ACTION_STATE_CHANGED,
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
            BluetoothAdapter.ACTION_DISCOVERY_STARTED,
            BluetoothAdapter.ACTION_SCAN_MODE_CHANGED,
            BluetoothDevice.ACTION_FOUND
        )

        listOfBluetoothIntents.forEach {
            context.registerReceiver(receiver, IntentFilter(it))
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning(leScanCallback: ScanCallback) {
        val scanSettings = android.bluetooth.le.ScanSettings.Builder()
            .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner?.startScan(null, scanSettings, leScanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning(leScanCallback: ScanCallback) {
        bluetoothLeScanner?.stopScan(leScanCallback)
    }

    @SuppressLint("MissingPermission")
    fun destroy(leScanCallback: ScanCallback) {
        context.unregisterReceiver(receiver)
        stopScanning(leScanCallback)
        pairViewModel.clearData()
    }

    fun turnBluetoothOn(requestBluetooth: ActivityResultLauncher<Intent>) {
        Log.i(TAG_BT_COMM, "Asking to enable bluetooth")

        if (isBluetoothEnabled() == true) {
            SensorCommServ.startService(context)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }, 3000)
        }
    }
}