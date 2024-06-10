package com.mila.mototpms

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import com.mila.mototpms.R.color
import com.mila.mototpms.R.drawable
import com.mila.mototpms.R.string
import com.mila.mototpms.ui.theme.MotoTPMSTheme


const val TAG_MAIN = "MainActivity"

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private var mBtComm : BluetoothConnectionManager? = null

    private val listOfPermissions = listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION
    )

    private val permissionsRequestCode = 25
    private var mainViewModel : MainViewModel? = null

    private val requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i("Bluetooth", "Enabled result arrived")
        if (result.resultCode == RESULT_OK) {
            Log.i("Bluetooth", "Enabled")
        } else {
            Log.i("Bluetooth", "Not enabled")
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBtComm = BluetoothConnectionManager(this)
        MotoTPMS.dataProvider = DataProvider(this, getString(string.preference_file_key), Context.MODE_PRIVATE)

        mainViewModel = MainViewModel()
        mainViewModel!!.init()

        val managePermissions = ManagePermissions(this, listOfPermissions, permissionsRequestCode)
        managePermissions.checkPermissions(callback = fun() {
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.i("Bluetooth", "Enabled result arrived")
                if (result.resultCode == RESULT_OK) {
                    Log.i("Bluetooth", "Enabled")
                } else {
                    Log.i("Bluetooth", "Not enabled")
                }
            }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            mBtComm!!.turnBluetoothOn(requestBluetooth)
        }
    }

    override fun onPause() {
        super.onPause()
        MotoTPMS.activityPaused()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            SensorCommServ.startService(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SensorCommServ.stopService(this)
    }
}

@Composable
fun FrontTyreCard(mainViewModel: MainViewModel) {
    TyreCard(tyre = "front", mainViewModel)
}

@Composable
fun RearTyreCard(mainViewModel: MainViewModel) {
    TyreCard(tyre = "rear", mainViewModel)
}

