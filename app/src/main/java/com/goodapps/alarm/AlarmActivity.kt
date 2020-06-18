package com.goodapps.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.goodapps.alarm.receiver.AlarmReceiver
import com.goodapps.alarm.utils.PluralMergeUtil
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.time_picker.*
import java.io.IOException
import java.util.*


private const val LOG_TAG = "AlarmActivity"

class AlarmActivity : AppCompatActivity() {

    private lateinit var broadcastReceiver: BroadcastReceiver
    private var player: MediaPlayer? = null
    private var alarmTime: Long = StorageUtil.NO_ALARM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        alarmTime = StorageUtil.getAlarmTime(applicationContext)
        checkAndUpdateAlarmTimeUI(alarmTime)

        setupViews()
    }

    override fun onStart() {
        super.onStart()
        checkAndUpdateAlarmTimeUI(StorageUtil.getAlarmTime(applicationContext))
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action!!.compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    updateAlarmInNMinutes(StorageUtil.getAlarmTime(applicationContext))
                }
            }
        }

        registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    private fun checkAndUpdateAlarmTimeUI(alarmTime: Long) {
        if (alarmTime == StorageUtil.NO_ALARM) {
            goToRecordScreenAndFinish()
        } else {
            updateAlarmTimeView(alarmTime)
            updateAlarmInNMinutes(alarmTime)
        }
    }

    private fun goToRecordScreenAndFinish() {
        startActivity(Intent(this, RecordActivity::class.java))
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        player?.release()
        player = null
    }


    private fun setupViews() {
        slide_to_cancel.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                StorageUtil.removeAlarm(applicationContext)
                cancelAlarm()
                goToRecordScreenAndFinish()
            }
        }

        play_pause_button.change(true, false)
        play_pause_button.setOnClickListener {
            play_pause_button.toggle()
            playOrPauseAudioRecord(!play_pause_button.isPlay)
        }

        alarm_time.setOnClickListener { showTimePicker() }
    }

    private fun updateAlarmTimeView(alarmTimeLong: Long) {
        val date = Date(alarmTimeLong)
        alarm_time.text =
            "${String.format("%02d", date.hours)}:${String.format("%02d", date.minutes)}"
    }

    private fun updateAlarmInNMinutes(alarmTimeLong: Long) {
        var diff: Long =
            alarmTimeLong + 60000 - System.currentTimeMillis() //+1 minutes to don't show 0 minutes left
        val hours = diff / (1000 * 60 * 60)
        diff -= hours * 1000 * 60 * 60
        val minutes = diff / (1000 * 60)

        if (hours == 0L && minutes == 1L)
            alarm_time_in_n_minutes.text = "Осталась "
        else if (hours == 1L)
            alarm_time_in_n_minutes.text = "Остался "
        else
            alarm_time_in_n_minutes.text = "Осталось "

        if (hours > 0)
            alarm_time_in_n_minutes.text = alarm_time_in_n_minutes.text.toString() +
                    "${PluralMergeUtil.choosePluralMerge(hours, "час", "часа", "часов")}"
        if (minutes > 0) {
            if (hours > 0)
                alarm_time_in_n_minutes.text = alarm_time_in_n_minutes.text.toString() + " "
            alarm_time_in_n_minutes.text = alarm_time_in_n_minutes.text.toString() +
                    "${PluralMergeUtil.choosePluralMerge(minutes, "минута", "минуты", "минут")}"
        } else if (hours == 0L && minutes == 0L) {
            alarm_time_in_n_minutes.text = alarm_time_in_n_minutes.text.toString() + "меньше минуты"
        }
    }


    private fun cancelAlarm() {
        val alarmManager =
            this.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this.applicationContext, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this.applicationContext, 2242, intent, FLAG_UPDATE_CURRENT)
        }
        alarmManager.cancel(alarmIntent)
    }

    private fun playOrPauseAudioRecord(toPlay: Boolean) {
        if (toPlay) {
            player = MediaPlayer().apply {
                try {
                    setDataSource(StorageUtil.getFilename(applicationContext))
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setAudioAttributes(audioAttributes)
                    isLooping = true
                    prepare()
                    start()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "prepare() failed")
                }
            }
        } else {
            player?.release()
            player = null
        }
    }

    private fun showTimePicker() {
        MaterialDialog(this)
            .title(R.string.change_time)
            .cancelable(true)
            .noAutoDismiss()
            .show {
                negativeButton(R.string.cancel) {
//                    MaterialDialog(this@RecordActivity)
//                        .title(R.string.cancel_alarm_scheduling)
//                        .message(R.string.cancel_alarm_scheduling_message)
//                        .positiveButton(R.string.yes, click = {
//                            outerDialog.dismiss()
//                        })
//                        .negativeButton(R.string.no, click = {
//                            it.dismiss()
//                        }).show()
                    dismiss()
                }

                positiveButton(R.string.ok) {
                    setupAlarm(time_picker.currentHour, time_picker.currentMinute)
                    dismiss()
                }
                customView(R.layout.time_picker)
                time_picker.setIs24HourView(true)

                val date = Date(alarmTime)
                time_picker.currentHour = date.hours
                time_picker.currentMinute = date.minutes
            }
    }

    private fun setupAlarm(hourOfDay: Int, minute: Int) {
        val now = Calendar.getInstance()
        val alarmTimeToday = Calendar.getInstance()
        val alarmTimeTomorrow = Calendar.getInstance()

        alarmTimeToday.set(Calendar.HOUR_OF_DAY, hourOfDay)
        alarmTimeToday.set(Calendar.MINUTE, minute)
        alarmTimeToday.set(Calendar.SECOND, 0)
        alarmTimeToday.set(Calendar.MILLISECOND, 0)

        alarmTimeTomorrow.set(Calendar.HOUR_OF_DAY, hourOfDay)
        alarmTimeTomorrow.set(Calendar.MINUTE, minute)
        alarmTimeTomorrow.add(Calendar.DATE, 1)
        alarmTimeTomorrow.set(Calendar.SECOND, 0)
        alarmTimeTomorrow.set(Calendar.MILLISECOND, 0)

        val alarmTimeFinal = if (alarmTimeToday.after(now))
            alarmTimeToday.timeInMillis
        else
            alarmTimeTomorrow.timeInMillis

        AlarmUtil.scheduleAlarm(applicationContext, alarmTimeFinal)

        StorageUtil.saveAlarmTime(
            this.applicationContext,
            alarmTimeFinal
        )
        updateAlarmTimeView(alarmTimeFinal)
        updateAlarmInNMinutes(alarmTimeFinal)
    }

}
