// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flowtune.music.database
import com.flowtune.music.enums.SongSortBy
import com.flowtune.music.enums.SortOrder
import com.flowtune.music.models.Song

class HomeSongsViewModel : ViewModel() {
    var items: List<Song> by mutableStateOf(emptyList())

    suspend fun loadSongs(
        sortBy: SongSortBy,
        sortOrder: SortOrder
    ) {
        database
            .songs(sortBy, sortOrder)
            .collect { items = it }
    }
}