@Composable
fun TyreCard(tyre : String, mainViewModel : MainViewModel) {
    val context = LocalContext.current
    val pairIntent: Intent = Intent(stringResource(string.start_pair_sensor_activity)).setPackage(context.packageName).putExtra("sensor_position", tyre)

    val title = if (tyre=="front") stringResource(id = string.frontTitle) else stringResource(id= string.rearTitle)
    val boundAddress = remember {

        if (tyre == "front") {
            mainViewModel.getFrontAddress()
        } else {
            mainViewModel.getRearAddress()
        }
    }

    val temperature = remember {
        if (tyre == "front") {
            mainViewModel.getFrontTemperature()
        } else {
            mainViewModel.getRearTemperature()
        }
    }

    val pressure = remember {
        if (tyre == "front") {
            mainViewModel.getFrontPressure()
        } else {
            mainViewModel.getRearPressure()
        }
    }

    val nanos = remember {
        if (tyre == "front") {
            mainViewModel.getFrontNanos()
        } else {
            mainViewModel.getRearNanos()
        }
    }

    val voltage = remember {
        if (tyre == "front") {
            mainViewModel.getFrontVoltage()
        } else {
            mainViewModel.getRearVoltage()
        }
    }

    var cardBackgroundColor = MaterialTheme.colorScheme.primaryContainer

    var borderColor = MaterialTheme.colorScheme.primary
    if (pressure.value != "") {
        val p = pressure.value.toDouble()
        if (p <= SensorCommServ.PRESSURE_LOW) {
            cardBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer
            borderColor = MaterialTheme.colorScheme.onTertiaryContainer
        } else if (p >= SensorCommServ.PRESSURE_HIGH){
            cardBackgroundColor = MaterialTheme.colorScheme.secondaryContainer
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
        .height(110.dp),
        verticalAlignment = Alignment.CenterVertically) {

        TyreCardContent(context, boundAddress, tyre, pressure, temperature, voltage, nanos, title, pairIntent)
    }
}

@Composable
fun TyreCardContent(context: Context,
                    boundAddress: MutableState<String>,
                    tyre: String,
                    pressure: MutableState<String>,
                    temperature: MutableState<String>,
                    voltage: MutableState<String>,
                    nanos: MutableState<String>,
                    title: String,
                    pairIntent: Intent) {

    var cardTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    if (pressure.value != "") {
        val p = pressure.value.toDouble()
        if (p <= SensorCommServ.PRESSURE_LOW) {
            cardTextColor = MaterialTheme.colorScheme.onTertiaryContainer
        } else if (p >= SensorCommServ.PRESSURE_HIGH){
            cardTextColor = MaterialTheme.colorScheme.onSecondaryContainer
        }
    }

    val textStyle = TextStyle(color = cardTextColor,
        fontSize = 17.sp)

    Column(modifier = Modifier
        .fillMaxWidth(0.5f)
        .height(110.dp), verticalArrangement = Arrangement.SpaceBetween) {

        Column {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = cardTextColor
                )
            )

            Text(
                text = boundAddress.value,
                modifier = Modifier.padding(top=5.dp),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    color = cardTextColor
                )
            )
        }


        Column {
            if (boundAddress.value != "") {
                Log.i(TAG_MAIN, "Bound address [$tyre]: $boundAddress")

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
    }

    Column(modifier= Modifier
        .fillMaxWidth(),
        horizontalAlignment = Alignment.End) {
        if (tyre == "front") {
            var pressureTextColor = cardTextColor
            if (pressure.value != "") {
                val p = pressure.value.toDouble()

                pressureTextColor = if (p <= SensorCommServ.PRESSURE_LOW) {
                    colorResource(id = color.red)
                } else if (p >= SensorCommServ.PRESSURE_HIGH) {
                    colorResource(id = color.orange)
                } else {
                    colorResource(id = color.green)
                }
            }

            val bigTextStyle = TextStyle(color = pressureTextColor,
                fontSize = 68.sp,
                fontFamily = FontFamily.Monospace)

            if (boundAddress.value != "") {
                Row {
                    if (pressure.value != "") {
                        Text(text = pressure.value, style = bigTextStyle)
                        Text(text = stringResource(id = string.pressure_unit), style = textStyle, modifier = Modifier.padding(start = 2.dp))
                    } else {
                        Text(text = stringResource(id = string.pressure_zero), style = textStyle)
                    }
                }
            }
        } else {
            var pressureTextColor = cardTextColor
            if (pressure.value != "") {
                val p = pressure.value.toDouble()

                pressureTextColor = if (p <= SensorCommServ.PRESSURE_LOW) {
                    colorResource(id = color.red)
                } else if (p >= SensorCommServ.PRESSURE_HIGH) {
                    colorResource(id = color.orange)
                } else {
                    colorResource(id = color.green)
                }
            }

            val bigTextStyle = TextStyle(color = pressureTextColor,
                fontSize = 68.sp,
                fontFamily = FontFamily.Monospace)

            if (boundAddress.value != "") {
                Row {
                    if (pressure.value != "") {
                        Text(text = pressure.value, style = bigTextStyle)
                        Text(text = stringResource(id = string.pressure_unit), style = textStyle, modifier = Modifier.padding(start = 2.dp))
                    } else {
                        Text(text = stringResource(id = string.pressure_zero), style = textStyle)
                    }
                }
            }
        }

        Text(text = nanos.value,
            style = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp),
            modifier = Modifier.padding(end = 32.dp))
    }
}

@Preview
@Composable
fun HomeViewPreview() {
    val mvm = MainViewModel()
    mvm.initPreview()
    HomeView(mainViewModel = mvm)
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
                            Icon(Icons.Filled.Delete, stringResource(id = string.delete_all_addresses))
                        }
                        IconButton(
                            onClick = { swapSensors(mainViewModel) },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(painterResource(id = drawable.swap_icon), stringResource(id = string.swap_sensors))
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
