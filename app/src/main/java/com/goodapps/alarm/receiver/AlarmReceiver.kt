package com.goodapps.alarm.receiver

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.goodapps.alarm.StorageUtil
import com.goodapps.alarm.WakeUpActivity

/**
 * Created by Dmitry Bochkov on 09.04.2017.
 */

class AlarmReceiver : WakefulBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("RECEIVER", "AlarmReceiver ok")
        WakeLocker.acquire(context.applicationContext)
        val newActivityIntent = Intent(context, WakeUpActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(newActivityIntent)
        StorageUtil.removeAlarm(context)
    }
}