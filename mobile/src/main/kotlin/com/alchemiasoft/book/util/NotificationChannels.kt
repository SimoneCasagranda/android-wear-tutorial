/*
 * Copyright 2015 Simone Casagranda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.alchemiasoft.book.util

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alchemiasoft.book.R

/**
 * Notification channels required since Android 8.0 (API 26).
 */
object NotificationChannels {

    /**
     * Channel used for book suggestions and purchase/sell confirmations.
     */
    const val BOOKS = "books"

    /**
     * Creates the app's channels. Safe to call multiple times.
     */
    fun ensureCreated(context: Context) {
        val channel = NotificationChannelCompat.Builder(BOOKS, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.app_name))
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    /**
     * Posts the notification, unless the user denied the POST_NOTIFICATIONS permission (Android 13+).
     */
    fun notify(context: Context, id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
