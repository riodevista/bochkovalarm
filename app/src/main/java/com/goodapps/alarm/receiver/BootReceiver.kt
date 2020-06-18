package com.goodapps.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.goodapps.alarm.AlarmUtil
import com.goodapps.alarm.StorageUtil

/**
 * Re-schedules all stored alarms. This is necessary as [AlarmManager] does not persist alarms
 * between reboots.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val alarmTime = StorageUtil.getAlarmTime(context.applicationContext)
            if (alarmTime != StorageUtil.NO_ALARM)
                AlarmUtil.scheduleAlarm(context.applicationContext, alarmTime)
        }
    }
}