// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.annotation.OptIn
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Battery0Bar
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.flowtune.music.LocalPlayerPadding
import com.flowtune.music.LocalPlayerServiceBinder
import com.flowtune.music.R
import com.flowtune.music.database
import com.flowtune.music.database.DatabaseDao
import com.flowtune.music.enums.QuickPicksSource
import com.flowtune.music.enums.SettingsSection
import com.flowtune.music.ui.components.ValueSelectorDialog
import com.flowtune.music.ui.styling.Dimensions
import com.flowtune.music.utils.isIgnoringBatteryOptimizations
import com.flowtune.music.utils.pauseSearchHistoryKey
import com.flowtune.music.utils.quickPicksSourceKey
import com.flowtune.music.utils.rememberPreference
import com.flowtune.music.utils.skipSilenceKey
import com.flowtune.music.utils.toast
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(UnstableApi::class)
@Composable
fun SettingsScreen(
    pop: () -> Unit,
    onGoToSettingsPage: (Int) -> Unit
) {
    val playerPadding = LocalPlayerPadding.current
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current

    var quickPicksSource by rememberPreference(quickPicksSourceKey, QuickPicksSource.Trending)
    var skipSilence by rememberPreference(skipSilenceKey, false)
    var pauseSearchHistory by rememberPreference(pauseSearchHistoryKey, false)

    val queriesCount by remember {
        database.queriesCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    val eventsCount by remember {
        database.eventsCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }
    val batteryOptimizationsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
        }

    val equalizerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = playerPadding)
            ) {
                SettingsEntry(
                    title = stringResource(id = R.string.about),
                    text = "",
                    icon = Icons.Outlined.Info,
                    onClick = { onGoToSettingsPage(SettingsSection.About.ordinal) }
                )

                SectionHeading(text = stringResource(id = R.string.general_settings))

                EnumValueSelectorSettingsEntry(
                    title = stringResource(id = R.string.quick_picks_source),
                    selectedValue = quickPicksSource,
                    onValueSelected = { quickPicksSource = it },
                    icon = Icons.AutoMirrored.Outlined.List,
                    valueText = { stringResource(id = it.resourceId) }
                )

                SectionHeading(text = stringResource(id = R.string.player))

                SwitchSettingEntry(
                    title = stringResource(id = R.string.skip_silence),
                    text = stringResource(id = R.string.skip_silence_description),
                    icon = Icons.Outlined.FastForward,
                    isChecked = skipSilence,
                    onCheckedChange = { skipSilence = it }
                )

                SettingsEntry(
                    title = stringResource(id = R.string.equalizer),
                    text = stringResource(id = R.string.equalizer_description),
                    icon = Icons.Outlined.Equalizer,
                    onClick = {
                        val intent = Intent(
                            AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL
                        ).apply {
                            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder?.player?.audioSessionId)
                            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                            putExtra(
                                AudioEffect.EXTRA_CONTENT_TYPE,
                                AudioEffect.CONTENT_TYPE_MUSIC
                            )
                        }

                        try {
                            equalizerLauncher.launch(intent)
                        } catch (_: ActivityNotFoundException) {
                            context.toast("Couldn't find an application to equalize audio")
                        }
                    }
                )

                SectionHeading(text = stringResource(id = R.string.cache))

                SettingsEntry(
                    title = stringResource(id = R.string.cache_management),
                    text = stringResource(id = R.string.cache_management_description),
                    icon = Icons.Outlined.History,
                    onClick = { onGoToSettingsPage(SettingsSection.Cache.ordinal) }
                )

                SectionHeading(text = stringResource(id = R.string.database_management))

                SwitchSettingEntry(
                    title = stringResource(id = R.string.pause_search_history),
                    text = stringResource(id = R.string.pause_search_history_description),
                    icon = Icons.Outlined.HistoryToggleOff,
                    isChecked = pauseSearchHistory,
                    onCheckedChange = { pauseSearchHistory = it }
                )

                SettingsEntry(
                    title = stringResource(id = R.string.clear_search_history),
                    text = if (queriesCount > 0) {
                        stringResource(id = R.string.delete_search_queries, queriesCount)
                    } else {
                        stringResource(id = R.string.history_is_empty)
                    },
                    icon = Icons.Outlined.DeleteSweep,
                    onClick = { database.query(DatabaseDao::clearQueries) },
                    isEnabled = queriesCount > 0
                )

                SettingsEntry(
                    title = stringResource(id = R.string.reset_quick_picks),
                    text = if (eventsCount > 0) {
                        stringResource(id = R.string.delete_playback_events, eventsCount)
                    } else {
                        stringResource(id = R.string.quick_picks_cleared)
                    },
                    icon = Icons.Outlined.RestartAlt,
                    onClick = { database.query(DatabaseDao::clearEvents) },
                    isEnabled = eventsCount > 0
                )

                SectionHeading(text = stringResource(id = R.string.battery))

                SettingsEntry(
                    title = stringResource(id = R.string.ignore_battery_optimizations),
                    text = if (isIgnoringBatteryOptimizations) {
                        stringResource(id = R.string.already_unrestricted)
                    } else {
                        stringResource(id = R.string.disable_background_restrictions)
                    },
                    icon = Icons.Outlined.Battery0Bar,
                    onClick = {
                        try {
                            batteryOptimizationsLauncher.launch(
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                            )
                        } catch (_: ActivityNotFoundException) {
                            try {
                                batteryOptimizationsLauncher.launch(
                                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                )
                            } catch (_: ActivityNotFoundException) {
                                context.toast("Couldn't find battery optimization settings, please whitelist FlowTune manually")
                            }
                        }
                    },
                    isEnabled = !isIgnoringBatteryOptimizations
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.1.sp
    )
}

