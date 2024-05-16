package com.mila.mototpms

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.appwidget.AppWidgetManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mila.mototpms.R.color
import com.mila.mototpms.R.drawable
import com.mila.mototpms.R.id
import com.mila.mototpms.R.layout
import com.mila.mototpms.R.string


private const val CHANNEL_ID = "SensorCommServ"
private const val CHANNEL_ID_EMERGENCY = "SensorCommServEmergency"

private const val TAG_SERVICE = "SensorCommServ"


class SensorCommServ : Service() {
    private var viewModel: MainViewModel? = null
    private var btComm : BluetoothConnectionManager? = null
    private var dataProvider: DataProvider? = null

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device
            val frontAddress = viewModel?.getFrontAddress()?.value
            val rearAddress = viewModel?.getRearAddress()?.value

            if (device.name != null) {
                val address : String? = when (device.address) {
                    frontAddress -> {
                        frontAddress
                    }
                    rearAddress -> {
                        rearAddress
                    }
                    else -> {
                        return
                    }
                }

                Log.i(TAG_SERVICE, "Dev: $result")

                if (result.scanRecord != null) {
                    val processedData = BluetoothConnectionManager.processData(result.scanRecord!!.bytes, result.timestampNanos)

                    dataProvider?.saveValues(address, processedData)
                    viewModel?.refreshData()

                    updateWidget()

                    if (!isInteractive() || !MotoTPMS.isActivityVisible) {

                        val rearPressure = viewModel?.getRearPressure()?.value
                        val frontPressure = viewModel?.getFrontPressure()?.value

                        if ((frontPressure != null && frontPressure != "")) {
                            if (frontPressure.toDouble() >= PRESSURE_HIGH)
                                showEmergencyNotification("frontPressureHigh")
                            if (frontPressure.toDouble() <= PRESSURE_LOW)
                                showEmergencyNotification("frontPressureLow")
                        }
                        if ((rearPressure != null && rearPressure != "")) {
                            if (rearPressure.toDouble() >= PRESSURE_HIGH)
                                showEmergencyNotification("rearPressureHigh")

                            if (rearPressure.toDouble() <= PRESSURE_LOW)
                                showEmergencyNotification("rearPressureLow")
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val PRESSURE_LOW = 28.0
        const val PRESSURE_HIGH = 42.0

        var serviceInstance: SensorCommServ? = null
        fun startService(context: Context) {
            val startIntent = Intent(context, SensorCommServ::class.java)
//            ContextCompat.startForegroundService(context, startIntent)
            context.startForegroundService(startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, SensorCommServ::class.java)
            context.stopService(stopIntent)
        }

    }

    @SuppressLint("MissingPermission")
    private fun showEmergencyNotification(config: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID_EMERGENCY)
            .setSmallIcon(drawable.ic_launcher_foreground_small)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(1000))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val collapsedView = RemoteViews(packageName, layout.notification_collapsed)
        val expandedView = RemoteViews(packageName, layout.notification_expanded)

        collapsedView.setTextViewText(id.pressure_alert_collapsed, getString(string.pressure_warning))

        when(config) {
            "frontPressureHigh" -> {
                builder.color = resources.getColor(color.orange)
                collapsedView.setColor(id.pressure_alert_collapsed, "setTextColor", color.orange)
                expandedView.setColor(id.pressure_alert_expanded, "setTextColor", color.orange)

                expandedView.setTextViewText(id.pressure_alert_expanded, getString(string.pressure_too_high))
                collapsedView.setImageViewResource(id.image_view_collapsed, drawable.front_high)
                expandedView.setImageViewResource(id.image_view_expanded, drawable.front_high)
            }

            "frontPressureLow" -> {
                builder.color = resources.getColor(color.red)
                collapsedView.setColor(id.pressure_alert_collapsed, "setTextColor", color.red)
                expandedView.setColor(id.pressure_alert_expanded, "setTextColor", color.red)

                expandedView.setTextViewText(id.pressure_alert_expanded, getString(string.pressure_too_low))
                collapsedView.setImageViewResource(id.image_view_collapsed, drawable.front_low)
                expandedView.setImageViewResource(id.image_view_expanded, drawable.front_low)
            }

            "rearPressureHigh" -> {
                builder.color = resources.getColor(color.orange)
                collapsedView.setColor(id.pressure_alert_collapsed, "setTextColor", color.orange)
                expandedView.setColor(id.pressure_alert_expanded, "setTextColor", color.orange)
                expandedView.setTextViewText(id.pressure_alert_expanded, getString(string.pressure_too_high))
                collapsedView.setImageViewResource(id.image_view_collapsed, drawable.rear_high)
                expandedView.setImageViewResource(id.image_view_expanded, drawable.rear_high)
            }

            "rearPressureLow" -> {
                builder.color = resources.getColor(color.red)
                collapsedView.setColor(id.pressure_alert_collapsed, "setTextColor", color.red)
                expandedView.setColor(id.pressure_alert_expanded, "setTextColor", color.red)
                expandedView.setTextViewText(id.pressure_alert_expanded, getString(string.pressure_too_low))
                collapsedView.setImageViewResource(id.image_view_collapsed, drawable.rear_low)
                expandedView.setImageViewResource(id.image_view_expanded, drawable.rear_low)
            }
        }


        with(NotificationManagerCompat.from(applicationContext)) {
            builder
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)

            notify(2, builder.build())
        }
    }



    override fun onCreate() {
        super.onCreate()

        serviceInstance = this

        createForegroundNotificationChannel()
        createEmergencyNotificationChannel()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
            }

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(string.foreground_notification_title))
            .setContentText(getText(string.foreground_notification_message))
            .setSmallIcon(drawable.ic_launcher_foreground_small)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification, FOREGROUND_SERVICE_TYPE_LOCATION)

        dataProvider = DataProvider(applicationContext, getString(string.preference_file_key), Context.MODE_PRIVATE)

        viewModel = MainViewModel()
        viewModel?.init()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)

        MotoTPMS.serviceStarted()
        btComm = BluetoothConnectionManager(applicationContext)
        startScanning()

        updateWidget()

        return result
    }

    private fun updateWidget() {

        val frontAddress = viewModel?.getFrontAddress()?.value
        val rearAddress = viewModel?.getRearAddress()?.value

        if (frontAddress == "" && rearAddress == "")
            return

        val intent = Intent(applicationContext, WidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application)
            .getAppWidgetIds(ComponentName(application, WidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, ids)
        sendBroadcast(intent)

        Handler(Looper.getMainLooper()).postDelayed({
            updateWidget()
        }, 60000)
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        btComm?.destroy(leScanCallback)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createForegroundNotificationChannel() {
        val serviceChannel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
        serviceChannel.setName(getString(string.foreground_service_notification_channel))
        val manager = NotificationManagerCompat.from(applicationContext)
        manager.createNotificationChannel(serviceChannel.build())
    }

    private fun createEmergencyNotificationChannel() {
        val serviceChannel = NotificationChannelCompat.Builder(CHANNEL_ID_EMERGENCY, NotificationManagerCompat.IMPORTANCE_MAX)
        serviceChannel.setName(getString(string.emergency_notification_channel))
        val manager = NotificationManagerCompat.from(applicationContext)
        manager.createNotificationChannel(serviceChannel.build())
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        android.bluetooth.le.ScanSettings.Builder()
            .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        btComm!!.startPeriodicScanning(leScanCallback)
    }

    fun isInteractive (): Boolean {
        return (getSystemService(POWER_SERVICE) as PowerManager).isInteractive
    }
}