package com.mila.mototpms

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews

const val TAG_WIDGET = "WidgetProvider"

class WidgetProvider : AppWidgetProvider() {

    private var viewModel : MainViewModel = MainViewModel()

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            var appWidgetIds = intent.getParcelableExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, IntArray::class.java)

            viewModel?.refreshData()

            appWidgetIds?.forEach { appWidgetId ->
                // Create an Intent to launch ExampleActivity.
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    /* context = */ context,
                    /* requestCode = */  0,
                    /* intent = */ Intent(context, MainActivity::class.java),
                    /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                var frontPressure = viewModel?.getFrontPressure()?.value!!
                var rearPressure = viewModel?.getRearPressure()?.value!!

                var imageId = R.drawable.rear_no_data_front_no_data

                if (rearPressure == "") {
                    if (frontPressure != "") {
                        val frontPressureDouble= frontPressure.toDouble()

                        imageId = if (frontPressureDouble <= SensorCommServ.PRESSURE_LOW) {
                            R.drawable.rear_no_data_front_low
                        } else if (frontPressureDouble <= SensorCommServ.PRESSURE_HIGH) {
                            R.drawable.rear_no_data_front_normal
                        } else {
                            R.drawable.rear_no_data_front_high
                        }

                    }
                }

                if (rearPressure != "") {
                    val rearPressureDouble = rearPressure.toDouble()

                    if (rearPressureDouble <= SensorCommServ.PRESSURE_LOW) {
                        imageId = if (frontPressure != "") {
                            val frontPressureDouble= frontPressure.toDouble()

                            if (frontPressureDouble <= SensorCommServ.PRESSURE_LOW) {
                                R.drawable.rear_low_front_low
                            } else if (frontPressureDouble <= SensorCommServ.PRESSURE_HIGH) {
                                R.drawable.rear_low_front_normal
                            } else {
                                R.drawable.rear_low_front_high
                            }

                        } else {
                            R.drawable.rear_low_front_no_data
                        }

                    } else if (rearPressureDouble <= SensorCommServ.PRESSURE_HIGH) {

                        imageId = if (frontPressure != "") {
                            val frontPressureDouble= frontPressure.toDouble()

                            if (frontPressureDouble <= SensorCommServ.PRESSURE_LOW) {
                                R.drawable.rear_normal_front_low
                            } else if (frontPressureDouble <= SensorCommServ.PRESSURE_HIGH) {
                                R.drawable.rear_normal_front_normal
                            } else {
                                R.drawable.rear_normal_front_high
                            }

                        } else {
                            R.drawable.rear_normal_front_no_data
                        }

                    } else {

                        imageId = if (frontPressure != "") {
                            val frontPressureDouble= frontPressure.toDouble()

                            if (frontPressureDouble <= SensorCommServ.PRESSURE_LOW) {
                                R.drawable.rear_high_front_low
                            } else if (frontPressureDouble <= SensorCommServ.PRESSURE_HIGH) {
                                R.drawable.rear_high_front_normal
                            } else {
                                R.drawable.rear_high_front_high
                            }

                        } else {
                            R.drawable.rear_high_front_no_data
                        }

                    }

                }

                // Get the layout for the widget and attach an on-click listener
                // to the button.
                val views: RemoteViews = RemoteViews(
                    context?.packageName,
                    R.layout.widget_layout
                ).apply {
                    setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
                    setTextViewText(R.id.widget_front_pressure, frontPressure)
//                    setTextViewText(R.id.widget_front_temperature, viewModel?.getFrontTemperature()?.value)
//                    setTextViewText(R.id.widget_front_voltage, viewModel?.getFrontVoltage()?.value)

                    setViewVisibility(R.id.widget_front_pressure_unit, View.VISIBLE)
//                    setViewVisibility(R.id.widget_front_temperature_unit, View.VISIBLE)
//                    setViewVisibility(R.id.widget_front_voltage_unit, View.VISIBLE)

                    setTextViewText(R.id.widget_rear_pressure, rearPressure)
//                    setTextViewText(R.id.widget_rear_temperature, viewModel?.getRearTemperature()?.value)
//                    setTextViewText(R.id.widget_rear_voltage, viewModel?.getRearVoltage()?.value)
                    setViewVisibility(R.id.widget_rear_pressure_unit, View.VISIBLE)
//                    setViewVisibility(R.id.widget_rear_temperature_unit, View.VISIBLE)
//                    setViewVisibility(R.id.widget_rear_voltage_unit, View.VISIBLE)

                    setImageViewResource(R.id.widget_front_image, imageId)
                }

                // Tell the AppWidgetManager to perform an update on the current
                // widget.
                var appWidgetManager = context?.getSystemService<AppWidgetManager>(AppWidgetManager::class.java)
                appWidgetManager?.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        Log.i(TAG_WIDGET, "Widget deleted")
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.i(TAG_WIDGET, "Widget enabled")
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Log.i(TAG_WIDGET, "Widget disabled")
    }
}
