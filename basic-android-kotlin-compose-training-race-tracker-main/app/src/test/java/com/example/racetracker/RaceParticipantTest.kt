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
package com.example.racetracker

import com.example.racetracker.ui.ParticipantColor
import com.example.racetracker.ui.RaceParticipant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RaceParticipantTest {
    private val raceParticipant = RaceParticipant(
        name = "Test",
        color = ParticipantColor.ROJO,
        speedMps = 2,
        maxProgress = 100,
        progressDelayMillis = 100L,
        initialProgress = 0f
    )

    @Test
    fun raceParticipant_RaceStarted_ProgressUpdated() = runTest {
        val expectedProgress = 0.2f // 2 m/s * 0.1s
        launch { raceParticipant.run() }
        advanceTimeBy(raceParticipant.progressDelayMillis)
        runCurrent()
        assertEquals(expectedProgress, raceParticipant.currentProgress, 0.01f)
    }

    @Test
    fun raceParticipant_RaceFinished_ProgressUpdated() = runTest {
        launch { raceParticipant.run() }
        // To cover 100m at 2m/s takes 50 seconds (50,000 ms)
        advanceTimeBy(50_000L)
        runCurrent()
        assertEquals(100f, raceParticipant.currentProgress, 0.01f)
    }

    @Test
    fun raceParticipant_RacePaused_ProgressUpdated() = runTest {
        val racerJob = launch { raceParticipant.run() }
        advanceTimeBy(5 * raceParticipant.progressDelayMillis) // 500ms = 0.5s -> 2 * 0.5 = 1.0m
        runCurrent()
        racerJob.cancelAndJoin()
        assertEquals(1.0f, raceParticipant.currentProgress, 0.01f)
    }

    @Test
    fun raceParticipant_RacePausedAndResumed_ProgressUpdated() = runTest {
        repeat(2) {
            val racerJob = launch { raceParticipant.run() }
            advanceTimeBy(5 * raceParticipant.progressDelayMillis) // 1.0m per run
            runCurrent()
            racerJob.cancelAndJoin()
        }
        assertEquals(2.0f, raceParticipant.currentProgress, 0.01f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun raceParticipant_SpeedZero_ExceptionThrown() {
        RaceParticipant(name = "Invalid Speed Test", speedMps = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun raceParticipant_MaxProgressZero_ExceptionThrown() {
        RaceParticipant(name = "Progress Test", maxProgress = 0)
    }
}
