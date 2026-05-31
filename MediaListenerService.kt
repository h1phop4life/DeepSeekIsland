package com.dynamicisland.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import de.robv.android.xposed.XposedHelpers

class MediaListenerService : LifecycleService() {

    private lateinit var overlayManager: OverlayWindowManager
    private var mediaController: MediaController? = null
    private var mediaSessionManager: MediaSessionManager? = null

    override fun onCreate() {
        super.onCreate()
        overlayManager = OverlayWindowManager(this)

        // Overlay callback'leri
        overlayManager.init(
            onExpand = { /* büyük mod açıldı */ },
            onCollapse = { /* küçük moda döndü */ },
            onPlayPause = { mediaController?.transportControls?.let {
                if (mediaController?.playbackState?.state == PlaybackState.STATE_PLAYING) it.pause()
                else it.play()
            }},
            onNext = { mediaController?.transportControls?.skipToNext() },
            onPrevious = { mediaController?.transportControls?.skipToPrevious() }
        )

        startForegroundNotification()
        setupMediaListener()
    }

    private fun startForegroundNotification() {
        val channelId = "dynamic_island_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Dynamic Island Service",
                NotificationManager.IMPORTANCE_MIN // Minimal ses/ışık
            ).apply {
                description = "Minimalist medya dinleyici servisi"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Dynamic Island")
            .setContentText("Medya dinleniyor...")
            .setSmallIcon(R.drawable.ic_notification)  // 24dp boyutlu basit bir ikon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun setupMediaListener() {
        // LSPosed yardımıyla kısıtlamasız MediaSessionManager al
        mediaSessionManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ için Xposed hook ile getSystemService çağrısını atlıyoruz
            XposedHelpers.callStaticMethod(
                XposedHelpers.findClass("android.media.session.MediaSessionManager", null),
                "getService",
                this
            ) as? MediaSessionManager ?: getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        } else {
            getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        }

        // Aktif session'ları al ve ilk medya controller'ı kur
        val sessions = mediaSessionManager?.getActiveSessions(null)
        if (!sessions.isNullOrEmpty()) {
            updateMediaController(sessions[0])
        }

        // Dinleyiciyi ekle (API 31+ için farklı, eski sürümlerde farklı)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaSessionManager?.addOnActiveSessionsChangedListener(
                { controllers ->
                    if (controllers.isNotEmpty()) {
                        updateMediaController(controllers[0])
                    } else {
                        mediaController = null
                        overlayManager.hide()
                    }
                },
                null
            )
        } else {
            // Eski API'ler için periyodik kontrol
            // Gerçek uygulamada Handler ile döngü kurulabilir, burada kısalttık.
        }
    }

    private fun updateMediaController(token: MediaSession.Token) {
        mediaController?.unregisterCallback(mediaCallback)
        mediaController = MediaController(this, token)
        mediaController?.registerCallback(mediaCallback)
        // Mevcut durumu hemen al
        onMetadataChanged(mediaController?.metadata)
        onPlaybackStateChanged(mediaController?.playbackState)
    }

    private val mediaCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
            val artBitmap: Bitmap? = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

            val isPlaying = mediaController?.playbackState?.state == PlaybackState.STATE_PLAYING
            val mediaData = MediaData(title, artist, album, artBitmap, isPlaying, mediaController?.sessionToken)

            if (overlayManager.isVisible) {
                overlayManager.updateMedia(mediaData)
            } else {
                overlayManager.showSmall(mediaData)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            val isPlaying = state?.state == PlaybackState.STATE_PLAYING
            overlayManager.mediaData = overlayManager.mediaData.copy(isPlaying = isPlaying)
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            overlayManager.hide()
            mediaController = null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        mediaController?.unregisterCallback(mediaCallback)
        overlayManager.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null
}