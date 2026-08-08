// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.innertube.Innertube
import com.github.innertube.requests.searchPage
import com.github.innertube.utils.from
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.models.ActionInfo
import com.flowtune.music.models.LocalMenuState
import com.flowtune.music.models.Section
import com.flowtune.music.ui.components.ChipScaffold
import com.flowtune.music.ui.components.NonQueuedMediaItemMenu
import com.flowtune.music.ui.components.SwipeToActionBox
import com.flowtune.music.ui.items.ListItemPlaceholder
import com.flowtune.music.ui.items.SongItem
import com.flowtune.music.ui.items.VideoItem
import com.flowtune.music.utils.asMediaItem
import com.flowtune.music.utils.enqueue
import com.flowtune.music.utils.forcePlay
import com.flowtune.music.utils.rememberPreference
import com.flowtune.music.utils.searchResultScreenTabIndexKey

@Composable
fun SearchResults(
    query: String,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit
) {
    val emptyItemsText = stringResource(id = R.string.no_results_found)
    val (tabIndex, onTabIndexChanges) = rememberPreference(searchResultScreenTabIndexKey, 0)
    val clampedTabIndex = tabIndex.coerceIn(0, 1)
    val sections = listOf(
        Section(stringResource(id = R.string.songs), Icons.Outlined.MusicNote),
        Section(stringResource(id = R.string.others), Icons.Outlined.Movie)
    )

    ChipScaffold(
        tabIndex = clampedTabIndex,
        onTabChanged = onTabIndexChanges,
        tabColumnContent = sections
    ) { index ->
        when (index) {
            0 -> {
                val binder = LocalPlayerServiceBinder.current
                val menuState = LocalMenuState.current

                ItemsPage(
                    tag = "searchResults/$query/songs",
                    itemsPageProvider = { continuation ->
                        if (continuation == null) {
                            Innertube.searchPage(
                                query = query,
                                params = Innertube.SearchFilter.Song.value,
                                fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                            )
                        } else {
                            Innertube.searchPage(
                                continuation = continuation,
                                fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                            )
                        }
                    },
                    emptyItemsText = emptyItemsText,
                    itemContent = { song ->
                        SwipeToActionBox(
                            modifier = Modifier.animateItem(),
                            primaryAction = ActionInfo(
                                onClick = { binder?.player?.enqueue(song.asMediaItem) },
                                icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                                description = R.string.enqueue
                            )
                        ) {
                            SongItem(
                                song = song,
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(song.asMediaItem)
                                    binder?.setupRadio(song.info?.endpoint)
                                },
                                onLongClick = {
                                    menuState.display {
                                        NonQueuedMediaItemMenu(
                                            onDismiss = menuState::hide,
                                            mediaItem = song.asMediaItem,
                                            onGoToAlbum = onAlbumClick,
                                            onGoToArtist = onArtistClick
                                        )
                                    }
                                }
                            )
                        }
                    },
                    itemPlaceholderContent = {
                        ListItemPlaceholder()
                    }
                )
            }

            1 -> {
                val binder = LocalPlayerServiceBinder.current
                val menuState = LocalMenuState.current

                ItemsPage(
                    tag = "searchResults/$query/others",
                    itemsPageProvider = { continuation ->
                        if (continuation == null) {
                            Innertube.searchPage(
                                query = query,
                                params = Innertube.SearchFilter.Video.value,
                                fromMusicShelfRendererContent = Innertube.VideoItem::from
                            )
                        } else {
                            Innertube.searchPage(
                                continuation = continuation,
                                fromMusicShelfRendererContent = Innertube.VideoItem::from
                            )
                        }
                    },
                    emptyItemsText = emptyItemsText,
                    itemContent = { video ->
                        SwipeToActionBox(
                            modifier = Modifier.animateItem(),
                            primaryAction = ActionInfo(
                                onClick = { binder?.player?.enqueue(video.asMediaItem) },
                                icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                                description = R.string.enqueue
                            )
                        ) {
                            VideoItem(
                                video = video,
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(video.asMediaItem)
                                    binder?.setupRadio(video.info?.endpoint)
                                },
                                onLongClick = {
                                    menuState.display {
                                        NonQueuedMediaItemMenu(
                                            mediaItem = video.asMediaItem,
                                            onDismiss = menuState::hide,
                                            onGoToAlbum = onAlbumClick,
                                            onGoToArtist = onArtistClick
                                        )
                                    }
                                }
                            )
                        }
                    },
                    itemPlaceholderContent = {
                        ListItemPlaceholder(
                            thumbnailHeight = 64.dp,
                            thumbnailAspectRatio = 16F / 9F
                        )
                    }
                )
            }
        }
    }
}
