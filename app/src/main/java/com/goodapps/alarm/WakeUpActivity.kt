package com.goodapps.alarm

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.goodapps.alarm.receiver.WakeLocker
import java.io.IOException


private const val LOG_TAG = "WakeUpActivity"

@SuppressLint("Registered")
class WakeUpActivity : AppCompatActivity() {

    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(R.layout.activity_wake_up)
        StorageUtil.removeAlarm(this.applicationContext)
        startPlaying()
        WakeLocker.release()

    }

    fun finishAlarm(view: View) {
        finish()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(StorageUtil.getFilename(applicationContext))
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                isLooping = true
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    override fun onDestroy() {
        StorageUtil.removeAlarm(this.applicationContext)
        super.onDestroy()
    }
}
