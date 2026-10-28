package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayItem
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.NeonRedGlow
import com.example.ui.theme.SkyBlueAccent

@Composable
fun PlayerControlsView(
    item: PlayItem,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentTime: Float,
    totalDuration: Float,
    playbackSpeed: Float,
    sleepTimerMinutes: Int?,
    sleepTimerRemainingSeconds: Int?,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekRelative: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPipClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onDimmerClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLive = totalDuration <= 0 || item.duration.contains("Live", ignoreCase = true)
    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Track Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title.ifEmpty { "Playing..." },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (item.channelName.isNotEmpty()) item.channelName else "YouTube Audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Live badge or current speed
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CrimsonPrimary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "● LIVE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (playbackSpeed != 1.0f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SkyBlueAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            color = SkyBlueAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrubber Bar (if not live)
            if (!isLive) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val sliderValue = if (isDraggingSlider) dragPosition else currentTime
                    Slider(
                        value = sliderValue.coerceIn(0f, totalDuration.coerceAtLeast(1f)),
                        onValueChange = {
                            isDraggingSlider = true
                            dragPosition = it
                        },
                        onValueChangeFinished = {
                            onSeek(dragPosition)
                            isDraggingSlider = false
                        },
                        valueRange = 0f..totalDuration.coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = CrimsonPrimary,
                            activeTrackColor = CrimsonPrimary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("playback_slider")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(if (isDraggingSlider) dragPosition else currentTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTime(totalDuration),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek Back 10s
                IconButton(
                    onClick = { onSeekRelative(-10f) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("seek_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10 seconds",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause FAB
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CrimsonPrimary, NeonRedGlow)
                            )
                        )
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(30.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Seek Forward 10s
                IconButton(
                    onClick = { onSeekRelative(10f) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("seek_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10 seconds",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Pills Bar (PiP, Speed, Sleep Timer, Dimmer, Favorite, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PiP Button
                ActionIconBadge(
                    icon = Icons.Default.PictureInPictureAlt,
                    label = "PiP",
                    badge = null,
                    highlight = false,
                    onClick = onPipClick,
                    tag = "pip_action_button"
                )

                // Speed Selector
                Box {
                    ActionIconBadge(
                        icon = Icons.Default.Speed,
                        label = "${playbackSpeed}x",
                        badge = null,
                        highlight = playbackSpeed != 1.0f,
                        onClick = { speedMenuExpanded = true },
                        tag = "speed_action_button"
                    )

                    DropdownMenu(
                        expanded = speedMenuExpanded,
                        onDismissRequest = { speedMenuExpanded = false }
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${speed}x",
                                        fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                                        color = if (playbackSpeed == speed) CrimsonPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSpeedChange(speed)
                                    speedMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Sleep Timer
                val sleepBadge = sleepTimerRemainingSeconds?.let { formatMinutesRemaining(it) }
                ActionIconBadge(
                    icon = Icons.Default.Bedtime,
                    label = "Sleep",
                    badge = sleepBadge,
                    highlight = sleepTimerMinutes != null,
                    onClick = onSleepTimerClick,
                    tag = "sleep_action_button"
                )

                // Screen Dimmer (Pocket Mode)
                ActionIconBadge(
                    icon = Icons.Default.DarkMode,
                    label = "Dimmer",
                    badge = null,
                    highlight = false,
                    onClick = onDimmerClick,
                    tag = "dimmer_action_button"
                )

                // Favorite
                ActionIconBadge(
                    icon = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    label = "Save",
                    badge = null,
                    highlight = item.isFavorite,
                    onClick = onFavoriteToggle,
                    tag = "favorite_action_button"
                )

                // Share
                ActionIconBadge(
                    icon = Icons.Default.Share,
                    label = "Share",
                    badge = null,
                    highlight = false,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, item.originalUrl)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share YouTube link"))
                    },
                    tag = "share_action_button"
                )
            }
        }
    }
}

@Composable
private fun ActionIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badge: String?,
    highlight: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    if (highlight) CrimsonPrimary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (highlight) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = badge ?: label,
            fontSize = 11.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(seconds: Float): String {
    val sec = seconds.toInt().coerceAtLeast(0)
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

private fun formatMinutesRemaining(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return if (m > 0) "${m}m" else "${s}s"
}
