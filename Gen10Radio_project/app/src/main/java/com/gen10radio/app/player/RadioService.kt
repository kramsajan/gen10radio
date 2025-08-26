package com.gen10radio.app.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.gen10radio.app.AppConfig

class RadioService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    /* handleAudioFocus= */ true
                )

                val metadata = MediaMetadata.Builder()
                    .setTitle(AppConfig.STATION_NAME)
                    .setArtist(AppConfig.STATION_SUBTITLE)
                    .build()

                val item = MediaItem.Builder()
                    .setUri(AppConfig.STREAM_URL)
                    .setMediaMetadata(metadata)
                    .build()

                setMediaItem(item)
                prepare()
            }

        mediaSession = MediaSession.Builder(this, player)
            .setId("gen10radio-session")
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.let { session ->
            session.player.release()
            session.release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val id = "radio_playback"
            if (mgr.getNotificationChannel(id) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(
                        id,
                        "Radio playback",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply { description = "Media playback controls" }
                )
            }
        }
    }
}
