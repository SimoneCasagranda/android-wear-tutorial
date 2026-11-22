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


// The tutorial uses the GoogleApiClient-based Wearable APIs (deprecated, still shipped in play-services-wearable).
@file:Suppress("DEPRECATION")

package com.alchemiasoft.common.sync

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import androidx.annotation.IntDef
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.util.UriUtil
import com.alchemiasoft.common.util.WearableUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageApi.SendMessageResult
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.Executors

/**
 * Class that allows to represent a book event.
 * This event can be an action or a data update.
 *
 * Created by Simone Casagranda on 09/02/15.
 */
object Event {

    /**
     * Entry point for the Events associated with the DataApi.
     */
    object DataApi {

        class Builder private constructor(private var mUri: Uri, private val mValues: ContentValues) {

            fun where(where: String?, vararg args: String): Builder {
                mUri = UriUtil.withWhere(mUri, where)
                mUri = UriUtil.withWhereArgs(mUri, *args)
                return this
            }

            fun asRequest(): PutDataRequest {
                val dataMapRequest = PutDataMapRequest.create(mUri.path!!)
                val dataMap = dataMapRequest.dataMap
                if (mValues.containsKey(BookDB.Book.NOTES)) {
                    dataMap.putString(BookDB.Book.NOTES, mValues.getAsString(BookDB.Book.NOTES))
                }
                mValues.getAsInteger(BookDB.Book.OWNED)?.let { dataMap.putInt(BookDB.Book.OWNED, it) }
                mValues.getAsLong(BookDB.Book.UPDATED_AT)?.let { dataMap.putLong(BookDB.Book.UPDATED_AT, it) }
                if (mValues.containsKey(BookDB.Book.TAG)) {
                    dataMap.putString(BookDB.Book.TAG, mValues.getAsString(BookDB.Book.TAG))
                }
                return dataMapRequest.asPutDataRequest()
            }

            companion object {
                fun create(uri: Uri, values: ContentValues): Builder = Builder(uri, values)
            }
        }

        class Item private constructor(item: DataItem) {

            private val mDataMap: DataMap = DataMapItem.fromDataItem(item).dataMap
            private val mUri: Uri = item.uri

            val where: String? = UriUtil.getWhere(mUri)
            val whereArgs: Array<String> = UriUtil.getWhereArgs(mUri)

            // Readability versus efficiency (this is a tutorial not production code)
            val uri: Uri
                get() = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + BookDB.AUTHORITY + mUri.path)

            val values: ContentValues
                get() = ContentValues().apply {
                    if (mDataMap.containsKey(BookDB.Book.NOTES)) {
                        put(BookDB.Book.NOTES, mDataMap.getString(BookDB.Book.NOTES))
                    }
                    if (mDataMap.containsKey(BookDB.Book.OWNED)) {
                        put(BookDB.Book.OWNED, mDataMap.getInt(BookDB.Book.OWNED))
                    }
                    if (mDataMap.containsKey(BookDB.Book.UPDATED_AT)) {
                        put(BookDB.Book.UPDATED_AT, mDataMap.getLong(BookDB.Book.UPDATED_AT))
                    }
                    if (mDataMap.containsKey(BookDB.Book.TAG)) {
                        put(BookDB.Book.TAG, mDataMap.getString(BookDB.Book.TAG))
                    }
                }

            val time: Long
                get() = mDataMap.getLong(BookDB.Book.UPDATED_AT, 0L)

            companion object {
                fun from(item: DataItem): Item = Item(item)
            }
        }
    }

    /**
     * Entry point for the Events associated with the Wearable message api.
     */
    object MessageApi {

        private const val ACTION = "action"

        const val OPEN = 23

        @IntDef(OPEN)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Action

        /**
         * Utility that allows to build and send a message through the WearableApi,
         * through a builder style.
         */
        class Sender private constructor(private val mClient: GoogleApiClient) {

            private var mTarget: String? = null
            private var mBookId: Long = 0L
            private var mAction: Int = OPEN
            private var mCallback: ResultCallback<SendMessageResult>? = null

            /**
             * Allows to specify a custom action.
             */
            fun action(@Action action: Int): Sender = apply { mAction = action }

            /**
             * Allows to set the book id.
             */
            fun bookId(bookId: Long): Sender = apply { mBookId = bookId }

            /**
             * Allows to specify a particular target for the message.
             */
            fun target(nodeId: String?): Sender = apply { mTarget = nodeId }

            /**
             * Allows to set a result Callback that will be notified of the sending status.
             */
            fun callback(callback: ResultCallback<SendMessageResult>?): Sender = apply { mCallback = callback }

            /**
             * Sends the message trying to fill missing parameters.
             */
            fun send() {
                if (mTarget.isNullOrEmpty()) {
                    mTarget = WearableUtil.getConnectedNodeAt(mClient, 0)?.id
                }
                val target = mTarget
                if (target.isNullOrEmpty()) {
                    return
                }
                val uri = UriUtil.withParam(BookDB.Book.create(mBookId), ACTION, mAction.toString())
                Wearable.MessageApi.sendMessage(mClient, target, uri.toString(), DEFAULT_DATA)
                    .setResultCallback(mCallback ?: SILENT_CALLBACK)
            }

            fun asyncSend() {
                EXECUTOR.execute { send() }
            }

            companion object {
                private val DEFAULT_DATA = ByteArray(0)
                private val EXECUTOR = Executors.newSingleThreadExecutor()
                private val SILENT_CALLBACK = ResultCallback<SendMessageResult> { }

                /**
                 * Creates an instance of Sender for the given book and GoogleApiClient.
                 *
                 * @param client used to send the message.
                 * @param bookId that will be sent.
                 * @return the Sender instance that supports method chaining.
                 */
                fun create(client: GoogleApiClient, bookId: Long): Sender = Sender(client).bookId(bookId).action(OPEN)
            }
        }

        /**
         * Allows to handle a MessageEvent granting the possibility to access restricted params.
         */
        class Receiver private constructor(event: MessageEvent) {

            private val mUri: Uri = Uri.parse(event.path)

            /**
             * The book id associated with the event.
             */
            val bookId: Long
                get() = mUri.lastPathSegment!!.toLong()

            /**
             * The action associated with the event.
             */
            val action: Int
                get() = UriUtil.getParam(mUri, ACTION)!!.toInt()

            companion object {
                /**
                 * Creates a new Receiver for the given event.
                 */
                fun from(event: MessageEvent): Receiver = Receiver(event)
            }
        }
    }
}