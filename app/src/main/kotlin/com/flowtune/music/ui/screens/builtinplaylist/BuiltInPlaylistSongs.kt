// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.builtinplaylist

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.flowtune.music.LocalPlayerPadding
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.database
import com.flowtune.music.enums.BuiltInPlaylist
import com.flowtune.music.models.ActionInfo
import com.flowtune.music.models.LocalMenuState
import com.flowtune.music.models.Song
import com.flowtune.music.models.SongWithContentLength
import com.flowtune.music.ui.components.CoverScaffold
import com.flowtune.music.ui.components.InHistoryMediaItemMenu
import com.flowtune.music.ui.components.NonQueuedMediaItemMenu
import com.flowtune.music.ui.components.SwipeToActionBox
import com.flowtune.music.ui.items.LocalSongItem
import com.flowtune.music.utils.asMediaItem
import com.flowtune.music.utils.enqueue
import com.flowtune.music.utils.forcePlayAtIndex
import com.flowtune.music.utils.forcePlayFromBeginning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@OptIn(UnstableApi::class)
@Composable
fun BuiltInPlaylistSongs(
    builtInPlaylist: BuiltInPlaylist,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current

    var songs: List<Song> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(Unit) {
        when (builtInPlaylist) {
            BuiltInPlaylist.Favorites -> database.favorites()

            BuiltInPlaylist.Offline -> database
                .songsWithContentLength()
                .flowOn(Dispatchers.IO)
                .map { songs ->
                    songs.filter { song ->
                        song.contentLength?.let {
                            binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                        } ?: false
                    }.map(SongWithContentLength::song)
                }
        }.collect { songs = it }
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp + playerPadding),
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "thumbnail") {
            CoverScaffold(
                primaryButton = ActionInfo(
                    enabled = songs.isNotEmpty(),
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            songs.shuffled().map(Song::asMediaItem)
                        )
                    },
                    icon = Icons.Outlined.Shuffle,
                    description = R.string.shuffle
                ),
                secondaryButton = ActionInfo(
                    enabled = songs.isNotEmpty(),
                    onClick = {
                        binder?.player?.enqueue(songs.map(Song::asMediaItem))
                    },
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    description = R.string.enqueue
                ),
                content = {
                    BuiltInPlaylistThumbnail(builtInPlaylist = builtInPlaylist)
                }
            )
        }

        item(key = "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        itemsIndexed(
            items = songs,
            key = { _, song -> song.id },
            contentType = { _, song -> song },
        ) { index, song ->
            SwipeToActionBox(
                modifier = Modifier.animateItem(),
                primaryAction = ActionInfo(
                    onClick = { binder?.player?.enqueue(song.asMediaItem) },
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    description = R.string.enqueue
                )
            ) {
                LocalSongItem(
                    song = song,
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayAtIndex(
                            songs.map(Song::asMediaItem),
                            index
                        )
                    },
                    onLongClick = {
                        menuState.display {
                            when (builtInPlaylist) {
                                BuiltInPlaylist.Favorites -> NonQueuedMediaItemMenu(
                                    mediaItem = song.asMediaItem,
                                    onDismiss = menuState::hide,
                                    onGoToAlbum = onGoToAlbum,
                                    onGoToArtist = onGoToArtist
                                )

                                BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                    song = song,
                                    onDismiss = menuState::hide,
                                    onGoToAlbum = onGoToAlbum,
                                    onGoToArtist = onGoToArtist
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
