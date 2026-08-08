// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.ui.screens.search

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flowtune.music.R

internal val SearchPillShape = RoundedCornerShape(percent = 50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchInputField(
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh

    SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = onSearch,
        placeholder = {
            Text(text = stringResource(id = R.string.search))
        },
        modifier = modifier,
        shape = SearchPillShape,
        colors = SearchBarDefaults.inputFieldColors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        trailingIcon = {
            if (textFieldState.text.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = null
                    )
                }
            }
        }
    )
}
