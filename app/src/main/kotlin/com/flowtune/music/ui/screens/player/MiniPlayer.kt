// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.ui.styling.Dimensions
import com.flowtune.music.ui.styling.px
import com.flowtune.music.utils.DisposableListener
import com.flowtune.music.utils.LocalPlayerAccentStrong
import com.flowtune.music.utils.forceSeekToNext
import com.flowtune.music.utils.forceSeekToPrevious
import com.flowtune.music.utils.miniplayerGesturesEnabledKey
import com.flowtune.music.utils.positionAndDurationState
import com.flowtune.music.utils.rememberPreference
import com.flowtune.music.utils.shouldBePlaying
import com.flowtune.music.utils.thumbnail
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    openPlayer: () -> Unit,
    stopPlayer: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var miniplayerGesturesEnabled by rememberPreference(miniplayerGesturesEnabledKey, true)
    var shouldBePlaying by remember { mutableStateOf(binder.player.shouldBePlaying) }

    var nullableMediaItem by remember {
        mutableStateOf(binder.player.currentMediaItem, neverEqualPolicy())
    }

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return
    val positionAndDuration by binder.player.positionAndDurationState()

    val miniPlayerContent: @Composable BoxScope.() -> Unit = @Composable {
        Card(
            shape = RoundedCornerShape(48.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(48.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(48.dp)
                )
                .clickable(onClick = openPlayer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = {
                            positionAndDuration.first.toFloat() /
                                positionAndDuration.second.coerceAtLeast(1)
                        },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        strokeWidth = 2.dp
                    )
                    AsyncImage(
                        model = mediaItem.mediaMetadata.artworkUri
                            .thumbnail(Dimensions.thumbnails.song.px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = mediaItem.mediaMetadata.title?.toString() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LocalPlayerAccentStrong.current),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (shouldBePlaying) binder.player.pause()
                            else {
                                if (binder.player.playbackState == Player.STATE_IDLE) {
                                    binder.player.prepare()
                                } else if (binder.player.playbackState == Player.STATE_ENDED) {
                                    binder.player.seekToDefaultPosition(0)
                                }
                                binder.player.play()
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                shouldBePlaying -> Icons.Outlined.Pause
                                binder.player.playbackState == Player.STATE_ENDED -> Icons.Outlined.Replay
                                else -> Icons.Outlined.PlayArrow
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LocalPlayerAccentStrong.current),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = stopPlayer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    val startAction = SwipeAction(
        onSwipe = { binder.player.forceSeekToPrevious() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.SkipPrevious,
                contentDescription = null,
                modifier = Modifier.padding(end = 32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        background = MaterialTheme.colorScheme.primaryContainer
    )

    val endAction = SwipeAction(
        onSwipe = { binder.player.forceSeekToNext() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.SkipNext,
                contentDescription = null,
                modifier = Modifier.padding(start = 32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        background = MaterialTheme.colorScheme.primaryContainer
    )

    if (miniplayerGesturesEnabled) {
        SwipeableActionsBox(
            startActions = listOf(startAction),
            endActions = listOf(endAction),
            content = miniPlayerContent
        )
    } else {
        Box(content = miniPlayerContent)
    }
}
