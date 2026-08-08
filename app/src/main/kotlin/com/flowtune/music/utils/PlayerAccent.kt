// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil3.imageLoader
import coil3.request.allowHardware
import coil3.request.ImageRequest
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalPlayerAccent = compositionLocalOf { Color.White }
val LocalPlayerAccentStrong = compositionLocalOf { Color.White }
val LocalPlayerAccentDark = compositionLocalOf { Color.White }

@Composable
fun rememberPlayerAccent(player: Player?): Triple<Color, Color, Color> {
    var artworkUri by remember { mutableStateOf<Uri?>(null) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                artworkUri = mediaItem?.mediaMetadata?.artworkUri
            }
        }

        artworkUri = player?.currentMediaItem?.mediaMetadata?.artworkUri
        player?.addListener(listener)
        onDispose {
            player?.removeListener(listener)
        }
    }

    var accent by remember { mutableStateOf<Color?>(null) }

    val context = LocalContext.current

    LaunchedEffect(artworkUri) {
        accent = null
        val uri = artworkUri ?: return@LaunchedEffect
        accent = withContext(Dispatchers.IO) {
            extractAccent(context = context, uri = uri)
        }
    }

    val accentValue = accent
    val strong = accentValue ?: Color.White
    val light = if (accentValue != null) lerp(accentValue, Color.White, 0.6f) else Color.White
    val dark = if (accentValue != null) lerp(accentValue, Color.Black, 0.7f) else Color.White
    return Triple(strong, light, dark)
}

private suspend fun extractAccent(context: Context, uri: Uri): Color? {
    return try {
        val request = ImageRequest.Builder(context)
            .data(uri)

            .size(128, 128)
            .allowHardware(false)
            .build()

        val result = context.imageLoader.execute(request)
        val bitmap = try {
            (result as? coil3.request.SuccessResult)?.image?.toBitmap()
        } catch (_: Exception) {
            null
        } ?: return null
        if (bitmap.isRecycled) return null
        dominantSaturatedColor(bitmap)?.let { Color(it) }
    } catch (_: Exception) {
        null
    }
}

private fun dominantSaturatedColor(bitmap: Bitmap): Int? {
    val width = bitmap.width
    val height = bitmap.height
    if (width == 0 || height == 0) return null

    val step = maxOf(1, minOf(width, height) / 64)

    val bucketWeights = FloatArray(size = 12)
    val bucketRed = FloatArray(size = 12)
    val bucketGreen = FloatArray(size = 12)
    val bucketBlue = FloatArray(size = 12)
    val bucketCounts = IntArray(size = 12)

    val hsv = FloatArray(size = 3)

    var y = 0
    while (y < height) {
        var x = 0
        while (x < width) {
            val pixel = bitmap.getPixel(x, y)
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            android.graphics.Color.colorToHSV(pixel, hsv)
            if (hsv[1] >= 0.25f && hsv[2] >= 0.25f) {
                val bucket = (((hsv[0] + 15f) % 360f) / 30f).toInt() % 12
                val weight = hsv[1] * hsv[2]
                bucketWeights[bucket] += weight
                bucketRed[bucket] += red * weight
                bucketGreen[bucket] += green * weight
                bucketBlue[bucket] += blue * weight
                bucketCounts[bucket] += 1
            }
            x += step
        }
        y += step
    }

    var best = -1
    var bestWeight = 0f
    for (i in 0 until 12) {
        if (bucketWeights[i] > bestWeight) {
            bestWeight = bucketWeights[i]
            best = i
        }
    }

    if (best == -1 || bucketCounts[best] == 0 || bucketWeights[best] <= 0f) return null

    val red = (bucketRed[best] / bucketWeights[best]).toInt().coerceIn(0, 255)
    val green = (bucketGreen[best] / bucketWeights[best]).toInt().coerceIn(0, 255)
    val blue = (bucketBlue[best] / bucketWeights[best]).toInt().coerceIn(0, 255)

    return android.graphics.Color.rgb(red, green, blue)
}
