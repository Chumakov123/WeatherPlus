package com.chumakov123.weatherplus.presentation.widget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootReceiver", "Устройство перезагружено, перезапускаем AlarmManager")
            WeatherAlarmScheduler.scheduleNext(context)
        }
    }
}
