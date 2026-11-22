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


// The tutorial uses IntentService and the GoogleApiClient-based Wearable APIs (deprecated, still available).
@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.alchemiasoft.book.service

import android.app.IntentService
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.alchemiasoft.book.R
import com.alchemiasoft.book.activity.HomeActivity
import com.alchemiasoft.book.util.NotificationChannels
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import com.alchemiasoft.common.sync.Event
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit

/**
 * Service that allows to perform actions like purchase, sell and annotate books.
 *
 * Created by Simone Casagranda on 28/12/14.
 */
class BookActionService : IntentService(TAG_LOG) {

    /**
     * Available actions.
     */
    private enum class Action {
        BUY, SELL, ADD_NOTE
    }

    class IntentBuilder private constructor(context: Context, book: Book, action: Action) {

        private val mIntent = Intent(context, BookActionService::class.java)
            .putExtra(EXTRA_BOOK_ID, book.id)
            .setAction(action.name)

        fun notificationId(id: Int): IntentBuilder = apply { mIntent.putExtra(EXTRA_NOTIFICATION_ID, id) }

        fun wearableInput(): IntentBuilder = apply { mIntent.putExtra(EXTRA_WEARABLE_INPUT, true) }

        fun build(): Intent = mIntent

        companion object {
            fun buy(context: Context, book: Book): IntentBuilder = IntentBuilder(context, book, Action.BUY)

            fun sell(context: Context, book: Book): IntentBuilder = IntentBuilder(context, book, Action.SELL)

            fun addNote(context: Context, book: Book): IntentBuilder = IntentBuilder(context, book, Action.ADD_NOTE)
        }
    }

    class RemoteInputBuilder private constructor(context: Context, label: String) {

        private val mContext: Context = context.applicationContext
        private val mBuilder: RemoteInput.Builder = RemoteInput.Builder(EXTRA_ADD_NOTE).setLabel(label)

        fun options(options: Int): RemoteInputBuilder = apply { mBuilder.setChoices(mContext.resources.getStringArray(options)) }

        fun build(): RemoteInput = mBuilder.build()

