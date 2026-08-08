// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.settings

import android.text.format.Formatter
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import coil3.imageLoader
import com.flowtune.music.LocalPlayerPadding
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.enums.CoilDiskCacheMaxSize
import com.flowtune.music.enums.ExoPlayerDiskCacheMaxSize
import com.flowtune.music.ui.components.ConfirmationDialog
import com.flowtune.music.ui.components.ValueSelectorDialog
import com.flowtune.music.utils.coilDiskCacheMaxSizeKey
import com.flowtune.music.utils.exoPlayerDiskCacheMaxSizeKey
import com.flowtune.music.utils.rememberPreference

private val DeleteRed = Color(0xFFD32F2F)

@OptIn(UnstableApi::class)
@Composable
fun CacheSettings() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val playerPadding = LocalPlayerPadding.current

    val diskCache = context.imageLoader.diskCache
    var diskCacheSize by remember(diskCache) {
        mutableLongStateOf(diskCache?.size ?: 0L)
    }

    var coilDiskCacheMaxSize by rememberPreference(
        coilDiskCacheMaxSizeKey,
        CoilDiskCacheMaxSize.`128MB`
    )
    var exoPlayerDiskCacheMaxSize by rememberPreference(
        exoPlayerDiskCacheMaxSizeKey,
        ExoPlayerDiskCacheMaxSize.`2GB`
    )

    var isShowingImageCacheDialog by rememberSaveable { mutableStateOf(false) }
    var isShowingCoilMaxSizeDialog by rememberSaveable { mutableStateOf(false) }
    var isShowingExoPlayerMaxSizeDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp + playerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (diskCache != null) {
            CacheHeading(text = stringResource(id = R.string.image_cache))

            SettingsProgressCard(
                text = Formatter.formatShortFileSize(context, diskCacheSize),
                progress = diskCacheSize.toFloat() / coilDiskCacheMaxSize.bytes.coerceAtLeast(
                    minimumValue = 1
                ).toFloat()
            )

            Spacer(modifier = Modifier.height(12.dp))

            MaxSizePillCard(
                icon = Icons.Outlined.Image,
                title = stringResource(id = R.string.max_size),
                value = coilDiskCacheMaxSize.name,
                onClick = { isShowingCoilMaxSizeDialog = true }
            )
        }

        binder?.cache?.let { cache ->
            val songDiskCacheSize by remember {
                derivedStateOf {
                    cache.cacheSpace
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CacheHeading(text = stringResource(id = R.string.song_cache))

            SettingsProgressCard(
                text = Formatter.formatShortFileSize(context, songDiskCacheSize),
                progress = when (val size = exoPlayerDiskCacheMaxSize) {
                    ExoPlayerDiskCacheMaxSize.Unlimited -> 0F
                    else -> (songDiskCacheSize.toFloat() / size.bytes.toFloat())
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            MaxSizePillCard(
                icon = Icons.Outlined.MusicNote,
                title = stringResource(id = R.string.max_size),
                value = if (exoPlayerDiskCacheMaxSize == ExoPlayerDiskCacheMaxSize.Unlimited) {
                    stringResource(id = R.string.unlimited)
                } else {
                    exoPlayerDiskCacheMaxSize.name
                },
                onClick = { isShowingExoPlayerMaxSizeDialog = true }
            )
        }

        if (diskCache != null) {
            Spacer(modifier = Modifier.height(12.dp))

            DeleteActionPillCard(
                onClick = { isShowingImageCacheDialog = true },
                background = DeleteRed
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsInformation(text = stringResource(id = R.string.cache_information))
    }

    if (isShowingCoilMaxSizeDialog) {
        ValueSelectorDialog(
            onDismiss = { isShowingCoilMaxSizeDialog = false },
            title = stringResource(id = R.string.max_size),
            selectedValue = coilDiskCacheMaxSize,
            values = CoilDiskCacheMaxSize.entries,
            onValueSelected = { coilDiskCacheMaxSize = it }
        ) { it.name }
    }

    if (isShowingExoPlayerMaxSizeDialog) {
        ValueSelectorDialog(
            onDismiss = { isShowingExoPlayerMaxSizeDialog = false },
            title = stringResource(id = R.string.max_size),
            selectedValue = exoPlayerDiskCacheMaxSize,
            values = ExoPlayerDiskCacheMaxSize.entries,
            onValueSelected = { exoPlayerDiskCacheMaxSize = it }
        ) {
            if (it == ExoPlayerDiskCacheMaxSize.Unlimited) stringResource(id = R.string.unlimited)
            else it.name
        }
    }

    if (isShowingImageCacheDialog) {
        ConfirmationDialog(
            title = stringResource(id = R.string.delete_image_cache),
            onDismiss = { isShowingImageCacheDialog = false },
            onConfirm = {
                diskCache?.clear()
                diskCacheSize = 0
            }
        )
    }
}

@Composable
private fun CacheHeading(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.1.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SettingsProgressCard(
    text: String,
    progress: Float
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .height(8.dp)
            )
        }
    }
}

@Composable
private fun MaxSizePillCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeleteActionPillCard(
    onClick: () -> Unit,
    background: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = background,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.delete_image_cache),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stringResource(id = R.string.clear_cache),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
