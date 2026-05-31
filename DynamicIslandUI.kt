package com.dynamicisland.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DynamicIslandContent(
    mediaData: MediaData,
    isExpanded: Boolean,
    textColor: Color,
    buttonColor: Color,
    backgroundColor: Color,
    onTouch: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    // Spring animasyonu ile genişleme/daralma
    val targetWidth = if (isExpanded) 280.dp else 48.dp
    val targetHeight = if (isExpanded) 80.dp else 48.dp
    val widthAnim by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 0.5.dp
        )
    )
    val heightAnim by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 0.5.dp
        )
    )

    Box(
        modifier = Modifier
            .size(width = widthAnim, height = heightAnim)
            .clip(shape)
            .background(backgroundColor.copy(alpha = 0.8f))
            .clickable { onTouch() },
        contentAlignment = Alignment.CenterStart
    ) {
        if (!isExpanded) {
            // Küçük mod: sadece albüm kapağı
            mediaData.albumArt?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // Büyük mod: albüm kapağı, şarkı bilgisi ve kontroller
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                mediaData.albumArt?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = mediaData.title ?: "Unknown",
                        color = textColor,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mediaData.artist ?: "Unknown Artist",
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = buttonColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onPlayPause, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (mediaData.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (mediaData.isPlaying) "Pause" else "Play",
                            tint = buttonColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = buttonColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}