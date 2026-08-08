// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.enums

import androidx.annotation.StringRes
import com.flowtune.music.R

enum class NavigationLabelsVisibility(
    @StringRes val resourceId: Int
) {
    Visible(resourceId = R.string.visible),
    VisibleWhenActive(resourceId = R.string.visible_when_active),
    Hidden(resourceId = R.string.hidden)
}
