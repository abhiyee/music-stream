// Flowtune Music by abhiram79
// github.com/abhiram79

package com.github.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val contents: Contents?,
) {
    @Serializable
    data class Contents(
        val tabbedSearchResultsRenderer: Tabs?
    )
}
