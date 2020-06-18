package com.goodapps.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.goodapps.alarm.receiver.AlarmReceiver

/**
 * Created by Dmitry Bochkov on 18.06.2020.
 */

object AlarmUtil {

    fun scheduleAlarm(appContext: Context, alarmTimeLong: Long) {
        val alarmManager =
            appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(appContext, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(appContext, 2242, intent, FLAG_UPDATE_CURRENT)
        }
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            alarmTimeLong,
            alarmIntent
        )

//        if (Build.VERSION.SDK_INT < 23) {
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
//        } else {
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
//        }
    }
}