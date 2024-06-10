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
const val NO_DATA = "no_data"
const val LOW = "low"
const val NORMAL = "normal"
const val HIGH = "high"

class WidgetProvider : AppWidgetProvider() {
    private var viewModel : MainViewModel = MainViewModel()

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetIds = intent.getParcelableExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, IntArray::class.java)

            viewModel.refreshData()

            appWidgetIds?.forEach { appWidgetId ->
                // Create an Intent to launch ExampleActivity.
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    /* context = */ context,
                    /* requestCode = */  0,
                    /* intent = */ Intent(context, MainActivity::class.java),
                    /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val frontPressure = viewModel.getFrontPressure().value
                val frontPressureVal = viewModel.getFrontPressure().value.toDoubleOrNull()
                val rearPressure = viewModel.getRearPressure().value
                val rearPressureVal = viewModel.getRearPressure().value.toDoubleOrNull()

                var motorcycleDrawableURI = "@drawable/rear_"
                motorcycleDrawableURI += if (rearPressureVal == null) {
                    NO_DATA
                } else if (rearPressureVal <= SensorCommServ.PRESSURE_LOW) {
                    LOW
                } else if (rearPressureVal <= SensorCommServ.PRESSURE_HIGH) {
                    NORMAL
                } else {
                    HIGH
                }

                motorcycleDrawableURI += "_front_"

                motorcycleDrawableURI += if (frontPressureVal == null) {
                    NO_DATA
                } else if (frontPressureVal <= SensorCommServ.PRESSURE_LOW) {
                    LOW
                } else if (frontPressureVal <= SensorCommServ.PRESSURE_HIGH) {
                    NORMAL
                } else {
                    HIGH
                }

                val imageId = context?.resources?.getIdentifier(motorcycleDrawableURI, null,
                    context.packageName
                )

                Log.i(TAG_WIDGET, "ImageName: $motorcycleDrawableURI")
                Log.i(TAG_WIDGET, "ImageId: $imageId")

                // Get the layout for the widget and attach an on-click listener
                // to the button.
                val views: RemoteViews = RemoteViews(
                    context?.packageName,
                    R.layout.widget_layout
                ).apply {
                    setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
                    setTextViewText(R.id.widget_front_pressure, frontPressure)

                    setViewVisibility(R.id.widget_front_pressure_unit, View.VISIBLE)

                    setTextViewText(R.id.widget_rear_pressure, rearPressure)
                    setViewVisibility(R.id.widget_rear_pressure_unit, View.VISIBLE)

                    setImageViewResource(R.id.widget_front_image, imageId!!)
                }

                // Tell the AppWidgetManager to perform an update on the current
                // widget.
                val appWidgetManager = context?.getSystemService(AppWidgetManager::class.java)
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
