// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flowtune.music.database
import com.flowtune.music.enums.PlaylistSortBy
import com.flowtune.music.enums.SortOrder
import com.flowtune.music.models.PlaylistPreview

class HomePlaylistsViewModel : ViewModel() {
    var items: List<PlaylistPreview> by mutableStateOf(emptyList())

    suspend fun loadArtists(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ) {
        database
            .playlistPreviews(sortBy, sortOrder)
            .collect { items = it }
    }
}
