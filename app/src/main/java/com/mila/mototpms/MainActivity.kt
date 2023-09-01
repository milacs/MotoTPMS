package com.mila.mototpms

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mila.mototpms.R.*
import com.mila.mototpms.ui.theme.MotoTPMSTheme
import java.text.SimpleDateFormat
import java.util.Date

const val TAG_MAIN = "MainActivity"

class MainActivity : ComponentActivity() {

    private var mBluetoothAdapter : BluetoothAdapter? = null

    private val listOfPermissions = listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val permissionsRequestCode = 25
    private var mainViewModel : MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBluetoothAdapter = (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        MotoTPMS.dataProvider = DataProvider(this, getString(string.preference_file_key), Context.MODE_PRIVATE)

        mainViewModel = MainViewModel()
        mainViewModel!!.init()

        val managePermissions = ManagePermissions(this, listOfPermissions, permissionsRequestCode)
        managePermissions.checkPermissions(callback = fun() {
            val requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.i("Bluetooth", "Enabled result arrived")
                if (result.resultCode == RESULT_OK) {
                    Log.i("Bluetooth", "Enabled")
                    SensorCommServ.startService(this)
                } else {
                    Log.i("Bluetooth", "Not enabled")
                }
            }

            Log.i("Bluetooth", "Asking to enable bluetooth")

            Handler(Looper.getMainLooper()).postDelayed({
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }, 3000)
        })


