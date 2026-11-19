/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetlagged.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.jetlagged.R
import com.example.jetlagged.ui.theme.HeadingStyle
import com.example.jetlagged.ui.theme.SmallHeadingStyle

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLanguage = if (LocalConfiguration.current.locales[0].language == "es") "es" else "en"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(id = R.string.settings_title),
            style = HeadingStyle,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Dark Mode setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.dark_mode_title), style = SmallHeadingStyle)
                Text(text = stringResource(id = R.string.dark_mode_desc), style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onDarkModeChanged(it) },
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Text(text = stringResource(id = R.string.language_title), style = SmallHeadingStyle)
            Text(text = stringResource(id = R.string.language_desc), style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = currentLanguage == "en",
                    onClick = { onLanguageChanged("en") },
                    label = { Text(text = stringResource(id = R.string.lang_english)) },
                )
                FilterChip(
                    selected = currentLanguage == "es",
                    onClick = { onLanguageChanged("es") },
                    label = { Text(text = stringResource(id = R.string.lang_spanish)) },
                )
            }
        }
    }
}
