// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@Immutable
@DatabaseView("SELECT * FROM SongPlaylistMap ORDER BY position")
data class SortedSongPlaylistMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val playlistId: Long,
    val position: Int
)
