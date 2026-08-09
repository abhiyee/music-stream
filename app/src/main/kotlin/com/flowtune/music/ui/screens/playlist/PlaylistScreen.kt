// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.playlist

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.github.innertube.Innertube
import com.github.innertube.requests.playlistPage
import com.flowtune.music.R
import com.flowtune.music.database
import com.flowtune.music.models.Playlist
import com.flowtune.music.models.SongPlaylistMap
import com.flowtune.music.ui.components.TextFieldDialog
import com.flowtune.music.ui.components.TooltipIconButton
import com.flowtune.music.utils.asMediaItem
import com.flowtune.music.utils.completed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    browseId: String,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    var playlistPage: Innertube.PlaylistOrAlbumPage? by remember { mutableStateOf(null) }
    var isImportingPlaylist by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (playlistPage != null && playlistPage?.songsPage?.continuation == null) return@LaunchedEffect

        playlistPage = withContext(Dispatchers.IO) {
            Innertube.playlistPage(browseId = browseId)?.completed()?.getOrNull()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = playlistPage?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black
                        )
                    )
                },
                actions = {
                    val context = LocalContext.current

                    TooltipIconButton(
                        description = R.string.import_playlist,
                        onClick = { isImportingPlaylist = true },
                        icon = Icons.Outlined.LibraryAdd,
                        inTopBar = true
                    )

                    TooltipIconButton(
                        description = R.string.share,
                        onClick = {
                            (playlistPage?.url
                                ?: "https://music.youtube.com/playlist?list=${
                                    browseId.removePrefix(
                                        "VL"
                                    )
                                }").let { url ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, url)
                                }

                                context.startActivity(
                                    Intent.createChooser(
                                        sendIntent,
                                        null
                                    )
                                )
                            }
                        },
                        icon = Icons.Outlined.Share,
                        inTopBar = true
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            PlaylistSongList(
                playlistPage = playlistPage,
                onGoToAlbum = onGoToAlbum,
                onGoToArtist = onGoToArtist
            )

            if (isImportingPlaylist) {
                TextFieldDialog(
                    title = stringResource(id = R.string.import_playlist),
                    hintText = stringResource(id = R.string.playlist_name_hint),
                    initialTextInput = playlistPage?.title ?: "",
                    onDismiss = { isImportingPlaylist = false },
                    onDone = { text ->
                        database.query {
                            database.transaction {
                                val playlistId = database.insert(
                                    Playlist(
                                        name = text,
                                        browseId = browseId
                                    )
                                )

                                playlistPage?.songsPage?.items
                                    ?.map(Innertube.SongItem::asMediaItem)
                                    ?.onEach(database::insert)
                                    ?.mapIndexed { index, mediaItem ->
                                        SongPlaylistMap(
                                            songId = mediaItem.mediaId,
                                            playlistId = playlistId,
                                            position = index
                                        )
                                    }?.let(database::insertSongPlaylistMaps)
                            }
                        }
                    }
                )
            }
        }
    }
}
