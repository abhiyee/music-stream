// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flowtune.music.database
import com.flowtune.music.enums.ArtistSortBy
import com.flowtune.music.enums.SortOrder
import com.flowtune.music.models.Artist

class HomeArtistsViewModel : ViewModel() {
    var items: List<Artist> by mutableStateOf(emptyList())

    suspend fun loadArtists(
        sortBy: ArtistSortBy,
        sortOrder: SortOrder
    ) {
        database
            .artists(sortBy, sortOrder)
            .collect { items = it }
    }
}
