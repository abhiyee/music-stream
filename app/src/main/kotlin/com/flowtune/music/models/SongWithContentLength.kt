// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded

@Immutable
data class SongWithContentLength(
    @Embedded val song: Song,
    val contentLength: Long?
)
