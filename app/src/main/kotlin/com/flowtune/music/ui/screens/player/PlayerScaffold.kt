// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flowtune.music.LocalPlayerPadding
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScaffold(
    navController: NavController,
    sheetState: SheetState,
    scaffoldPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    Box(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets(
                left = scaffoldPadding.calculateLeftPadding(layoutDirection),
                right = scaffoldPadding.calculateRightPadding(layoutDirection)
            )
        )
    ) {
        BottomSheetScaffold(
            sheetContent = {
                AnimatedContent(
                    targetState = sheetState.targetValue,
                    label = "player",
                    contentKey = { value ->
                        if (value == SheetValue.Expanded) 0 else 1
                    }
                ) { value ->
                    if (value == SheetValue.Expanded) {
                        Player(
                            onGoToAlbum = { browseId ->
                                scope.launch { sheetState.partialExpand() }
                                navController.navigate(
                                    route = com.flowtune.music.ui.navigation.Routes.Album(
                                        id = browseId
                                    )
                                )
                            },
                            onGoToArtist = { browseId ->
                                scope.launch { sheetState.partialExpand() }
                                navController.navigate(
                                    route = com.flowtune.music.ui.navigation.Routes.Artist(
                                        id = browseId
                                    )
                                )
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.dp)
                        )
                    }
                }
            },
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetMaxWidth = Int.MAX_VALUE.dp,
            sheetDragHandle = {
                Surface(
                    modifier = Modifier.height(0.dp),
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(0.dp))
                }
            }
        ) {
            CompositionLocalProvider(
                value = LocalPlayerPadding provides scaffoldPadding.calculateBottomPadding()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    content = content
                )
            }
        }
    }
}
