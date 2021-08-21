package com.goodapps.alarm

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.activity_record.*
import kotlinx.android.synthetic.main.time_picker.*
import java.io.IOException
import java.util.*

private const val LOG_TAG = "MainActivity"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecordActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var pulseAnimationHandler = Handler()
    private var pulseAnimationRunnable: Runnable? = null

    init {
        pulseAnimationRunnable = Runnable {
            pulse_animation.visibility = View.VISIBLE
            pulse_animation.alpha = 0.5f
            pulse_animation.animate().scaleX(3f).scaleY(3f).alpha(0f)
                .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(2500)
                .withEndAction {
                    pulse_animation.scaleX = 1f
                    pulse_animation.scaleY = 1f
                    pulse_animation.alpha = 0.5f
                    pulseAnimationRunnable?.let {
                        pulseAnimationHandler.postDelayed(it, 3000)
                    }
                }
        }
    }


    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmTime = StorageUtil.getAlarmTime(applicationContext)
        if (alarmTime != StorageUtil.NO_ALARM) {
            goToAlarmScreenAndFinish()
        }
        setContentView(R.layout.activity_record)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, permissions,
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }


        setupViews()
    }

    private fun goToAlarmScreenAndFinish() {
        startActivity(Intent(this@RecordActivity, AlarmActivity::class.java))
        finish()
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun setupViews() {

        record_button.setVideoDuration(20 * 1000)
        record_button.enablePhotoTaking(false)
        record_button.enableVideoRecording(true)
        record_button.actionListener = object : RecordButton.ActionListener {

            override fun onStartRecord() {
                stopPulseAnimation()
                startRecording()

            }

            override fun onEndRecord() {
                stopRecording()
            }

            override fun onSingleTap() {
                stopPulseAnimation()
                Log.e("MY TAG", "CALL the on single tap record ")
            }

            override fun onDurationTooShortError() {
                cancelRecording()
//                startPulseAnimation()
                Log.e("MY TAG", "CALL the on on duration record ")

            }

            override fun onCancelled() {
//                startPulseAnimation()
                Log.e("MY TAG", "CALL the on on cancel record ")
            }
        }
    }

    private fun startPulseAnimation() {
        pulseAnimationRunnable?.let {
            pulseAnimationHandler.post(it)
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimationRunnable?.let {
            pulseAnimationHandler.removeCallbacks(it)
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setOutputFile(
                StorageUtil.getFilename(
                    applicationContext
                )
            )
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000);
            setAudioSamplingRate(44100);
            try {
                prepare()
                try {
                    start()
                } catch (e: IllegalStateException) {
                    release()
                    recorder = null
                    Log.e(LOG_TAG, "start() failed")
                }
            } catch (e: IOException) {
                release()
                recorder = null
                Log.e(LOG_TAG, "prepare() failed")
            }

        }
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                showTimePicker()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recorder?.release()
        recorder = null
    }

    private fun cancelRecording() {
        recorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recorder?.release()
        recorder = null
    }

    private fun showTimePicker() {
        MaterialDialog(this)
            .title(R.string.select_time)
            .cancelable(false)
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

                neutralButton(R.string.play_audio) {
                    if (getActionButton(WhichButton.NEUTRAL).text == getString(R.string.play_audio)) {
                        getActionButton(WhichButton.NEUTRAL).text = getString(R.string.stop_audio)
                        playOrPauseAudioRecord(true)
                    } else {
                        getActionButton(WhichButton.NEUTRAL).text = getString(R.string.play_audio)
                        playOrPauseAudioRecord(false)
                    }
                }
                val c = Calendar.getInstance()
                time_picker.currentHour = c.get(Calendar.HOUR_OF_DAY)
                time_picker.currentMinute = c.get(Calendar.MINUTE)
                setOnDismissListener {
                    playOrPauseAudioRecord(false)
                }
            }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        setupAlarm(hourOfDay, minute)
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
        goToAlarmScreenAndFinish()
    }


    private fun playOrPauseAudioRecord(toPlay: Boolean) {
        if (toPlay) {
            player = MediaPlayer().apply {
                try {
                    setDataSource(StorageUtil.getFilename(applicationContext))
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
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

    override fun onPause() {
        super.onPause()
        stopPulseAnimation()
    }

    override fun onResume() {
        super.onResume()
        startPulseAnimation()
    }
}
