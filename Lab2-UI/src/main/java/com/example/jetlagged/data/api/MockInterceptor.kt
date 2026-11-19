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

package com.example.jetlagged.data.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val uri = chain.request().url.toUri()
        val path = uri.path
        val query = uri.query

        if (path.contains("sleep-data")) {
            val range = if (query?.contains("range=day") == true) {
                "day"
            } else if (query?.contains("range=month") == true) {
                "month"
            } else {
                "week"
            }

            val json = when (range) {
                "day" -> """
                    {
                      "range": "day",
                      "averageTimeInBed": "7h 30min",
                      "averageSleepTime": "6h 45min",
                      "sleepScore": 82,
                      "snoring": 5,
                      "coughing": 1,
                      "respiration": 14
                    }
                """.trimIndent()
                "month" -> """
                    {
                      "range": "month",
                      "averageTimeInBed": "8h 15min",
                      "averageSleepTime": "7h 10min",
                      "sleepScore": 88,
                      "snoring": 8,
                      "coughing": 2,
                      "respiration": 12
                    }
                """.trimIndent()
                else -> """
                    {
                      "range": "week",
                      "averageTimeInBed": "8h 42min",
                      "averageSleepTime": "7h 42min",
                      "sleepScore": 90,
                      "snoring": 10,
                      "coughing": 4,
                      "respiration": 5
                    }
                """.trimIndent()
            }

            return Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(chain.request())
                .body(json.toResponseBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()
        }

        return chain.proceed(chain.request())
    }
}