@Composable
inline fun <reified T : Enum<T>> EnumValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    crossinline onValueSelected: (T) -> Unit,
    icon: ImageVector,
    isEnabled: Boolean = true,
    crossinline valueText: @Composable (T) -> String,
    noinline trailingContent: @Composable (() -> Unit)? = null
) {
    ValueSelectorSettingsEntry(
        title = title,
        selectedValue = selectedValue,
        values = enumValues<T>().toList(),
        onValueSelected = onValueSelected,
        icon = icon,
        isEnabled = isEnabled,
        valueText = valueText,
        trailingContent = trailingContent,
    )
}

@Composable
inline fun <T> ValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    values: List<T>,
    crossinline onValueSelected: (T) -> Unit,
    icon: ImageVector,
    isEnabled: Boolean = true,
    crossinline valueText: @Composable (T) -> String = { it.toString() },
    noinline trailingContent: @Composable (() -> Unit)? = null
) {
    var isShowingDialog by remember { mutableStateOf(false) }

    if (isShowingDialog) {
        ValueSelectorDialog(
            onDismiss = { isShowingDialog = false },
            title = title,
            selectedValue = selectedValue,
            values = values,
            onValueSelected = onValueSelected,
            valueText = valueText
        )
    }

    SettingsEntry(
        title = title,
        text = valueText(selectedValue),
        icon = icon,
        onClick = { isShowingDialog = true },
        isEnabled = isEnabled,
        trailingContent = trailingContent ?: {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
fun SwitchSettingEntry(
    title: String,
    text: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean = true
) {
    SettingsEntry(
        title = title,
        text = text,
        icon = icon,
        onClick = { onCheckedChange(!isChecked) },
        isEnabled = isEnabled
    ) {
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = isEnabled
        )
    }
}

@Composable
fun SettingsEntry(
    title: String,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .alpha(if (isEnabled) 1F else Dimensions.lowOpacity)
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

                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            trailingContent?.invoke()
        }
    }
}

@Composable
fun SettingsInformation(
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SettingsProgress(
    text: String,
    progress: Float,
    actionButton: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.width(240.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            )
        }

        actionButton()
    }
}
