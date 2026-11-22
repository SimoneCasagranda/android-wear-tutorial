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


package com.alchemiasoft.book.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.alchemiasoft.book.R
import com.alchemiasoft.book.activity.HomeActivity
import com.alchemiasoft.book.util.NotificationChannels
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book

/**
 * JobIntentService that takes care of suggesting the user to buy a book that he doesn't already own.
 *
 * Created by Simone Casagranda on 27/12/14.
 */
@Suppress("DEPRECATION") // JobIntentService is deprecated in favour of WorkManager, but still works.
class SuggestionService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        Log.d(TAG_LOG, "Starting a new book suggestion...")
        val book = contentResolver.query(BookDB.Book.CONTENT_URI, null, SELECTION, SELECT_NOT_OWNED, null)?.use { c ->
            if (c.moveToNext()) Book.oneFrom(c) else null
        } ?: return
        // Showing a notification for the not owned book that has been found
        Log.d(TAG_LOG, "Found book that can be suggested: $book")
        val content = getString(R.string.content_book_suggestion, book.title, book.author)
        NotificationChannels.ensureCreated(this)
        val builder = NotificationCompat.Builder(this, NotificationChannels.BOOKS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setContentTitle(getString(R.string.title_book_suggestion))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(
                PendingIntent.getActivity(this, 0, HomeActivity.createFor(this, book), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

        // ONLY 4 WEARABLE(s)
        val wearableExtender = NotificationCompat.WearableExtender()
        // SECOND PAGE WITH BOOK DESCRIPTION
        wearableExtender.addPage(
            NotificationCompat.Builder(this, NotificationChannels.BOOKS)
                .setContentTitle(getString(R.string.description))
                .setStyle(NotificationCompat.BigTextStyle().bigText(book.description))
                .build()
        )
        wearableExtender.setBackground(BitmapFactory.decodeResource(resources, R.drawable.background))
        // ACTION TO PURCHASE A BOOK FROM A WEARABLE
        val purchaseIntent = PendingIntent.getService(
            this, 1, BookActionService.IntentBuilder.buy(this, book).notificationId(ID_SUGGESTION).wearableInput().build(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        wearableExtender.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_buy, getString(R.string.action_buy), purchaseIntent).build())
        // ACTION TO ADD NOTES VIA VOICE REPLY (must be mutable to receive the reply)
        val input = BookActionService.RemoteInputBuilder.create(this).options(R.array.note_options).build()
        val notesIntent = PendingIntent.getService(
            this, 2, BookActionService.IntentBuilder.addNote(this, book).notificationId(ID_SUGGESTION).wearableInput().build(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        wearableExtender.addAction(
            NotificationCompat.Action.Builder(R.drawable.ic_action_notes, getString(R.string.action_notes), notesIntent).addRemoteInput(input).build()
        )
        // Finally extending the notification
        builder.extend(wearableExtender)

        // Sending the notification
        NotificationChannels.notify(this, ID_SUGGESTION, builder.build())
    }

    companion object {

        private val TAG_LOG = SuggestionService::class.java.simpleName

        private const val ID_SUGGESTION = 23

        private const val JOB_ID = 1023

        private const val SELECTION = BookDB.Book.OWNED + " = ?"
        private val SELECT_NOT_OWNED = arrayOf("0")

        fun enqueue(context: Context) {
            enqueueWork(context, SuggestionService::class.java, JOB_ID, Intent(context, SuggestionService::class.java))
        }
    }
}
