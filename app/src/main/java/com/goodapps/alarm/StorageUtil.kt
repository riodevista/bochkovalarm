package com.goodapps.alarm

import android.content.Context
import java.util.*

/**
 * Created by Dmitry Bochkov on 17.05.2020.
 */

const val PREFS = "bochkov_alarm_prefs"
const val ALARM_TIME = "alarm_time"

object StorageUtil {


    const val NO_ALARM: Long = -1

    fun getFilename(appContext: Context) = "${appContext.filesDir.absolutePath}/bochkovalarmapp.mp4"

    fun getAlarmTime(appContext: Context): Long {
        var alarmTime = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(
                ALARM_TIME,
                NO_ALARM
            )
        if (Date(alarmTime).before(Date(System.currentTimeMillis()))) {
            alarmTime = NO_ALARM
            saveAlarmTime(appContext, alarmTime)
        }
        return alarmTime
    }

    fun saveAlarmTime(appContext: Context, alarmTimeLong: Long) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(ALARM_TIME, alarmTimeLong).apply()
    }

    public fun removeAlarm(appContext: Context) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(ALARM_TIME).apply()
    }
}