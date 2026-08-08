// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtune.music.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    snackbarHost: @Composable (() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    openSettings: () -> Unit,
    content: @Composable (() -> Unit)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FLOWTUNE",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black
                        ),
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                },
                actions = {
                    TooltipIconButton(
                        description = R.string.settings,
                        onClick = openSettings,
                        icon = Icons.Outlined.Settings,
                        inTopBar = true
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier.height(96.dp)
            )
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets()
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            content = content
        )
    }
}
