// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.innertube.Innertube
import com.github.innertube.requests.itemsPage
import com.github.innertube.requests.itemsPageContinuation
import com.github.innertube.utils.from
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.database
import com.flowtune.music.models.LocalMenuState
import com.flowtune.music.models.Section
import com.flowtune.music.ui.components.NonQueuedMediaItemMenu
import com.flowtune.music.ui.components.TabScaffold
import com.flowtune.music.ui.components.adaptiveThumbnailContent
import com.flowtune.music.ui.items.AlbumItem
import com.flowtune.music.ui.items.ItemPlaceholder
import com.flowtune.music.ui.items.ListItemPlaceholder
import com.flowtune.music.ui.items.SongItem
import com.flowtune.music.ui.screens.search.ItemsPage
import com.flowtune.music.utils.LocalPlayerAccent
import com.flowtune.music.utils.LocalPlayerAccentDark
import com.flowtune.music.utils.artistScreenTabIndexKey
import com.flowtune.music.utils.asMediaItem
import com.flowtune.music.utils.forcePlay
import com.flowtune.music.utils.rememberPreference
import com.flowtune.music.viewmodels.ArtistViewModel
import kotlinx.coroutines.launch

@Composable
fun ArtistScreen(
    browseId: String,
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    val viewModel: ArtistViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        Section(stringResource(id = R.string.overview), Icons.Outlined.Person),
        Section(stringResource(id = R.string.songs), Icons.Outlined.MusicNote),
        Section(stringResource(id = R.string.albums), Icons.Outlined.Album),
        Section(stringResource(id = R.string.singles), Icons.Outlined.Album),
        Section(stringResource(id = R.string.library), Icons.Outlined.LibraryMusic)
    )
    var tabIndex by rememberPreference(artistScreenTabIndexKey, defaultValue = 0)
    val pagerState = rememberPagerState(
        initialPage = tabIndex,
        pageCount = { tabs.size }
    )

    LaunchedEffect(Unit) {
        viewModel.loadArtist(
            browseId = browseId,
            tabIndex = pagerState.currentPage
        )
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { index ->
            tabIndex = index
        }
    }

    val thumbnailContent = adaptiveThumbnailContent(
        isLoading = viewModel.artist?.timestamp == null,
        url = viewModel.artist?.thumbnailUrl
    )

    val isFollowing = viewModel.artist?.bookmarkedAt != null

    val followChipShape = RoundedCornerShape(percent = 50)

    TabScaffold(
        pagerState = pagerState,
        topIconButtonId = Icons.AutoMirrored.Outlined.ArrowBack,
        onTopIconButtonClick = pop,
        sectionTitle = viewModel.artist?.name ?: "",
        appBarActions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clip(followChipShape)
                    .background(
                        if (isFollowing) {
                            LocalPlayerAccentDark.current.copy(alpha = 0.3f)
                        } else {
                            LocalPlayerAccent.current
                        }
                    )
                    .clickable {
                        database.query {
                            viewModel.artist
                                ?.copy(
                                    bookmarkedAt = if (isFollowing) null else System.currentTimeMillis()
                                )
                                ?.let(database::update)
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFollowing) Icons.Filled.Check else Icons.Outlined.PersonAdd,
                        contentDescription = null,
                        tint = if (isFollowing) LocalPlayerAccent.current else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = stringResource(
                            id = if (isFollowing) R.string.following else R.string.follow
                        ),
                        color = if (isFollowing) LocalPlayerAccent.current else Color.Black,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LocalPlayerAccent.current)
                    .clickable(onClick = pop),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        tabColumnContent = tabs
    ) { index ->
        when (index) {
            0 -> ArtistOverview(
                youtubeArtistPage = viewModel.artistPage,
                thumbnailContent = thumbnailContent,
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onPlaylistClick = onPlaylistClick,
                onViewAllSongsClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                onViewAllAlbumsClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
                onViewAllSinglesClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(3)
                    }
                }
            )

            1 -> {
                val binder = LocalPlayerServiceBinder.current
                val menuState = LocalMenuState.current

                ItemsPage(
                    tag = "artist/$browseId/songs",
                    itemsPageProvider = viewModel.artistPage?.let { artistPage ->
                        { continuation ->
                            continuation?.let {
                                Innertube.itemsPageContinuation(
                                    continuation = continuation,
                                    fromMusicResponsiveListItemRenderer = Innertube.SongItem::from,
                                )
                            } ?: artistPage
                                .songsEndpoint
                                ?.takeIf { it.browseId != null }
                                ?.let { endpoint ->
                                    Innertube.itemsPage(
                                        browseId = endpoint.browseId!!,
                                        params = endpoint.params,
                                        fromMusicResponsiveListItemRenderer = Innertube.SongItem::from,
                                    )
                                }
                            ?: Result.success(
                                Innertube.ItemsPage(
                                    items = artistPage.songs,
                                    continuation = null
                                )
                            )
                        }
                    },
                    itemContent = { song ->
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
                                        onGoToAlbum = onAlbumClick
                                    )
                                }
                            }
                        )
                    },
                    itemPlaceholderContent = {
                        ListItemPlaceholder()
                    }
                )
            }

            2 -> ItemsPage(
                tag = "artist/$browseId/albums",
                emptyItemsText = stringResource(id = R.string.no_albums_artist),
                itemsPageProvider = viewModel.artistPage?.let { artistPage ->
                    { continuation ->
                        continuation?.let {
                            Innertube.itemsPageContinuation(
                                continuation = continuation,
                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                            )
                        } ?: artistPage
                            .albumsEndpoint
                            ?.takeIf { it.browseId != null }
                            ?.let { endpoint ->
                                Innertube.itemsPage(
                                    browseId = endpoint.browseId!!,
                                    params = endpoint.params,
                                    fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                )
                            }
                        ?: Result.success(
                            Innertube.ItemsPage(
                                items = artistPage.albums,
                                continuation = null
                            )
                        )
                    }
                },
                itemContent = { album ->
                    AlbumItem(
                        album = album,
                        onClick = { onAlbumClick(album.key) }
                    )
                },
                itemPlaceholderContent = {
                    ItemPlaceholder()
                }
            )

            3 -> ItemsPage(
                tag = "artist/$browseId/singles",
                emptyItemsText = stringResource(id = R.string.no_singles_artist),
                itemsPageProvider = viewModel.artistPage?.let { artistPage ->
                    { continuation ->
                        continuation?.let {
                            Innertube.itemsPageContinuation(
                                continuation = continuation,
                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                            )
                        } ?: artistPage
                            .singlesEndpoint
                            ?.takeIf { it.browseId != null }
                            ?.let { endpoint ->
                                Innertube.itemsPage(
                                    browseId = endpoint.browseId!!,
                                    params = endpoint.params,
                                    fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                )
                            }
                    ?: Result.success(
                            Innertube.ItemsPage(
                                items = artistPage.singles,
                                continuation = null
                            )
                        )
                    }
                },
                itemContent = { album ->
                    AlbumItem(
                        album = album,
                        onClick = { onAlbumClick(album.key) }
                    )
                },
                itemPlaceholderContent = {
                    ItemPlaceholder()
                }
            )

            4 -> ArtistLocalSongs(
                browseId = browseId,
                thumbnailContent = thumbnailContent,
                onGoToAlbum = onAlbumClick
            )
        }
    }
}
