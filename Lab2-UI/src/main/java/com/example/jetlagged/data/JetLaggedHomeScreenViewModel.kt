/*
 * Copyright 2023 The Android Open Source Project
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

package com.example.jetlagged.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetlagged.data.api.RetrofitClient
import com.example.jetlagged.sleep.SleepTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JetLaggedHomeScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(JetLaggedHomeScreenState())
    val uiState: StateFlow<JetLaggedHomeScreenState> = _uiState.asStateFlow()

    init {
        loadDataForTab(SleepTab.Week)
    }

    fun loadDataForTab(tab: SleepTab) {
        val rangeStr = when (tab) {
            SleepTab.Day -> "day"
            SleepTab.Month -> "month"
            else -> "week"
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getSleepData(rangeStr)
                _uiState.update { currentState ->
                    currentState.copy(
                        averageTimeInBed = response.averageTimeInBed,
                        averageSleepTime = response.averageSleepTime,
                        wellnessData = WellnessData(response.snoring, response.coughing, response.respiration),
                        sleepScore = response.sleepScore
                    )
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
    }
}
