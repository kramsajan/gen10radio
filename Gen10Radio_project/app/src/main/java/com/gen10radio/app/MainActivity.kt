package com.gen10radio.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.gen10radio.app.player.RadioService

class MainActivity : ComponentActivity() {
    private var controller: MediaController? = null

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeAskForNotificationPermission()

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    var isPlaying by remember { mutableStateOf(false) }
                    RadioScreen(
                        title = AppConfig.STATION_NAME,
                        subtitle = AppConfig.STATION_SUBTITLE,
                        isPlaying = isPlaying,
                        onPlayPause = {
                            togglePlay()
                            isPlaying = controller?.isPlaying == true
                        },
                        onStop = {
                            controller?.stop()
                            isPlaying = false
                        },
                        onOpenWeb = { startActivity(Intent(this, WebPlayerActivity::class.java)) },
                        streamHint = streamHint(AppConfig.STREAM_URL)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, RadioService::class.java))
        val future = MediaController.Builder(this, sessionToken).buildAsync()
        future.addListener({
            controller = future.get()
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controller)
        controller = null
    }

    private fun togglePlay() {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            c.setMediaItem(MediaItem.fromUri(AppConfig.STREAM_URL))
            c.prepare()
        }
        if (c.isPlaying) c.pause() else c.play()
    }

    private fun maybeAskForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun RadioScreen(
    title: String,
    subtitle: String,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onOpenWeb: () -> Unit,
    streamHint: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        if (streamHint.isNotEmpty()) {
            Text(streamHint, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onPlayPause) { Text(if (isPlaying) "Pause" else "Play") }
            OutlinedButton(onClick = onStop) { Text("Stop") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenWeb) { Text("Open Web Player") }
    }
}

fun streamHint(url: String): String {
    val lc = url.lowercase()
    val looksLikeAudio = lc.endsWith(".mp3") || lc.endsWith(".aac") || lc.endsWith(".m3u8")
    return if (looksLikeAudio) "" else "Note: The configured URL looks like a website. If Play fails, use Open Web Player."
}
