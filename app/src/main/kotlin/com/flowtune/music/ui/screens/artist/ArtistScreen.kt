// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.github.innertube.Innertube
import com.github.innertube.requests.itemsPage
import com.github.innertube.requests.itemsPageContinuation
import com.github.innertube.utils.from
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.database
import com.flowtune.music.models.LocalMenuState
import com.flowtune.music.models.Section
import com.flowtune.music.ui.components.ExpressiveActionButton
import com.flowtune.music.ui.components.ExpressiveChip
import com.flowtune.music.ui.components.NonQueuedMediaItemMenu
import com.flowtune.music.ui.components.ShimmerHost
import com.flowtune.music.ui.items.AlbumItem
import com.flowtune.music.ui.items.ItemPlaceholder
import com.flowtune.music.ui.items.ListItemPlaceholder
import com.flowtune.music.ui.items.SongItem
import com.flowtune.music.ui.screens.search.ItemsPage
import com.flowtune.music.ui.styling.px
import com.flowtune.music.utils.LocalPlayerAccent
import com.flowtune.music.utils.LocalPlayerAccentDark
import com.flowtune.music.utils.artistScreenTabIndexKey
import com.flowtune.music.utils.asMediaItem
import com.flowtune.music.utils.forcePlay
import com.flowtune.music.utils.rememberPreference
import com.flowtune.music.utils.thumbnail
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
        Section(stringResource(id = R.string.albums), Icons.Outlined.Album)
    )
    var tabIndex by rememberPreference(artistScreenTabIndexKey, defaultValue = 0)
    val safeTabIndex = tabIndex.coerceIn(0, tabs.size - 1)
    val pagerState = rememberPagerState(
        initialPage = safeTabIndex,
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

    val isFollowing = viewModel.artist?.bookmarkedAt != null

    Scaffold(
        topBar = {
            Text(
                text = stringResource(id = R.string.artist),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            ArtistHeader(
                viewModel = viewModel,
                isFollowing = isFollowing
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, section ->
                    ExpressiveChip(
                        selected = index == pagerState.currentPage,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = section.title,
                        leadingIcon = section.icon
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { index ->
                when (index) {
                    0 -> ArtistOverview(
                        youtubeArtistPage = viewModel.artistPage,
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
                }
            }
        }
    }
}

@Composable
private fun ArtistHeader(
    viewModel: ArtistViewModel,
    isFollowing: Boolean
) {
    val binder = LocalPlayerServiceBinder.current

    val thumbnailUrl = viewModel.artist?.thumbnailUrl
    val isLoading = viewModel.artist?.timestamp == null

    val pictureModifier = Modifier
        .size(120.dp)
        .clip(RoundedCornerShape(32.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            if (isLoading) {
                ShimmerHost {
                    Spacer(
                        modifier = pictureModifier.background(MaterialTheme.colorScheme.shimmer)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))

                AsyncImage(
                    model = thumbnailUrl?.thumbnail(120.dp.px),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = pictureModifier
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = viewModel.artist?.name ?: "",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
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
                        .padding(horizontal = 24.dp, vertical = 10.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val shuffleEndpoint = viewModel.artistPage?.shuffleEndpoint
                    val radioEndpoint = viewModel.artistPage?.radioEndpoint

                    ExpressiveActionButton(
                        text = stringResource(id = R.string.shuffle),
                        icon = Icons.Outlined.Shuffle,
                        enabled = shuffleEndpoint != null,
                        backgroundColor = LocalPlayerAccent.current,
                        contentColor = Color.Black,
                        onClick = {
                            binder?.stopRadio()
                            binder?.playRadio(shuffleEndpoint)
                        }
                    )

                    ExpressiveActionButton(
                        text = stringResource(id = R.string.radio),
                        icon = Icons.Outlined.Podcasts,
                        enabled = radioEndpoint != null,
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            binder?.stopRadio()
                            binder?.playRadio(radioEndpoint)
                        }
                    )
                }
            }
        }
    }
}
