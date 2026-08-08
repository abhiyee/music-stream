// Flowtune Music by abhiram79
// github.com/abhiram79

package com.github.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class GetQueueResponse(
    val queueDatas: List<QueueData>?,
) {
    @Serializable
    data class QueueData(
        val content: NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer.Content?
    )
}
