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

package com.alchemiasoft.books.service

import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.IntentCompat
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.sync.Event
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit

/**
 * IntentService that takes care of executing in the background any Book update.
 *
 * Created by Simone Casagranda on 28/01/15.
 */
class BookService : IntentService(TAG_LOG) {

    /**
     * Builder that allows to create in a simple manner the Intents with whom the
     * BookService should be invoked.
     */
    class Invoker private constructor(context: Context) {

        private val mContext: Context = context.applicationContext

        /**
         * Meta-data used to identify target and operation.
         */
        private val mMetaData = Bundle()

        /**
         * Params that will be passed directly to the ContentProvider.
         */
        private val mDBValues = ContentValues()

        fun bookId(id: Long): Invoker = apply { mMetaData.putLong(KEY_PARAM_BOOK_ID, id) }

        fun where(where: String?, vararg whereArgs: String): Invoker = apply {
            mMetaData.putString(KEY_PARAM_WHERE, where)
            mMetaData.putStringArray(KEY_PARAM_WHERE_ARGS, arrayOf(*whereArgs))
        }

        fun notes(notes: String?): Invoker = apply { mDBValues.put(BookDB.Book.NOTES, notes) }

        fun owned(owned: Boolean): Invoker = apply { mDBValues.put(BookDB.Book.OWNED, if (owned) 1 else 0) }

        fun invoke() {
            val intent = Intent(mContext, BookService::class.java)
                .putExtras(mMetaData)
                .putExtra(KEY_PARAM_DB_VALUES, mDBValues)
            mContext.startService(intent)
        }

        companion object {
            fun get(context: Context): Invoker = Invoker(context)
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
        Log.d(TAG_LOG, "New intent: $intent")
        val values = IntentCompat.getParcelableExtra(intent, KEY_PARAM_DB_VALUES, ContentValues::class.java)
        if (values == null || values.size() == 0) {
            Log.e(TAG_LOG, "Trying to update the DB with values=$values")
            return
        }
        values.put(BookDB.Book.UPDATED_AT, System.currentTimeMillis())
        val bookId = intent.getLongExtra(KEY_PARAM_BOOK_ID, NOT_VALID)
        if (bookId != NOT_VALID) {
            val uri = BookDB.Book.create(bookId)
            val updated = contentResolver.update(uri, values, null, null)
            Log.d(TAG_LOG, "Updated $updated with Uri=$uri")
            // We want to sync with the wearable
            trySyncDataWithWearableLayer(Event.DataApi.Builder.create(uri, values))
        } else {
            val uri = BookDB.Book.CONTENT_URI
            val where = intent.getStringExtra(KEY_PARAM_WHERE)
            val whereArgs = intent.getStringArrayExtra(KEY_PARAM_WHERE_ARGS) ?: emptyArray()
            val updated = contentResolver.update(uri, values, where, whereArgs)
            Log.d(TAG_LOG, "Updated $updated with Uri=$uri, where=$where <= ${whereArgs.contentToString()}")
            // We want to sync with the wearable
            trySyncDataWithWearableLayer(Event.DataApi.Builder.create(uri, values).where(where, *whereArgs))
        }
    }

    private fun trySyncDataWithWearableLayer(builder: Event.DataApi.Builder) {
        val client = mGoogleApiClient
        if (client == null) {
            Log.e(TAG_LOG, "GoogleApiClient not available.")
            return
        }
        if (!client.isConnected && !client.blockingConnect(GOOGLE_PLAY_TIMEOUT, TimeUnit.MILLISECONDS).isSuccess) {
            Log.e(TAG_LOG, "Cannot connect to GoogleApiClient.")
            return
        }
        Wearable.DataApi.putDataItem(client, builder.asRequest())
    }

    companion object {

        /**
         * Tag used for logging.
         */
        private val TAG_LOG = BookService::class.java.simpleName

        /**
         * Timeout for the GooglePlayClient connection in milliseconds.
         */
        private const val GOOGLE_PLAY_TIMEOUT = 2000L

        /**
         * Params.
         */
        private const val KEY_PARAM_BOOK_ID = "com.alchemiasoft.books.service.book.PARAM_BOOK_ID"
        private const val KEY_PARAM_DB_VALUES = "com.alchemiasoft.books.service.book.PARAM_DB_VALUES"
        private const val KEY_PARAM_WHERE = "com.alchemiasoft.books.service.book.WHERE"
        private const val KEY_PARAM_WHERE_ARGS = "com.alchemiasoft.books.service.book.WHERE_ARGS"

        /**
         * Not valid ID for a book entry.
         */
        private const val NOT_VALID = -1L
    }
}
