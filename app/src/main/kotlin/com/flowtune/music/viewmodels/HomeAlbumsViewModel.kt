// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flowtune.music.database
import com.flowtune.music.enums.AlbumSortBy
import com.flowtune.music.enums.SortOrder
import com.flowtune.music.models.Album

class HomeAlbumsViewModel : ViewModel() {
    var items: List<Album> by mutableStateOf(emptyList())

    suspend fun loadAlbums(
        sortBy: AlbumSortBy,
        sortOrder: SortOrder
    ) {
        database
            .albums(sortBy, sortOrder)
            .collect { items = it }
    }
}
