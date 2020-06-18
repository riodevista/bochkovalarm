//package com.goodapps.alarm
//
//import android.Manifest
//import android.animation.Animator
//import android.animation.AnimatorListenerAdapter
//import android.annotation.SuppressLint
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.app.TimePickerDialog
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.media.AudioAttributes
//import android.media.MediaPlayer
//import android.media.MediaRecorder
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.TimePicker
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.app.AlarmManagerCompat
//import com.afollestad.materialdialogs.MaterialDialog
//import com.afollestad.materialdialogs.customview.customView
//import com.goodapps.alarm.receiver.AlarmReceiver
//import com.jackandphantom.instagramvideobutton.InstagramVideoButton
//import com.ncorti.slidetoact.SlideToActView
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.time_picker.*
//import java.io.IOException
//import java.lang.IllegalStateException
//import java.util.*
//
//private const val LOG_TAG = "MainActivity"
//private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
//
//@SuppressLint("Registered")
//class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
//
//    private var recorder: MediaRecorder? = null
//    private var player: MediaPlayer? = null
//
//
//    // Requesting permission to RECORD_AUDIO
//    private var permissionToRecordAccepted = false
//    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
//    private var shortAnimationDuration: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        ActivityCompat.requestPermissions(
//            this, permissions,
//            REQUEST_RECORD_AUDIO_PERMISSION
//        )
//
//        setupViews()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        updateViewState(StorageUtil.getAlarmTime(applicationContext))
//    }
//
//    override fun onStop() {
//        super.onStop()
//        recorder?.release()
//        recorder = null
//        player?.release()
//        player = null
//    }
//
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
//            grantResults[0] == PackageManager.PERMISSION_GRANTED
//        } else {
//            false
//        }
//        if (!permissionToRecordAccepted) finish()
//    }
//
//    private fun setupViews() {
//
//        record_button.setMinimumVideoDuration(100L)
//        record_button.setVideoDuration(20 * 1000)
//        record_button.enablePhotoTaking(false)
//        record_button.enableVideoRecording(true)
//        record_button.actionListener = object : RecordButton.ActionListener {
//            override fun onStartRecord() {
//                startRecording()
//
//            }
//
//            override fun onEndRecord() {
//                stopRecording()
//            }
//
//            override fun onSingleTap() {
//                Log.e("MY TAG", "CALL the on single tap record ")
//            }
//
//            override fun onDurationTooShortError() {
//                Log.e("MY TAG", "CALL the on on duration record ")
//
//            }
//
//            override fun onCancelled() {
//                Log.e("MY TAG", "CALL the on on cancel record ")
//            }
//        }
//
//        slide_to_cancel.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
//            override fun onSlideComplete(view: SlideToActView) {
//                StorageUtil.removeAlarm(applicationContext)
//                cancelAlarm()
//            }
//        }
//
//        play_pause_button.change(true, false)
//        play_pause_button.setOnClickListener {
//            play_pause_button.toggle()
//            playOrPauseAudioRecord(!play_pause_button.isPlay)
//        }
//
//        // Retrieve and cache the system's default "short" animation time.
//        shortAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
//
//        updateViewState(StorageUtil.getAlarmTime(this.applicationContext))
//    }
//
//    private fun updateViewState(alarmTimeLong: Long, withAnim: Boolean = false) {
//        if (alarmTimeLong != StorageUtil.NO_ALARM) {
//            if (withAnim) {
//                goToAlarmState()
//            } else {
//                record_button.visibility = View.INVISIBLE
//                play_pause_button.visibility = View.VISIBLE
//                info_text.visibility = View.INVISIBLE
//                slide_to_cancel.visibility = View.VISIBLE
//                alarm_time_container.visibility = View.VISIBLE
//            }
//
//            slide_to_cancel.resetSlider()
//            updateAlarmTimeView(alarmTimeLong)
//        } else {
//            if (withAnim) {
//                goToRecordState()
//            } else {
//                record_button.visibility = View.VISIBLE
//                play_pause_button.visibility = View.INVISIBLE
//                info_text.visibility = View.VISIBLE
//                slide_to_cancel.visibility = View.INVISIBLE
//                alarm_time_container.visibility = View.INVISIBLE
//            }
//        }
//    }
//
//    private fun goToRecordState() {
//        play_pause_button.animate()
//            .alpha(0f)
//            .setDuration(shortAnimationDuration.toLong())
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    play_pause_button.visibility = View.INVISIBLE
//                    record_button.apply {
//                        alpha = 0f
//                        visibility = View.VISIBLE
//
//                        animate()
//                            .alpha(1f)
//                            .setDuration(shortAnimationDuration.toLong())
//                            .setListener(null)
//                    }
//                    info_text.apply {
//                        alpha = 0f
//                        visibility = View.VISIBLE
//
//                        animate()
//                            .alpha(1f)
//                            .setDuration(shortAnimationDuration.toLong())
//                            .setListener(null)
//                    }
//                }
//            })
//        alarm_time_container.animate()
//            .alpha(0f)
//            .setDuration(shortAnimationDuration.toLong())
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    play_pause_button.visibility = View.INVISIBLE
//                }
//            })
//        slide_to_cancel.animate()
//            .alpha(0f)
//            .setDuration(shortAnimationDuration.toLong())
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    play_pause_button.visibility = View.INVISIBLE
//                }
//            })
//    }
//
//    private fun goToAlarmState() {
//        alarm_time_container.apply {
//            alpha = 0f
//            visibility = View.VISIBLE
//
//            animate()
//                .alpha(1f)
//                .setDuration(shortAnimationDuration.toLong())
//                .setListener(null)
//        }
//
//        slide_to_cancel.apply {
//            alpha = 0f
//            visibility = View.VISIBLE
//
//            animate()
//                .alpha(1f)
//                .setDuration(shortAnimationDuration.toLong())
//                .setListener(null)
//        }
//
//        record_button.animate()
//            .alpha(0f)
//            .setDuration(shortAnimationDuration.toLong())
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    record_button.visibility = View.INVISIBLE
//                    play_pause_button.apply {
//                        alpha = 0f
//                        visibility = View.VISIBLE
//
//                        animate()
//                            .alpha(1f)
//                            .setDuration(shortAnimationDuration.toLong())
//                            .setListener(null)
//                    }
//                }
//            })
//        info_text.animate()
//            .alpha(0f)
//            .setDuration(shortAnimationDuration.toLong())
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    info_text.visibility = View.INVISIBLE
//                }
//            })
//    }
//
//
//
//    private fun updateAlarmTimeView(alarmTimeLong: Long) {
//        val date = Date(alarmTimeLong)
//        alarm_time.text =
//            "${String.format("%02d", date.hours)}:${String.format("%02d", date.minutes)}"
//    }
//
//    private fun startRecording() {
//        recorder = MediaRecorder().apply {
//            setOutputFile(
//                StorageUtil.getFilename(
//                    applicationContext
//                )
//            )
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setAudioEncodingBitRate(128000);
//            setAudioSamplingRate(44100);
//            try {
//                prepare()
//            } catch (e: IOException) {
//                Log.e(LOG_TAG, "prepare() failed")
//            }
//            try {
//                start()
//            } catch (e: IllegalStateException) {
//                release()
//                recorder = null
//                Log.e(LOG_TAG, "start() failed")
//            }
//        }
//    }
//
//    private fun stopRecording() {
//        recorder?.apply {
//            try {
//                stop()
//                release()
//                showTimePicker()
//            } catch (e: IllegalStateException) {
//                e.printStackTrace()
//            }
//        }
//        recorder = null
//    }
//
//    private fun showTimePicker() {
//        MaterialDialog(this)
//            .title(R.string.select_time)
//            .cancelable(false)
//            .negativeButton(R.string.cancel, click = { outerDialog ->
//                MaterialDialog(this)
//                    .title(R.string.cancel_alarm_scheduling)
//                    .message(R.string.cancel_alarm_scheduling_message)
//                    .positiveButton(R.string.yes, click = {
//                        outerDialog.dismiss()
//                    })
//                    .negativeButton(R.string.no, click = {
//                        it.dismiss()
//                    })
//            })
//            .positiveButton(R.string.ok)
//            .show {
//                customView(R.layout.time_picker)
//                time_picker.setIs24HourView(true)
//                val c = Calendar.getInstance()
//                time_picker.currentHour = c.get(Calendar.HOUR_OF_DAY)
//                time_picker.currentMinute = c.get(Calendar.MINUTE)
//                positiveButton { setupAlarm(time_picker.currentHour, time_picker.currentMinute) }
//            }
//    }
//
//    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
//        setupAlarm(hourOfDay, minute)
//    }
//
//
//    private fun setupAlarm(hourOfDay: Int, minute: Int) {
//        val now = Calendar.getInstance()
//        val alarmTimeToday = Calendar.getInstance()
//        val alarmTimeTomorrow = Calendar.getInstance()
//
//        alarmTimeToday.set(Calendar.HOUR_OF_DAY, hourOfDay)
//        alarmTimeToday.set(Calendar.MINUTE, minute)
//        alarmTimeToday.set(Calendar.SECOND, 0)
//        alarmTimeToday.set(Calendar.MILLISECOND, 0)
//
//        alarmTimeTomorrow.set(Calendar.HOUR_OF_DAY, hourOfDay)
//        alarmTimeTomorrow.set(Calendar.MINUTE, minute)
//        alarmTimeTomorrow.add(Calendar.DATE, 1)
//        alarmTimeTomorrow.set(Calendar.SECOND, 0)
//        alarmTimeTomorrow.set(Calendar.MILLISECOND, 0)
//
//        val alarmTimeFinal = if (alarmTimeToday.after(now))
//            alarmTimeToday.timeInMillis
//        else
//            alarmTimeTomorrow.timeInMillis
//
//        scheduleAlarm(alarmTimeFinal)
//
//        StorageUtil.saveAlarmTime(
//            this.applicationContext,
//            alarmTimeFinal
//        )
//        updateViewState(alarmTimeFinal, true)
//    }
//
//
//    private fun scheduleAlarm(alarmTimeLong: Long) {
//        val alarmManager =
//            this.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val alarmIntent = Intent(this.applicationContext, AlarmReceiver::class.java).let { intent ->
//            PendingIntent.getBroadcast(this.applicationContext, 0, intent, 0)
//        }
//        AlarmManagerCompat.setExactAndAllowWhileIdle(
//            alarmManager,
//            AlarmManager.RTC_WAKEUP,
//            alarmTimeLong,
//            alarmIntent
//        )
//    }
//
//    private fun cancelAlarm() {
//        val alarmManager =
//            this.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val alarmIntent = Intent(this.applicationContext, AlarmReceiver::class.java).let { intent ->
//            PendingIntent.getBroadcast(this.applicationContext, 0, intent, 0)
//        }
//        alarmManager.cancel(alarmIntent)
//        updateViewState(StorageUtil.NO_ALARM, true)
//    }
//
//    private fun playOrPauseAudioRecord(toPlay: Boolean) {
//        if (toPlay) {
//            player = MediaPlayer().apply {
//                try {
//                    setDataSource(StorageUtil.getFilename(applicationContext))
//                    val audioAttributes = AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ALARM)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                        .build()
//                    setAudioAttributes(audioAttributes)
//                    isLooping = true
//                    prepare()
//                    start()
//                } catch (e: IOException) {
//                    Log.e(LOG_TAG, "prepare() failed")
//                }
//            }
//        } else {
//            player?.release()
//            player = null
//        }
//    }
//
//    fun startSettings(view: View) {}
//
//}
