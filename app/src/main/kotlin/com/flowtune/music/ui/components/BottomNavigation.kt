// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.flowtune.music.enums.NavigationLabelsVisibility
import com.flowtune.music.ui.navigation.Routes
import com.flowtune.music.ui.navigation.TopDestinations
import com.flowtune.music.utils.navigationLabelsVisibilityKey
import com.flowtune.music.utils.rememberPreference

@Composable
fun BottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isSearchScreen = currentDestination?.hierarchy?.any {
        it.hasRoute(route = Routes.Search::class)
    } == true
    var navigationLabelsVisibility by rememberPreference(
        navigationLabelsVisibilityKey,
        NavigationLabelsVisibility.Visible
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = if (isSearchScreen) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black
                        )
                    )
                }
            )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            TopDestinations.list.forEachIndexed { index, destination ->
                val selected =
                    currentDestination?.hierarchy?.any { it.hasRoute(route = destination.route::class) } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(route = destination.route) {
                                popUpTo(id = navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = stringResource(id = destination.resourceId),
                            tint = Color.White.copy(alpha = if (selected) 1f else 0.6f)
                        )
                    },
                    label = {
                        if (navigationLabelsVisibility != NavigationLabelsVisibility.Hidden) {
                            Text(
                                text = stringResource(id = destination.resourceId),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White.copy(alpha = if (selected) 1f else 0.6f)
                            )
                        }
                    },
                    alwaysShowLabel = navigationLabelsVisibility != NavigationLabelsVisibility.VisibleWhenActive,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.White.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
