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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

/**
 * Predefined colors for race participants.
 */
enum class ParticipantColor(val displayName: String, val color: Color) {
    ROJO("Rojo", Color(0xFFE53935)),
    AZUL("Azul", Color(0xFF1E88E5)),
    AMARILLO("Amarillo", Color(0xFFFDD835)),
    VERDE("Verde", Color(0xFF4CAF50)),
    NARANJA("Naranja", Color(0xFFFB8C00)),
    MORADO("Morado", Color(0xFF8E24AA)),
    ROSADO("Rosado", Color(0xFFE91E63))
}

/**
 * This class represents a state holder for a race participant.
 */
class RaceParticipant(
    name: String,
    color: ParticipantColor = ParticipantColor.AZUL,
    speedMps: Int = 1,
    maxProgress: Int = 100,
    val progressDelayMillis: Long = 100L,
    initialProgress: Float = 0f
) {
    var name by mutableStateOf(name)
    var color by mutableStateOf(color)
    var speedMps by mutableStateOf(speedMps)
    var maxProgress by mutableStateOf(maxProgress)

    init {
        require(maxProgress > 0) { "maxProgress=$maxProgress; must be > 0" }
        require(speedMps in 1..10) { "speedMps=$speedMps; must be between 1 and 10 m/s" }
    }

    /**
     * Indicates the race participant's current progress in meters.
     */
    var currentProgress by mutableStateOf(initialProgress)
        private set

    /**
     * Updates the value of [currentProgress] based on [speedMps] until it reaches [maxProgress].
     * There is a delay of [progressDelayMillis] between each update.
     */
    suspend fun run() {
        val incrementPerDelay = speedMps * (progressDelayMillis / 1000f)
        while (currentProgress < maxProgress) {
            delay(progressDelayMillis)
            currentProgress = (currentProgress + incrementPerDelay).coerceAtMost(maxProgress.toFloat())
        }
    }

    /**
     * Resets the [currentProgress] to 0.
     */
    fun reset() {
        currentProgress = 0f
    }
}

/**
 * Calculates the progress factor (0.0f to 1.0f) for progress indicators.
 */
val RaceParticipant.progressFactor: Float
    get() = (currentProgress / maxProgress.toFloat()).coerceIn(0f, 1f)
