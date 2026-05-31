package com.dynamicisland.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.math.abs

class OverlayWindowManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    // Mevcut durum
    var isVisible = false
        private set
    var isExpanded = false
        private set
    var mediaData by mutableStateOf(MediaData())
        private set

    // Zamanlayıcılar
    private val handler = Handler(Looper.getMainLooper())
    private var smallTimerRunnable: Runnable? = null
    private var expandedTimerRunnable: Runnable? = null

    // Dinamik renkler için state
    var dynamicTextColor by mutableStateOf(false)
    var dynamicButtonColor by mutableStateOf(false)
    var dynamicBackgroundColor by mutableStateOf(false)

    // Kullanıcı ayarları
    var smallDurationSec = 5
    var expandedDurationSec = 10

    // Pozisyon
    var horizontalPosition = "left"  // left, center, right
    var offsetX = 0
    var offsetY = 0

    // Sabit renkler
    var staticTextColor by mutableStateOf(androidx.compose.ui.graphics.Color.White)
    var staticButtonColor by mutableStateOf(androidx.compose.ui.graphics.Color.White)
    var staticBackgroundColor by mutableStateOf(androidx.compose.ui.graphics.Color.Black)

    // Referanslar
    private var onExpandRequest: (() -> Unit)? = null
    private var onCollapseRequest: (() -> Unit)? = null
    private var onPlayPause: (() -> Unit)? = null
    private var onNext: (() -> Unit)? = null
    private var onPrevious: (() -> Unit)? = null

    fun init(
        onExpand: () -> Unit,
        onCollapse: () -> Unit,
        onPlayPause: () -> Unit,
        onNext: () -> Unit,
        onPrevious: () -> Unit
    ) {
        this.onExpandRequest = onExpand
        this.onCollapseRequest = onCollapse
        this.onPlayPause = onPlayPause
        this.onNext = onNext
        this.onPrevious = onPrevious
    }

    fun showSmall(media: MediaData) {
        mediaData = media
        if (!isVisible) {
            addOverlayView()
            isVisible = true
        }
        isExpanded = false
        updateViewSize()
        // Zamanlayıcıyı başlat
        startSmallTimer()
    }

    fun expand() {
        if (!isVisible) return
        isExpanded = true
        updateViewSize()
        cancelSmallTimer()
        startExpandedTimer()
    }

    fun collapse() {
        isExpanded = false
        updateViewSize()
        startSmallTimer()
    }

    fun hide() {
        isVisible = false
        removeOverlayView()
        cancelAllTimers()
    }

    fun updateMedia(media: MediaData) {
        mediaData = media
        // Eğer görünür haldeyse yeni medya geldiğinde küçük moda geçip zamanlayıcıyı sıfırla
        if (isVisible) {
            isExpanded = false
            updateViewSize()
            cancelAllTimers()
            startSmallTimer()
        }
    }

    private fun addOverlayView() {
        if (overlayView != null) return

        val view = ComposeView(context).apply {
            setContent {
                DynamicIslandContent(
                    mediaData = mediaData,
                    isExpanded = isExpanded,
                    textColor = getEffectiveTextColor(),
                    buttonColor = getEffectiveButtonColor(),
                    backgroundColor = getEffectiveBackgroundColor(),
                    onTouch = {
                        if (!isExpanded) {
                            expand()
                            onExpandRequest?.invoke()
                        }
                    },
                    onPlayPause = { onPlayPause?.invoke() },
                    onNext = { onNext?.invoke() },
                    onPrevious = { onPrevious?.invoke() }
                )
            }
        }

        layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or when (horizontalPosition) {
                "left" -> Gravity.START
                "right" -> Gravity.END
                else -> Gravity.CENTER_HORIZONTAL
            }
            x = offsetX
            y = offsetY
        }

        // Dış dokunuşları yakala
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                if (isExpanded) {
                    collapse()
                    onCollapseRequest?.invoke()
                }
                true
            } else false
        }

        windowManager.addView(view, layoutParams)
        overlayView = view
    }

    private fun removeOverlayView() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    private fun updateViewSize() {
        // Compose içeriği otomatik boyutlanır, sadece dokunma alanı için gerekirse ayarlanır
    }

    private fun startSmallTimer() {
        cancelSmallTimer()
        smallTimerRunnable = Runnable {
            if (isVisible && !isExpanded) {
                hide()
            }
        }
        handler.postDelayed(smallTimerRunnable!!, smallDurationSec * 1000L)
    }

    private fun startExpandedTimer() {
        cancelExpandedTimer()
        expandedTimerRunnable = Runnable {
            if (isVisible && isExpanded) {
                collapse()
                onCollapseRequest?.invoke()
            }
        }
        handler.postDelayed(expandedTimerRunnable!!, expandedDurationSec * 1000L)
    }

    private fun cancelSmallTimer() {
        smallTimerRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun cancelExpandedTimer() {
        expandedTimerRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun cancelAllTimers() {
        cancelSmallTimer()
        cancelExpandedTimer()
    }

    private fun getEffectiveTextColor(): androidx.compose.ui.graphics.Color {
        return if (dynamicTextColor) extractPaletteColor(mediaData.albumArt) else staticTextColor
    }
    private fun getEffectiveButtonColor(): androidx.compose.ui.graphics.Color {
        return if (dynamicButtonColor) extractPaletteColor(mediaData.albumArt) else staticButtonColor
    }
    private fun getEffectiveBackgroundColor(): androidx.compose.ui.graphics.Color {
        return if (dynamicBackgroundColor) extractPaletteColor(mediaData.albumArt) else staticBackgroundColor
    }

    private fun extractPaletteColor(bitmap: android.graphics.Bitmap?): androidx.compose.ui.graphics.Color {
        if (bitmap == null) return staticTextColor
        val palette = androidx.palette.graphics.Palette.from(bitmap).generate()
        val dominant = palette.getDominantColor(staticTextColor.hashCode())
        return androidx.compose.ui.graphics.Color(dominant)
    }

    fun destroy() {
        hide()
    }
}