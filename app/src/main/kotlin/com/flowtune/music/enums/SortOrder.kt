// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.enums

enum class SortOrder {
    Ascending,
    Descending;

    operator fun not() = when (this) {
        Ascending -> Descending
        Descending -> Ascending
    }
}
