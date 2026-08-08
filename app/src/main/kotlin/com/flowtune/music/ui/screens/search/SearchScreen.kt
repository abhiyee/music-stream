// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.innertube.Innertube
import com.github.innertube.requests.searchPage
import com.github.innertube.requests.searchSuggestions
import com.github.innertube.utils.from
import com.flowtune.music.LocalPlayerPadding
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.database
import com.flowtune.music.enums.SongSortBy
import com.flowtune.music.enums.SortOrder
import com.flowtune.music.models.LocalMenuState
import com.flowtune.music.models.SearchQuery
import com.flowtune.music.models.Song
import com.flowtune.music.ui.components.NonQueuedMediaItemMenu
import com.flowtune.music.ui.styling.Dimensions
import com.flowtune.music.ui.styling.px
import com.flowtune.music.utils.LocalPlayerAccent
import com.flowtune.music.utils.asMediaItem
import com.flowtune.music.utils.forcePlay
import com.flowtune.music.utils.pauseSearchHistoryKey
import com.flowtune.music.utils.preferences
import com.flowtune.music.utils.thumbnail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit
) {
    val context = LocalContext.current
    var searchText: String? by rememberSaveable { mutableStateOf(null) }
    var history: List<SearchQuery> by remember { mutableStateOf(emptyList()) }
    var librarySuggestion: Song? by remember { mutableStateOf(null) }
    var suggestionsResult: Result<List<String>?>? by remember { mutableStateOf(null) }
    var topSong: Innertube.SongItem? by remember { mutableStateOf(null) }

    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val onBack: () -> Unit = {
        if (searchText == null) pop()
        else {
            searchText = null
            focusRequester.requestFocus()
        }
    }

    val onSearch: (String) -> Unit = { query ->
        if (query.isNotEmpty()) {
            textFieldState.setTextAndPlaceCursorAtEnd(text = query)
            searchText = query
            focusManager.clearFocus()
            keyboardController?.hide()

            if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                database.query {
                    database.insert(SearchQuery(query = query))
                }
            }
        }
    }

    val inputField = @Composable {
        SearchInputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = onSearch,
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused && searchText != null) searchText = null
                },
            onClear = {
                textFieldState.clearText()
                if (searchText != null) searchText = null
                focusRequester.requestFocus()
            }
        )
    }

    LaunchedEffect(textFieldState.text) {
        if (textFieldState.text.isNotEmpty()) {
            database.songs(SongSortBy.PlayTime, SortOrder.Descending).collect {
                librarySuggestion = it.find { song ->
                    song.title.contains(
                        other = textFieldState.text,
                        ignoreCase = true
                    )
                }
            }
        } else librarySuggestion = null
    }

    LaunchedEffect(textFieldState.text) {
        if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
            database.queries("%${textFieldState.text}%")
                .distinctUntilChanged { old, new -> old.size == new.size }
                .collect { history = it }
        }
    }

    LaunchedEffect(textFieldState.text) {
        suggestionsResult = if (textFieldState.text.isNotEmpty()) {
            delay(duration = 200.milliseconds)
            Innertube.searchSuggestions(input = "${textFieldState.text}")
        } else null
    }

    LaunchedEffect(textFieldState.text) {
        if (textFieldState.text.isNotEmpty()) {
            delay(duration = 200.milliseconds)
            topSong = Innertube.searchPage(
                query = "${textFieldState.text}",
                params = Innertube.SearchFilter.Song.value,
                fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
            )?.getOrNull()?.items?.firstOrNull()
        } else topSong = null
    }

    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(SearchBarDefaults.windowInsets)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LocalPlayerAccent.current)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                SearchBar(
                    state = searchBarState,
                    inputField = inputField,
                    shape = SearchPillShape,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            if (searchText == null) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = LocalPlayerPadding.current + 16.dp)
                ) {
                    topSong?.let { song ->
                        item(key = "topSong") {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = song.info?.name ?: "",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .clickable {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(song.asMediaItem)
                                        pop()
                                    },
                                supportingContent = {
                                    val artists = song.authors
                                        ?.joinToString("") { it.name ?: "" }
                                    if (!artists.isNullOrEmpty()) {
                                        Text(
                                            text = artists,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                leadingContent = {
                                    Box(modifier = Modifier.size(size = 56.dp)) {
                                        AsyncImage(
                                            model = song.thumbnail?.url?.thumbnail(size = 56.dp.px),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(shape = MaterialTheme.shapes.medium)
                                        )
                                    }
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem,
                                                    onGoToAlbum = onAlbumClick,
                                                    onGoToArtist = onArtistClick
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.MoreVert,
                                            contentDescription = null
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Black
                                )
                            )
                        }
                    }

                    librarySuggestion?.let { song ->
                        item(key = "librarySuggestion") {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = song.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .clickable {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(song.asMediaItem)
                                        pop()
                                    },
                                supportingContent = {
                                    song.artistsText?.let { artists ->
                                        Text(
                                            text = artists,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                leadingContent = {
                                    Box(modifier = Modifier.size(size = 56.dp)) {
                                        AsyncImage(
                                            model = song.thumbnailUrl?.thumbnail(size = 56.dp.px),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(shape = MaterialTheme.shapes.medium)
                                        )
                                    }
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem,
                                                    onGoToAlbum = onAlbumClick
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.MoreVert,
                                            contentDescription = null
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Black
                                )
                            )
                        }
                    }

                    items(
                        items = history,
                        key = SearchQuery::id
                    ) { query ->
                        ListItem(
                            headlineContent = {
                                Text(text = query.query)
                            },
                            modifier = Modifier.clickable { onSearch(query.query) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null
                                )
                            },
                            trailingContent = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            database.query { database.delete(query) }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = null
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            textFieldState.setTextAndPlaceCursorAtEnd(text = query.query)
                                        },
                                        modifier = Modifier.rotate(225F)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Black
                            )
                        )
                    }

                    suggestionsResult?.getOrNull()?.let { suggestions ->
                        items(items = suggestions) { suggestion ->
                            ListItem(
                                headlineContent = {
                                    Text(text = suggestion)
                                },
                                modifier = Modifier.clickable { onSearch(suggestion) },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = { textFieldState.setTextAndPlaceCursorAtEnd(text = suggestion) },
                                        modifier = Modifier.rotate(225F)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = null
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Black
                                )
                            )
                        }
                    } ?: suggestionsResult?.exceptionOrNull()?.let {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "An error has occurred.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .alpha(Dimensions.mediumOpacity)
                                )
                            }
                        }
                    }
                }
            } else {
                searchText?.let { query ->
                    SearchResults(
                        query = query,
                        onAlbumClick = onAlbumClick,
                        onArtistClick = onArtistClick
                    )
                }
            }
        }
    }
}
