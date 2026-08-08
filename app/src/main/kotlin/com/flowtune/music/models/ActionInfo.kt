// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class ActionInfo(
    val enabled: Boolean = true,
    val onClick: () -> Unit,
    val icon: ImageVector,
    @StringRes val description: Int
)
