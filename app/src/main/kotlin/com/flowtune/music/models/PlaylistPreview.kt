// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.models

import androidx.compose.runtime.Immutable

@Immutable
data class PlaylistPreview(
    val id: Long,
    val name: String,
    val songCount: Int
) {
    val playlist by lazy {
        Playlist(
            id = id,
            name = name
        )
    }
}
