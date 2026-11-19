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
package com.example.racetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.racetracker.R
import com.example.racetracker.ui.theme.RaceTrackerTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed class RaceScreenRoute(val route: String) {
    object Setup : RaceScreenRoute("setup")
    object Race : RaceScreenRoute("race")
}

@Composable
fun RaceTrackerApp() {
    val navController = rememberNavController()
    var trackLength by remember { mutableStateOf(100) }
    val participants = remember {
        mutableStateListOf(
            RaceParticipant(name = "Corredor 1", color = ParticipantColor.ROJO, speedMps = 2, maxProgress = 100),
            RaceParticipant(name = "Corredor 2", color = ParticipantColor.AZUL, speedMps = 3, maxProgress = 100)
        )
    }
    var raceInProgress by remember { mutableStateOf(false) }

    if (raceInProgress) {
        LaunchedEffect(participants.toList(), raceInProgress) {
            coroutineScope {
                participants.forEach { participant ->
                    launch { participant.run() }
                }
            }
            raceInProgress = false
        }
    }

    val modifier = Modifier
        .statusBarsPadding()
        .fillMaxSize()
        .safeDrawingPadding()
        .padding(horizontal = dimensionResource(R.dimen.padding_medium))

    NavHost(
        navController = navController,
        startDestination = RaceScreenRoute.Setup.route
    ) {
        composable(RaceScreenRoute.Setup.route) {
            CompetitorSetupScreen(
                trackLength = trackLength,
                onTrackLengthChange = { newLength ->
                    trackLength = newLength
                    participants.forEach { it.maxProgress = newLength }
                },
                participants = participants,
                onAddParticipant = {
                    if (participants.size < 5) {
                        val availableColors = ParticipantColor.values()
                        val nextColor = availableColors[participants.size % availableColors.size]
                        participants.add(
                            RaceParticipant(
                                name = "Corredor ${participants.size + 1}",
                                color = nextColor,
                                speedMps = 2,
                                maxProgress = trackLength
                            )
                        )
                    }
                },
                onRemoveParticipant = { index ->
                    if (participants.size > 1) {
                        participants.removeAt(index)
                    }
                },
                onStartRace = {
                    participants.forEach {
                        it.maxProgress = trackLength
                        it.reset()
                    }
                    raceInProgress = false
                    navController.navigate(RaceScreenRoute.Race.route)
                },
                modifier = modifier
            )
        }

        composable(RaceScreenRoute.Race.route) {
            RaceTrackerScreen(
                trackLength = trackLength,
                participants = participants,
                isRunning = raceInProgress,
                onRunStateChange = { raceInProgress = it },
                onReset = {
                    participants.forEach { it.reset() }
                    raceInProgress = false
                },
                onNavigateToSetup = {
                    raceInProgress = false
                    navController.popBackStack()
                },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompetitorSetupScreen(
    trackLength: Int,
    onTrackLengthChange: (Int) -> Unit,
    participants: List<RaceParticipant>,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (Int) -> Unit,
    onStartRace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackLengthOptions = listOf(20, 50, 100, 200)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        Text(
            text = stringResource(R.string.setup_competitors),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Longitud de pista
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                Text(
                    text = stringResource(R.string.track_length_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    trackLengthOptions.forEach { length ->
                        FilterChip(
                            selected = (trackLength == length),
                            onClick = { onTrackLengthChange(length) },
                            label = { Text(stringResource(R.string.meters_unit, length)) }
                        )
                    }
                }
            }
        }

        // Encabezado de competidores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Competidores (${participants.size}/5)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddParticipant,
                enabled = participants.size < 5
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(stringResource(R.string.add_competitor))
            }
        }

        // Lista de tarjetas de competidores
        participants.forEachIndexed { index, participant ->
            CompetitorCard(
                index = index,
                participant = participant,
                canDelete = participants.size > 1,
                onDelete = { onRemoveParticipant(index) }
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

        Button(
            onClick = onStartRace,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.start_race),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
    }
}

@Composable
private fun CompetitorCard(
    index: Int,
    participant: RaceParticipant,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.competitor_number, index + 1),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.remove_competitor),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Nombre
            OutlinedTextField(
                value = participant.name,
                onValueChange = { participant.name = it },
                label = { Text(stringResource(R.string.competitor_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de Color
            Text(
                text = stringResource(R.string.competitor_color_label),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ParticipantColor.values().forEach { participantColor ->
                    val isSelected = (participant.color == participantColor)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(participantColor.color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else Modifier
                            )
                            .clickable { participant.color = participantColor },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = participantColor.displayName,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Velocidad Slider
            Column {
                Text(
                    text = stringResource(R.string.speed_label, participant.speedMps),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = participant.speedMps.toFloat(),
                    onValueChange = { participant.speedMps = it.roundToInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RaceTrackerScreen(
    trackLength: Int,
    participants: List<RaceParticipant>,
    isRunning: Boolean,
    onRunStateChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        Text(
            text = stringResource(R.string.run_a_race),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Pista: ${trackLength}m | Competidores: ${participants.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Icon(
            painter = painterResource(R.drawable.ic_walk),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(R.dimen.padding_small))
                .size(48.dp)
        )

        // Lista de indicadores para cada competidor
        participants.forEach { participant ->
            StatusIndicator(
                participant = participant,
                maxTrackMeters = trackLength,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_small)))
        }

        RaceControls(
            isRunning = isRunning,
            onRunStateChange = onRunStateChange,
            onReset = onReset,
            onNavigateToSetup = onNavigateToSetup,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
    }
}

@Composable
private fun StatusIndicator(
    participant: RaceParticipant,
    maxTrackMeters: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = participant.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${participant.speedMps} m/s",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = participant.progressFactor,
            color = participant.color.color,
            trackColor = participant.color.color.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.progress_indicator_height))
                .clip(RoundedCornerShape(dimensionResource(R.dimen.progress_indicator_corner_radius)))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(
                    R.string.progress_meters,
                    participant.currentProgress.toInt(),
                    maxTrackMeters
                ),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${(participant.progressFactor * 100).toInt()}%",
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RaceControls(
    isRunning: Boolean,
    onRunStateChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = dimensionResource(R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        Button(
            onClick = { onRunStateChange(!isRunning) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRunning) stringResource(R.string.pause) else stringResource(R.string.start))
        }
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset))
        }
        TextButton(
            onClick = onNavigateToSetup,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back_to_setup))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RaceTrackerAppPreview() {
    RaceTrackerTheme {
        RaceTrackerApp()
    }
}