        setContent {
            MotoTPMSTheme {
                // A surface container using the 'background' color from the theme
                HomeView(mainViewModel!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel?.refreshData()
        MotoTPMS.activityResumed()
    }

    override fun onPause() {
        super.onPause()
        MotoTPMS.activityPaused()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        SensorCommServ.startService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SensorCommServ.stopService(this)
    }
}

fun getResultTimestamp(nanos: String, default: String): String {
    if (nanos == "") return default

    val timestampMilliseconds = System.currentTimeMillis() -
            SystemClock.elapsedRealtime() +
            nanos.toLong() / 1000000

    return SimpleDateFormat("E dd/MM/yy HH:mm:ss").format(Date(timestampMilliseconds))
}

@Composable
fun FrontTyreCard(mainViewModel: MainViewModel) {
    TyreCard(type = "front", mainViewModel )
}

@Composable
fun RearTyreCard(mainViewModel: MainViewModel) {
    TyreCard(type = "rear", mainViewModel )
}

@Composable
fun TyreCard(type : String, mainViewModel : MainViewModel) {
    val context = LocalContext.current
    val pairIntent: Intent = Intent(stringResource(string.start_pair_sensor_activity)).putExtra("sensor_position", type)

    val title = if (type=="front") stringResource(id = string.frontTitle) else stringResource(id= string.rearTitle)
    val boundAddress = remember {

        if (type == "front") {
            mainViewModel.getFrontAddress()
        } else {
            mainViewModel.getRearAddress()
        }
    }

    val temperature = remember {
        if (type == "front") {
            mainViewModel.getFrontTemperature()
        } else {
            mainViewModel.getRearTemperature()
        }
    }

    val pressure = remember {
        if (type == "front") {
            mainViewModel.getFrontPressure()
        } else {
            mainViewModel.getRearPressure()
        }
    }

    val nanos = remember {
        if (type == "front") {
            mainViewModel.getFrontNanos()
        } else {
            mainViewModel.getRearNanos()
        }
    }

    val voltage = remember {
        if (type == "front") {
            mainViewModel.getFrontVoltage()
        } else {
            mainViewModel.getRearVoltage()
        }
    }

    var cardBackgroundColor = MaterialTheme.colorScheme.primaryContainer
    var cartTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    var borderColor = MaterialTheme.colorScheme.primary
    if (pressure.value != "") {
        val p = pressure.value.toDouble()
        if (p <= SensorCommServ.PRESSURE_LOW) {
            cardBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer
            cartTextColor = MaterialTheme.colorScheme.onTertiaryContainer
            borderColor = MaterialTheme.colorScheme.onTertiaryContainer
        } else if (p >= SensorCommServ.PRESSURE_HIGH){
            cardBackgroundColor = MaterialTheme.colorScheme.secondaryContainer
            cartTextColor = MaterialTheme.colorScheme.onSecondaryContainer
            borderColor = MaterialTheme.colorScheme.secondary
        }
    }

    Row(modifier= Modifier
        .padding(10.dp)
        .clip(RoundedCornerShape(60f))
        .border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(60f)
        )
        .background(cardBackgroundColor)
        .clickable { context.startActivity(pairIntent) }
        .padding(15.dp)
        .height(110.dp)) {

        val textStyle = TextStyle(color = cartTextColor,
            fontSize = 17.sp)

        Column(modifier = Modifier
            .align(alignment = Alignment.CenterVertically)
            .fillMaxWidth(0.5f)
            .height(110.dp), verticalArrangement = Arrangement.SpaceEvenly) {

            Text(
                text = title,
                modifier = Modifier.padding(bottom = 10.dp),
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = cartTextColor
                )
            )


            if (boundAddress.value != "") {
                Log.i(TAG_MAIN, "Bonded address [$type]: $boundAddress")
                Row {
                    Text(text = stringResource(id = string.pressure), style = textStyle)

                    if (pressure.value != "") {
                        Text(text = pressure.value, style = textStyle)
                        Text(text = stringResource(id = string.pressure_unit), style = textStyle, modifier = Modifier.padding(start = 2.dp))
                    } else {
                        Text(text = stringResource(id = string.pressure_zero), style = textStyle)
                    }
                }

                Row {
                    Text(text = stringResource(id = string.temperature), style = textStyle)

                    if (temperature.value != "") {
                        Text(text = temperature.value, style = textStyle)
                        Text(text = stringResource(id = string.temperature_unit), style = textStyle)
                    } else {
                        Text(text = stringResource(id = string.loading_info), style = textStyle)
                    }
                }

                Row {
                    Text(text = stringResource(id = string.voltage), style = textStyle)

                    if (voltage.value != "") {
                        Text(text = voltage.value, style = textStyle)
                        Text(text = stringResource(id = string.voltage_unit), style = textStyle, modifier = Modifier.padding(start = 2.dp))
                    } else {
                        Text(text = stringResource(id = string.loading_info), style = textStyle)
                    }
                }
            } else {
                Button(onClick = { context.startActivity(pairIntent) }) {
                    Text(text = stringResource(id = string.pair_button), style = TextStyle(fontSize = 22.sp))
                }
            }
        }

        Column(modifier= Modifier
            .fillMaxWidth()
            .align(alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (type == "front") {
                var imageId = drawable.front_no_data
                if (pressure.value != "") {
                    val p = pressure.value.toDouble()
                    imageId = if (p <= SensorCommServ.PRESSURE_LOW) {
                        drawable.front_low

                    } else if (p >= SensorCommServ.PRESSURE_HIGH){
                        drawable.front_high
                    } else {
                        drawable.front_normal
                    }
                }

                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .background(Color.Transparent)
                        .padding(start = 25.dp))
                
            } else {
                var imageId = drawable.rear_no_data
                if (pressure.value != "") {
                    val p = pressure.value.toDouble()
                    imageId = if (p <= SensorCommServ.PRESSURE_LOW) {
                        drawable.rear_low
                    } else if (p >= SensorCommServ.PRESSURE_HIGH){
                        drawable.rear_high
                    } else {
                        drawable.rear_normal
                    }
                }
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .background(Color.Transparent)
                        .padding(start = 25.dp))
            }
            Text(text = getResultTimestamp(nanos.value, stringResource(id = string.not_synced_yet)),
                style = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp),
                modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(mainViewModel: MainViewModel) {

    val isServiceRunning = remember {
        MotoTPMS.isServiceRunning
    }

    MotoTPMSTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        stringResource(id = string.app_name),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 10.dp)
                            .zIndex(0f)
                    )
                },
                    modifier = Modifier
                        .height(54.dp)
                        .zIndex(1f)
                        .shadow(elevation = 4.dp)
                        .fillMaxSize()
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
                        },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        scrolledContainerColor = MaterialTheme.colorScheme.primary,
                    ),
                    actions = {
                        IconButton(
                            onClick = { eraseData(mainViewModel) },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(Icons.Filled.Delete, null)
                        }
                        IconButton(
                            onClick = { swapSensors(mainViewModel) },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(painterResource(id = drawable.swap_icon), null)
                        }
                    }
                )
            },
            content = {
                if (!isServiceRunning.value) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 54.dp)
                            .background(MaterialTheme.colorScheme.inversePrimary)
                            .fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp)
                ) {
                        FrontTyreCard(mainViewModel)
                        RearTyreCard(mainViewModel)
                }
            }
        )
    }
}

fun swapSensors(viewModel: MainViewModel) {
    viewModel.swapSensors()
}

fun eraseData(viewModel: MainViewModel) {
    viewModel.clearData()   
}
