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

package com.example.jetlagged.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jetlagged.data.api.RetrofitClient

class SleepSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        return try {
            Log.d("SleepSyncWorker", "Starting background sync of sleep data...")
            val response = RetrofitClient.apiService.getSleepData("week")
            Log.d("SleepSyncWorker", "Successfully synced sleep data: $response")
            androidx.work.ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("SleepSyncWorker", "Failed to sync sleep data", e)
            androidx.work.ListenableWorker.Result.retry()
        }
    }
}
