// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import com.flowtune.music.R

object TopDestinations {
    val list = listOf(
        TopDestination(
            route = Routes.Home,
            resourceId = R.string.home,
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        TopDestination(
            route = Routes.Search,
            resourceId = R.string.search,
            unselectedIcon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search
        ),
        TopDestination(
            route = Routes.Playlists,
            resourceId = R.string.playlists,
            unselectedIcon = Icons.AutoMirrored.Outlined.QueueMusic,
            selectedIcon = Icons.AutoMirrored.Filled.QueueMusic
        )
    )

    val routes = list.map { it.route }
}
