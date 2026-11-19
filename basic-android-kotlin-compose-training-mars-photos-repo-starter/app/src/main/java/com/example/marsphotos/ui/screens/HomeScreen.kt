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
package com.example.marsphotos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.model.MarsPhoto

@Composable
fun HomeScreen(
    marsUiState: MarsUiState,
    isFavorite: (MarsPhoto) -> Boolean,
    onToggleFavorite: (MarsPhoto) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    columns: Int = 2,
    onPhotoClick: ((MarsPhoto) -> Unit)? = null
) {
    when (marsUiState) {
        is MarsUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
        is MarsUiState.Success -> HomeSuccessContent(
            photos = marsUiState.photos,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            columns = columns,
            onPhotoClick = onPhotoClick
        )
        is MarsUiState.Error -> ErrorScreen(
            retryAction = onRetry,
            modifier = modifier.fillMaxSize()
        )
    }
}

/**
 * Success content including the ID search bar and photo grid.
 */
@Composable
fun HomeSuccessContent(
    photos: List<MarsPhoto>,
    isFavorite: (MarsPhoto) -> Boolean,
    onToggleFavorite: (MarsPhoto) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    columns: Int = 2,
    onPhotoClick: ((MarsPhoto) -> Unit)? = null
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredPhotos = photos.filter { photo ->
        photo.id.contains(searchQuery.trim(), ignoreCase = true)
    }

    Column(
        modifier = modifier.padding(contentPadding)
    ) {
        // Search bar (only shown on Home screen, only accepts numeric digits)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    searchQuery = newValue
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(text = stringResource(R.string.search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        if (filteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.search_no_results),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            PhotosGridScreen(
                photos = filteredPhotos,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
                columns = columns,
                onPhotoClick = onPhotoClick
            )
        }
    }
}

/**
 * The home screen displaying the loading message.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.loading)
        )
    }
}

/**
 * The home screen displaying error message with re-attempt button.
 */
@Composable
fun ErrorScreen(
    retryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = null
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * Grid displaying the list of Mars photos with configurable column count (2 or 3).
 */
@Composable
fun PhotosGridScreen(
    photos: List<MarsPhoto>,
    isFavorite: (MarsPhoto) -> Boolean,
    onToggleFavorite: (MarsPhoto) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    columns: Int = 2,
    onPhotoClick: ((MarsPhoto) -> Unit)? = null
) {
    val spacing = if (columns > 2) 8.dp else 12.dp
    val paddingHorizontal = if (columns > 2) 8.dp else 12.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.padding(horizontal = paddingHorizontal),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(items = photos, key = { photo -> photo.id }) { photo ->
            MarsPhotoCard(
                photo = photo,
                isFavorite = isFavorite(photo),
                onToggleFavorite = { onToggleFavorite(photo) },
                columns = columns,
                onPhotoClick = onPhotoClick
            )
        }
    }
}

/**
 * Card item for each Mars photo with image, ID label, favorite button, and configurable columns support.
 */
@Composable
fun MarsPhotoCard(
    photo: MarsPhoto,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    onPhotoClick: ((MarsPhoto) -> Unit)? = null
) {
    val isCompact = columns > 2
    val cardCorner = if (isCompact) 10.dp else 16.dp
    val elevation = if (isCompact) 3.dp else 6.dp
    val iconSize = if (isCompact) 28.dp else 36.dp
    val textStyle = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardCorner))
            .then(
                if (onPhotoClick != null) {
                    Modifier.clickable { onPhotoClick(photo) }
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(cardCorner)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(photo.imgSrc)
                    .crossfade(true)
                    .build(),
                error = painterResource(R.drawable.ic_broken_image),
                placeholder = painterResource(R.drawable.loading_img),
                contentDescription = stringResource(R.string.mars_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
                    .padding(horizontal = if (isCompact) 6.dp else 12.dp, vertical = if (isCompact) 4.dp else 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ID: ${photo.id}",
                        style = textStyle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(iconSize)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                            tint = if (isFavorite) Color(0xFFE91E63) else Color.White
                        )
                    }
                }
            }
        }
    }
}
