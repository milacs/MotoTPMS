package com.mila.mototpms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import com.mila.mototpms.R.*
import com.mila.mototpms.ui.theme.MotoTPMSTheme

const val TAG_PAIR = "PairDevicesList"

class PairDevicesList : FragmentActivity() {

    private var btComm : BluetoothConnectionManager? = null
    private var pairViewModel = PairDevicesViewModel()
    private var dataProvider : DataProvider? = null

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device
            if ((device.name != null)) {
                pairViewModel.addItem(BluetoothDeviceItem(result))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorPosition = intent.extras?.getString("sensor_position")
        dataProvider = DataProvider(this, getString(string.preference_file_key), Context.MODE_PRIVATE)

        btComm = BluetoothConnectionManager(this)
        btComm!!.startScanning(leScanCallback)

        setContent {
            MotoTPMSTheme {
                ListOfDevices(dataProvider!!, pairViewModel.getData(), sensorPosition)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        btComm?.destroy(leScanCallback)
        pairViewModel.clearData()
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfDevices(
    dataProvider: DataProvider,
    devices: MutableList<BluetoothDeviceItem>,
    sensorPosition: String?
) {

    val context = LocalContext.current as Activity

    val topAppModifier = Modifier
        .height(54.dp)
        .zIndex(1f)
        .shadow(elevation = 4.dp)
        .fillMaxHeight()
        .drawWithContent {
            drawContent()
            clipRect { // Not needed if you do not care about painting half stroke outside
                val strokeWidth = Stroke.DefaultMiter
                val y = size.height // - strokeWidth
                // if the whole line should be inside component
                drawLine(
                    brush = SolidColor(Color.Red),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square,
                    start = Offset.Zero.copy(y = y),
                    end = Offset(x = size.width, y = y)
                )
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    stringResource(if (sensorPosition == "front") string.pair_list_title_front else string.pair_list_title_rear, sensorPosition!!),
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 10.dp))},
                modifier = topAppModifier,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        content = {
            LinearProgressIndicator(modifier = Modifier
                .padding(top = 54.dp)
                .background(MaterialTheme.colorScheme.inversePrimary)
                .fillMaxWidth())
            LazyColumn(
                modifier = Modifier.padding(top = 58.dp)) {
                itemsIndexed(devices) {
                    index, device ->
                    Row(modifier = Modifier
                        .background(color = if (index % 2 == 1) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer)
                        .border(
                            width = 1.dp,
                            color = Color.Transparent,
                            shape = RoundedCornerShape(10f)
                        )
                        .clickable {
                            savePairedDevice(context, dataProvider, sensorPosition, device)
                        }
                        .padding(10.dp)
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier
                            .fillMaxWidth(0.7f)) {
                            Text(text= device.name,
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (index % 2 == 1) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer),
                                modifier = Modifier.fillMaxWidth())
                            Text(text=device.address,
                                style = TextStyle(fontSize = 16.sp, color = if (index % 2 == 1) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer),
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth())
                            Text(text=stringResource(id = string.signalRSSI) +  device.rssi,
                                style = TextStyle(fontSize = 16.sp, color = if (index % 2 == 1) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer),
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth())
                        }

                        Column(modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.inversePrimary,
                                RoundedCornerShape(10.dp)
                            )
                            .background(MaterialTheme.colorScheme.inversePrimary),
                            horizontalAlignment = Alignment.End){
                            val pairedWith = dataProvider.isPaired(device.address)
                            if (pairedWith == "front")
                                Text(stringResource(id = string.front_tyre), modifier = Modifier.padding(10.dp))
                            else if (pairedWith == "rear")
                                Text(stringResource(id = string.rear_tyre), modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }
        }
    )
}

fun savePairedDevice(context: Context, dataProvider: DataProvider, sensorPosition: String?, device: BluetoothDeviceItem) {
    val activity = context as Activity
    val intent = Intent(activity.getString(string.broadcast_update_model))

    val pairButton = { _: DialogInterface, _: Int ->
        dataProvider.savePairedDevice(sensorPosition, device.address, BluetoothConnectionManager.processData(
            device.data!!, device.nanos))
        activity.sendBroadcast(intent)
        activity.finish()
    }

    val alertDialogBuilder = AlertDialog.Builder(activity)
        .setTitle(string.pair_dialog_title)
        .setPositiveButton(string.pair_button, pairButton)

    alertDialogBuilder.show()
}
