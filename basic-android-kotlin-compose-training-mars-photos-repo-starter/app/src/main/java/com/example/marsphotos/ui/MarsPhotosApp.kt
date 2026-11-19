/*
 * Copyright (C) 2023 The Android Open Source Project
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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.marsphotos.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.R
import com.example.marsphotos.ui.screens.FavoritesScreen
import com.example.marsphotos.ui.screens.HomeScreen
import com.example.marsphotos.ui.screens.MarsViewModel

/**
 * Navigation screens enum for Mars Photos app.
 */
enum class MarsAppScreen(
    @StringRes val title: Int,
    val icon: ImageVector
) {
    HOME(
        title = R.string.tab_home,
        icon = Icons.Default.Home
    ),
    FAVORITES(
        title = R.string.tab_favorites,
        icon = Icons.Default.Favorite
    )
}

@Composable
fun MarsPhotosApp() {
    val marsViewModel: MarsViewModel = viewModel()
    var currentScreen by rememberSaveable { mutableStateOf(MarsAppScreen.HOME) }
    var gridColumns by rememberSaveable { mutableStateOf(2) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MarsTopAppBar(
                currentScreen = currentScreen,
                scrollBehavior = scrollBehavior,
                columns = gridColumns,
                onToggleColumns = {
                    gridColumns = if (gridColumns == 2) 3 else 2
                }
            )
        },
        bottomBar = {
            MarsBottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it },
                favoriteCount = marsViewModel.favoritePhotos.size
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentScreen) {
                MarsAppScreen.HOME -> HomeScreen(
                    marsUiState = marsViewModel.marsUiState,
                    isFavorite = { marsViewModel.isFavorite(it) },
                    onToggleFavorite = { marsViewModel.toggleFavorite(it) },
                    onRetry = { marsViewModel.getMarsPhotos() },
                    contentPadding = innerPadding,
                    columns = gridColumns
                )
                MarsAppScreen.FAVORITES -> FavoritesScreen(
                    favoritePhotos = marsViewModel.favoritePhotos,
                    onToggleFavorite = { marsViewModel.toggleFavorite(it) },
                    contentPadding = innerPadding,
                    columns = gridColumns
                )
            }
        }
    }
}

/**
 * Top App Bar displaying screen title and grid columns toggle action button.
 */
@Composable
fun MarsTopAppBar(
    currentScreen: MarsAppScreen,
    scrollBehavior: TopAppBarScrollBehavior,
    columns: Int,
    onToggleColumns: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(
                    if (currentScreen == MarsAppScreen.HOME) R.string.app_name else R.string.favorites_title
                ),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        actions = {
            IconButton(onClick = onToggleColumns) {
                Text(
                    text = if (columns == 2) "3x" else "2x",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Bottom Navigation Bar for switching between Home and Favorites screens using Jetpack Compose.
 */
@Composable
fun MarsBottomNavigationBar(
    currentScreen: MarsAppScreen,
    onScreenSelected: (MarsAppScreen) -> Unit,
    favoriteCount: Int,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        MarsAppScreen.values().forEach { screen ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onScreenSelected(screen) },
                label = { Text(text = stringResource(screen.title)) },
                icon = {
                    if (screen == MarsAppScreen.FAVORITES && favoriteCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(text = favoriteCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.title)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(screen.title)
                        )
                    }
                }
            )
        }
    }
}
