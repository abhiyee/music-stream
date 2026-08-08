// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flowtune.music.LocalPlayerPadding
import com.flowtune.music.R
import com.flowtune.music.enums.AlbumSortBy
import com.flowtune.music.enums.SortOrder
import com.flowtune.music.models.Album
import com.flowtune.music.ui.components.HomeScaffold
import com.flowtune.music.ui.components.SortingHeader
import com.flowtune.music.ui.items.LocalAlbumItem
import com.flowtune.music.utils.albumSortByKey
import com.flowtune.music.utils.albumSortOrderKey
import com.flowtune.music.utils.rememberPreference
import com.flowtune.music.viewmodels.HomeAlbumsViewModel

@Composable
fun HomeAlbums(
    openSettings: () -> Unit,
    onAlbumClick: (Album) -> Unit
) {
    val playerPadding = LocalPlayerPadding.current

    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.Title)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Ascending)

    val viewModel: HomeAlbumsViewModel = viewModel()

    LaunchedEffect(sortBy, sortOrder) {
        viewModel.loadAlbums(
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    HomeScaffold(
        openSettings = openSettings
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                bottom = 16.dp + playerPadding
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(
                key = "header",
                span = { GridItemSpan(maxCurrentLineSpan) }
            ) {
                SortingHeader(
                    sortBy = sortBy,
                    changeSortBy = { sortBy = it },
                    sortByEntries = AlbumSortBy.entries.toList(),
                    sortOrder = sortOrder,
                    toggleSortOrder = { sortOrder = !sortOrder },
                    size = viewModel.items.size,
                    itemCountText = R.plurals.number_of_albums
                )
            }

            items(
                items = viewModel.items,
                key = Album::id
            ) { album ->
                LocalAlbumItem(
                    modifier = Modifier.animateItem(),
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}
