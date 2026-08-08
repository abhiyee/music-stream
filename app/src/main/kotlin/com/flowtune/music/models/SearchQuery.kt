// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    indices = [
        Index(
            value = ["query"],
            unique = true
        )
    ]
)
data class SearchQuery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String
)
