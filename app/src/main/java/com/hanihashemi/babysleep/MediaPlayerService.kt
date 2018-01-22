package com.hanihashemi.babysleep

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.hanihashemi.babysleep.model.Music
import timber.log.Timber

/**
 * Created by irantalent on 1/4/18.
 */
class MediaPlayerService : Service(), MediaPlayer.OnErrorListener {
    private var player: PerfectLoopMediaPlayer? = null
    private var music: Music? = null
    private var lastStatus = STATUS.STOP
    private var sleepTimerMillis = 0L
    private var countDownTimer: CountDownTimer? = null

    enum class ACTIONS {
        PLAY, PAUSE, STOP, SYNC, SET_SLEEP_TIMER
    }

    enum class STATUS {
        PLAYING, PAUSE, STOP, NONE
    }

    enum class ARGUMENTS {
        ACTION, MUSIC_OBJ, SLEEP_TIMER_MILLIS
    }

    companion object {
        const val BROADCAST_KEY = "message_from_mars"
        const val BROADCAST_ARG_STATUS = "status"
        const val BROADCAST_ARG_MUSIC = "music_track"
        const val BROADCAST_ARG_MILLIS_UNTIL_FINISHED = "CURRENT_MILLIS"
        const val ONGOING_NOTIFICATION_ID = 221
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.getSerializableExtra(ARGUMENTS.ACTION.name)

            when ((action as ACTIONS)) {
                ACTIONS.PLAY -> {
                    val receivedMusic = intent.getParcelableExtra<Music>(ARGUMENTS.MUSIC_OBJ.name)
                    play(receivedMusic ?: this.music)
                }
                ACTIONS.PAUSE -> pause()
                ACTIONS.STOP -> stop()
                ACTIONS.SYNC -> sync(lastStatus)
                ACTIONS.SET_SLEEP_TIMER -> {
                    sleepTimerMillis = intent.getLongExtra(ARGUMENTS.SLEEP_TIMER_MILLIS.name, 0)
                    stopTimer()
                    if (player?.isPlaying!!)
                        startTimer()
                }
            }
        }

        return START_STICKY
    }

    private fun startTimer() {
        if (sleepTimerMillis <= 2000L) {
            sleepTimerMillis = 0
            return
        }
        this.countDownTimer = object : CountDownTimer(sleepTimerMillis, 1000) {
            override fun onFinish() {
                stop()
            }

            override fun onTick(millisUntilFinished: Long) {
                sleepTimerMillis = millisUntilFinished
                sync(STATUS.NONE, millisUntilFinished + 1)
            }
        }
        this.countDownTimer?.start()
    }

    private fun stopTimer() {
        this.countDownTimer?.cancel()
    }

    private fun sync(status: STATUS, millisUntilFinished: Long = 0) {
        lastStatus = status
        val intent = Intent(BROADCAST_KEY)
        intent.putExtra(BROADCAST_ARG_STATUS, lastStatus)
        intent.putExtra(BROADCAST_ARG_MUSIC, music)
        intent.putExtra(BROADCAST_ARG_MILLIS_UNTIL_FINISHED, millisUntilFinished)
        LocalBroadcastManager.getInstance(this as Context).sendBroadcast(intent)
    }

    private fun stop() {
        sync(STATUS.STOP)
        player?.stop()
        stopTimer()
        stopForeground(true)
    }

    private fun pause() {
        sync(STATUS.PAUSE)
        stopTimer()
        player?.pause()
        stopForeground(true)
    }

    private fun play(music: Music?) {
        if (music == null)
            return
        if (player != null && player!!.isPlaying) {
            player?.stop()
            player?.release()
        }

        this.music = music
        startTimer()
        player = PerfectLoopMediaPlayer.create(this, music.fileId)
        showNotification("در حال پخش")
        sync(STATUS.PLAYING)
    }

    private fun showNotification(title: String) = startForeground(ONGOING_NOTIFICATION_ID, NotificationManager(this as Context).mediaPlayerServiceNotification(title))

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        sync(STATUS.STOP)

        when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Timber.w("Error =====> MEDIA_ERROR_UNKNOWN")
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Timber.w("Error =====> MEDIA_ERROR_SERVER_DIED")
        }

        when (extra) {
            MediaPlayer.MEDIA_ERROR_IO -> Timber.w("Error =====> MEDIA_ERROR_IO")
            MediaPlayer.MEDIA_ERROR_MALFORMED -> Timber.w("Error =====> MEDIA_ERROR_MALFORMED")
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> Timber.w("Error =====> MEDIA_ERROR_TIME_OUT")
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> Timber.w("Error =====> MEDIA_ERROR_UNSUPPORTED")
        }

        return true
    }
}