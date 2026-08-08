// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.player

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.database
import com.flowtune.music.models.Song
import com.flowtune.music.ui.components.SeekBar
import com.flowtune.music.ui.styling.Dimensions
import com.flowtune.music.utils.forceSeekToNext
import com.flowtune.music.utils.forceSeekToPrevious
import com.flowtune.music.utils.LocalPlayerAccent
import com.flowtune.music.utils.formatAsDuration
import com.flowtune.music.utils.rememberPreference
import com.flowtune.music.utils.trackLoopEnabledKey
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Controls(
    mediaId: String,
    title: String,
    artist: String,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    onGoToArtist: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)
    var scrubbingPosition by remember(mediaId) { mutableStateOf<Long?>(null) }
    var likedAt by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(mediaId) {
        database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = title,
            modifier = Modifier.basicMarquee(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = artist,
            modifier = Modifier.clickable(
                enabled = onGoToArtist != null,
                onClick = onGoToArtist ?: {}
            ),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.weight(0.5f)
        )

        SeekBar(
            value = scrubbingPosition ?: position,
            minimumValue = 0,
            maximumValue = duration,
            onDragStart = {
                scrubbingPosition = it
            },
            onDrag = { delta ->
                scrubbingPosition = if (duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                } else {
                    null
                }
            },
            onDragEnd = {
                scrubbingPosition?.let(binder.player::seekTo)
                scrubbingPosition = null
            },
            color = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                Text(
                    text = formatAsDuration(duration),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(40.dp),
            color = LocalPlayerAccent.current,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        val currentMediaItem = binder.player.currentMediaItem
                        database.query {
                            if (database.like(
                                    mediaId,
                                    if (likedAt == null) System.currentTimeMillis() else null
                                ) == 0
                            ) {
                                currentMediaItem
                                    ?.takeIf { it.mediaId == mediaId }
                                    ?.let {
                                        database.insert(currentMediaItem, Song::toggleLike)
                                    }
                            }
                        }
                    },
                    modifier = Modifier.weight(1F)
                ) {
                    Icon(
                        imageVector = if (likedAt == null) Icons.Outlined.FavoriteBorder else Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = Color.Black
                    )
                }

                IconButton(
                    onClick = binder.player::forceSeekToPrevious,
                    modifier = Modifier.weight(1F)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (shouldBePlaying) {
                            binder.player.pause()
                        } else {
                            if (binder.player.playbackState == Player.STATE_IDLE) {
                                binder.player.prepare()
                            } else if (binder.player.playbackState == Player.STATE_ENDED) {
                                binder.player.seekToDefaultPosition(0)
                            }
                            binder.player.play()
                        }
                    },
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector =
                        if (shouldBePlaying) Icons.Filled.Pause
                        else if (binder.player.playbackState == Player.STATE_ENDED) Icons.Filled.Replay
                        else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = binder.player::forceSeekToNext,
                    modifier = Modifier.weight(1F)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )
                }

                IconButton(
                    onClick = { trackLoopEnabled = !trackLoopEnabled },
                    modifier = Modifier.weight(1F)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RepeatOne,
                        contentDescription = null,
                        modifier = Modifier
                            .alpha(if (trackLoopEnabled) 1F else Dimensions.lowOpacity)
                            .size(26.dp),
                        tint = Color.Black
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )
    }
}
