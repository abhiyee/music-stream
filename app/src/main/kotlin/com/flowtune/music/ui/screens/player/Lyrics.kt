// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import com.github.kugou.KuGou
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.database
import com.flowtune.music.models.Lyrics
import com.flowtune.music.ui.components.TextPlaceholder
import com.flowtune.music.ui.styling.Dimensions
import com.flowtune.music.utils.SynchronizedLyrics
import com.flowtune.music.utils.verticalFadingEdge
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    durationProvider: () -> Long
) {
    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val currentView = LocalView.current

        var lyrics by remember {
            mutableStateOf<Lyrics?>(null)
        }

        val text = lyrics?.synced

        var isError by remember(mediaId) {
            mutableStateOf(false)
        }

        LaunchedEffect(mediaId) {
            withContext(Dispatchers.IO) {
                database.lyrics(mediaId).collect {
                    if (it?.synced == null) {
                        val mediaMetadata = mediaMetadataProvider()
                        var duration = withContext(Dispatchers.Main) {
                            durationProvider()
                        }

                        while (duration == C.TIME_UNSET) {
                            delay(duration = 100.milliseconds)
                            duration = withContext(Dispatchers.Main) {
                                durationProvider()
                            }
                        }

                        KuGou.lyrics(
                            artist = mediaMetadata.artist?.toString() ?: "",
                            title = mediaMetadata.title?.toString() ?: "",
                            duration = duration / 1000
                        )?.onSuccess { syncedLyrics ->
                            database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = it?.fixed,
                                    synced = syncedLyrics?.value ?: ""
                                )
                            )
                        }?.onFailure {
                            isError = true
                        }
                    } else {
                        lyrics = it
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            currentView.keepScreenOn = true
            onDispose {
                currentView.keepScreenOn = false
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .fillMaxSize()
                .background(Color.Black.copy(0.8f))
        ) {
            AnimatedVisibility(
                visible = isError && text == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(id = R.string.error_fetching_synchronized_lyrics),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = text?.let(String::isEmpty) == true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(id = R.string.synchronized_lyrics_not_available),
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            if (text?.isNotEmpty() == true) {
                val density = LocalDensity.current
                val player = LocalPlayerServiceBinder.current?.player
                    ?: return@AnimatedVisibility

                val synchronizedLyrics = remember(text) {
                    SynchronizedLyrics(KuGou.Lyrics(text).sentences) {
                        player.currentPosition + 50
                    }
                }

                val lazyListState = rememberLazyListState(
                    synchronizedLyrics.index,
                    with(density) { size.roundToPx() } / 6)

                LaunchedEffect(synchronizedLyrics) {
                    val center = with(density) { size.roundToPx() } / 6

                    while (isActive) {
                        delay(duration = 50.milliseconds)
                        if (synchronizedLyrics.update()) {
                            lazyListState.animateScrollToItem(
                                synchronizedLyrics.index,
                                center
                            )
                        }
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(vertical = size / 2),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalFadingEdge()
                ) {
                    itemsIndexed(items = synchronizedLyrics.sentences) { index, sentence ->
                        Text(
                            text = sentence.second,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            fontWeight = if (index == synchronizedLyrics.index)
                                FontWeight.SemiBold
                            else FontWeight.Normal,
                            lineHeight = 28.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                .alpha(if (index == synchronizedLyrics.index) 1F else Dimensions.mediumOpacity)
                        )
                    }
                }
            }

            if (text == null && !isError) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.shimmer()
                ) {
                    repeat(4) {
                        TextPlaceholder(
                            modifier = Modifier.alpha(1f - it * 0.2f)
                        )
                    }
                }
            }
        }
    }
}
