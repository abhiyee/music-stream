// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtune.music.enums.SettingsSection
import com.flowtune.music.utils.LocalPlayerAccent

@Composable
fun SettingsPage(
    section: SettingsSection,
    pop: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (section != SettingsSection.About && section != SettingsSection.Cache) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LocalPlayerAccent.current)
                            .clickable(onClick = pop),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(id = section.resourceId),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                when (section) {
                    SettingsSection.General -> GeneralSettings()
                    SettingsSection.Player -> PlayerSettings()
                    SettingsSection.Gestures -> GestureSettings()
                    SettingsSection.Cache -> CacheSettings()
                    SettingsSection.Database -> DatabaseSettings()
                    SettingsSection.Other -> OtherSettings()
                    SettingsSection.About -> About()
                }
            }
        }
    }
}