        companion object {
            fun create(context: Context): RemoteInputBuilder = RemoteInputBuilder(context, context.getString(R.string.add_notes))
        }
    }

    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate() {
        super.onCreate()
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = GoogleApiClient.Builder(this).addApi(Wearable.API).build()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Always disconnect the client
        mGoogleApiClient?.disconnect()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            return
        }
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, NOT_VALID_NOTIFICATION)
        // Cancelling any shown notification
        if (notificationId != NOT_VALID_NOTIFICATION) {
            Log.d(TAG_LOG, "Dismissing notification with id=$notificationId")
            NotificationManagerCompat.from(this).cancel(notificationId)
        }
        val bookId = intent.getLongExtra(EXTRA_BOOK_ID, NOT_VALID_BOOK)
        if (bookId == NOT_VALID_BOOK) {
            return
        }
        val action = Action.valueOf(intent.action!!)
        Log.d(TAG_LOG, "Performing action=$action on book with id=$bookId")
        val cv = ContentValues()
        cv.put(BookDB.Book.UPDATED_AT, System.currentTimeMillis())
        val uri = BookDB.Book.create(bookId)
        val fromWearable = intent.getBooleanExtra(EXTRA_WEARABLE_INPUT, false)
        when (action) {
            Action.BUY -> {
                cv.put(BookDB.Book.OWNED, 1)
                if (contentResolver.update(uri, cv, null, null) == 1 && fromWearable) {
                    getBook(bookId)?.let { book ->
                        val builder = confirmationBuilder(book, R.string.book_purchased)
                        // ONLY 4 WEARABLE(s)
                        val wearableExtender = NotificationCompat.WearableExtender()
                        wearableExtender.setBackground(BitmapFactory.decodeResource(resources, R.drawable.background))
                        // ACTION TO SELL A BOOK FROM A WEARABLE
                        val sellIntent = PendingIntent.getService(
                            this, 0, IntentBuilder.sell(this, book).notificationId(NOTIFICATION_ID).wearableInput().build(),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        wearableExtender.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_sell, getString(R.string.action_sell), sellIntent).build())
                        // Finally extending the notification
                        builder.extend(wearableExtender)
                        NotificationChannels.notify(this, NOTIFICATION_ID, builder.build())
                    }
                }
                // We want to sync with the wearable
                trySyncDataWithWearableLayer(uri, cv)
            }
            Action.SELL -> {
                cv.put(BookDB.Book.OWNED, 0)
                if (contentResolver.update(uri, cv, null, null) == 1 && fromWearable) {
                    getBook(bookId)?.let { book ->
                        NotificationChannels.notify(this, NOTIFICATION_ID, confirmationBuilder(book, R.string.book_sold).build())
                    }
                }
                // We want to sync with the wearable
                trySyncDataWithWearableLayer(uri, cv)
            }
            Action.ADD_NOTE -> {
                val notes = getExtraNotes(intent)
                if (!notes.isNullOrEmpty()) {
                    cv.put(BookDB.Book.NOTES, notes.toString())
                    contentResolver.update(uri, cv, null, null)
                    // We want to sync with the wearable
                    trySyncDataWithWearableLayer(uri, cv)
                }
            }
        }
    }

    /**
     * Notification confirming an action performed on a book (e.g. purchased, sold).
     */
    private fun confirmationBuilder(book: Book, @StringRes title: Int): NotificationCompat.Builder {
        NotificationChannels.ensureCreated(this)
        return NotificationCompat.Builder(this, NotificationChannels.BOOKS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setContentTitle(getString(title))
            .setContentText(book.title)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, HomeActivity.createFor(this, book), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
    }

    private fun trySyncDataWithWearableLayer(uri: Uri, data: ContentValues) {
        val client = mGoogleApiClient
        if (client == null) {
            Log.e(TAG_LOG, "GoogleApiClient not available.")
            return
        }
        if (!client.isConnected && !client.blockingConnect(GOOGLE_PLAY_TIMEOUT, TimeUnit.MILLISECONDS).isSuccess) {
            Log.e(TAG_LOG, "Cannot connect to GoogleApiClient.")
            return
        }
        Wearable.DataApi.putDataItem(client, Event.DataApi.Builder.create(uri, data).asRequest())
    }

    private fun getBook(bookId: Long): Book? =
        contentResolver.query(BookDB.Book.create(bookId), null, null, null, null)?.use { c ->
            if (c.moveToNext()) Book.oneFrom(c) else null
        }

    private fun getExtraNotes(intent: Intent): CharSequence? =
        RemoteInput.getResultsFromIntent(intent)?.getCharSequence(EXTRA_ADD_NOTE)

    companion object {

        /**
         * Tag used for logging purposes.
         */
        private val TAG_LOG = BookActionService::class.java.simpleName

        private const val NOTIFICATION_ID = 37

        /**
         * Timeout for the GooglePlayClient connection in milliseconds.
         */
        private const val GOOGLE_PLAY_TIMEOUT = 2000L

        /**
         * Available params.
         */
        private const val EXTRA_BOOK_ID = "com.alchemiasoft.book.service.BOOK_ID"
        private const val EXTRA_NOTIFICATION_ID = "com.alchemiasoft.book.service.NOTIFICATION_ID"
        private const val EXTRA_WEARABLE_INPUT = "com.alchemiasoft.book.service.WEARABLE_INPUT"
        private const val EXTRA_ADD_NOTE = "com.alchemiasoft.book.service.ADD_NOTE"

        /**
         * Not valid id for book.
         */
        private const val NOT_VALID_BOOK = -1L

        /**
         * Not valid id for notifications.
         */
        private const val NOT_VALID_NOTIFICATION = -1
    }
}
