// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.innertube.Innertube
import com.flowtune.music.LocalPlayerPadding
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.models.ActionInfo
import com.flowtune.music.models.LocalMenuState
import com.flowtune.music.ui.components.ExpressiveActionButton
import com.flowtune.music.ui.components.NonQueuedMediaItemMenu
import com.flowtune.music.ui.components.ShimmerHost
import com.flowtune.music.ui.components.SwipeToActionBox
import com.flowtune.music.ui.items.ListItemPlaceholder
import com.flowtune.music.ui.items.SongItem
import com.flowtune.music.ui.styling.px
import com.flowtune.music.ui.styling.shimmer
import com.flowtune.music.utils.LocalPlayerAccent
import com.flowtune.music.utils.asMediaItem
import com.flowtune.music.utils.enqueue
import com.flowtune.music.utils.forcePlayAtIndex
import com.flowtune.music.utils.forcePlayFromBeginning
import com.flowtune.music.utils.thumbnail

@Composable
fun PlaylistSongList(
    playlistPage: Innertube.PlaylistOrAlbumPage?,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current

    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp + playerPadding),
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "art") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val artModifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)

                if (playlistPage == null) {
                    ShimmerHost {
                        Spacer(
                            modifier = artModifier.background(MaterialTheme.colorScheme.shimmer)
                        )
                    }
                } else {
                    AsyncImage(
                        model = playlistPage?.thumbnail?.url?.thumbnail(140.dp.px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = artModifier
                    )
                }
            }
        }

        item(key = "actions") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                ExpressiveActionButton(
                    text = stringResource(id = R.string.shuffle),
                    icon = Icons.Outlined.Shuffle,
                    enabled = playlistPage != null,
                    backgroundColor = LocalPlayerAccent.current,
                    contentColor = androidx.compose.ui.graphics.Color.Black,
                    onClick = {
                        playlistPage?.songsPage?.items?.let { songs ->
                            if (songs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    songs.shuffled().map(Innertube.SongItem::asMediaItem)
                                )
                            }
                        }
                    }
                )

                ExpressiveActionButton(
                    text = stringResource(id = R.string.enqueue),
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    enabled = playlistPage != null,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        playlistPage?.songsPage?.items?.map(Innertube.SongItem::asMediaItem)
                            ?.let { mediaItems ->
                                binder?.player?.enqueue(mediaItems)
                            }
                    }
                )
            }
        }

        item(key = "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        itemsIndexed(items = playlistPage?.songsPage?.items ?: emptyList()) { index, song ->
            SwipeToActionBox(
                primaryAction = ActionInfo(
                    onClick = { binder?.player?.enqueue(song.asMediaItem) },
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    description = R.string.enqueue
                )
            ) {
                SongItem(
                    song = song,
                    onClick = {
                        playlistPage?.songsPage?.items?.map(Innertube.SongItem::asMediaItem)
                            ?.let { mediaItems ->
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(mediaItems, index)
                            }
                    },
                    onLongClick = {
                        menuState.display {
                            NonQueuedMediaItemMenu(
                                onDismiss = menuState::hide,
                                mediaItem = song.asMediaItem,
                                onGoToAlbum = onGoToAlbum,
                                onGoToArtist = onGoToArtist
                            )
                        }
                    }
                )
            }
        }

        if (playlistPage == null) {
            item(key = "loading") {
                ShimmerHost {
                    repeat(4) {
                        ListItemPlaceholder()
                    }
                }
            }
        }
    }
}
