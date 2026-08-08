// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class Album(
    @PrimaryKey val id: String,
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val year: String? = null,
    val authorsText: String? = null,
    val shareUrl: String? = null,
    val timestamp: Long? = null,
    val bookmarkedAt: Long? = null
)
