// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.database
import com.flowtune.music.utils.DisposableListener
import com.flowtune.music.utils.LocalPlayerAccentStrong
import com.flowtune.music.utils.isLandscape
import com.flowtune.music.utils.positionAndDurationState
import com.flowtune.music.utils.shouldBePlaying
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Player(
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var shouldBePlaying by remember { mutableStateOf(binder.player.shouldBePlaying) }
    var nullableMediaItem by remember {
        mutableStateOf(
            binder.player.currentMediaItem,
            neverEqualPolicy()
        )
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

    var artistId: String? by remember(mediaItem) {
        mutableStateOf(
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artists ->
                if (artists.size == 1) artists.first()
                else null
            }
        )
    }

    var isShowingLyrics by rememberSaveable { mutableStateOf(false) }
    var isShowingStatsForNerds by rememberSaveable { mutableStateOf(false) }
    var isQueueOpen by rememberSaveable { mutableStateOf(false) }
    val queueState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(mediaItem) {
        withContext(Dispatchers.IO) {
            if (artistId == null) {
                val artistsInfo = database.songArtistInfo(mediaItem.mediaId)
                if (artistsInfo.size == 1) artistId = artistsInfo.first().id
            }
        }
    }

    val thumbnailContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
        Thumbnail(
            isShowingLyrics = isShowingLyrics,
            onShowLyrics = { isShowingLyrics = it },
            isShowingStatsForNerds = isShowingStatsForNerds,
            onShowStatsForNerds = { isShowingStatsForNerds = it },
            modifier = modifier
        )
    }

    val controlsContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
        Controls(
            mediaId = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString().orEmpty(),
            artist = mediaItem.mediaMetadata.artist?.toString().orEmpty(),
            shouldBePlaying = shouldBePlaying,
            position = positionAndDuration.first,
            duration = positionAndDuration.second,
            onGoToArtist = artistId?.let {
                { onGoToArtist(it) }
            },
            modifier = modifier
        )
    }

    val accent = LocalPlayerAccentStrong.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to accent,
                        0.45f to accent.copy(alpha = 0.35f),
                        0.6f to Color.Transparent
                    )
                )
            )
    ) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.weight(1F)
        ) {
            if (isLandscape) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(0.66f)
                            .padding(bottom = 16.dp)
                    ) {
                        thumbnailContent(
                            Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    controlsContent(
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 54.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1.25f)
                    ) {
                        thumbnailContent(
                            Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                        )
                    }

                    controlsContent(
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BottomSheetDefaults.ExpandedShape)
                .clickable { isQueueOpen = true }
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(vertical = 2.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount < 0) isQueueOpen = true
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { isQueueOpen = true }) {
                Icon(
                    imageVector = Icons.Filled.ExpandLess,
                    contentDescription = stringResource(R.string.open_queue),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isQueueOpen) {
            ModalBottomSheet(
                onDismissRequest = { isQueueOpen = false },
                modifier = Modifier.fillMaxWidth(),
                sheetState = queueState,
                containerColor = Color.Black,
                dragHandle = {
                    Surface(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Black,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
                    }
                }
            ) {
                Queue(
                    onGoToAlbum = onGoToAlbum,
                    onGoToArtist = onGoToArtist
                )
            }
        }
    }
    }
}
