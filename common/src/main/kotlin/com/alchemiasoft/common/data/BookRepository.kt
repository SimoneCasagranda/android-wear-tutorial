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


package com.alchemiasoft.common.data

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import com.alchemiasoft.common.util.UriUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

/**
 * Reactive access to the books stored by [com.alchemiasoft.common.content.BookContentProvider].
 *
 * The provider stays the single source of truth (services and the Wear sync layer write into it);
 * this class turns its change notifications into [Flow]s that the Compose UIs collect.
 */
class BookRepository(context: Context) {

    private val mResolver: ContentResolver = context.applicationContext.contentResolver

    /**
     * Books, optionally filtered by ownership and limited in number, re-emitted on every change.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBooks(owned: Boolean? = null, limit: Int? = null): Flow<List<Book>> {
        val uri = if (limit != null) UriUtil.withLimit(BookDB.Book.CONTENT_URI, limit) else BookDB.Book.CONTENT_URI
        val selection = owned?.let { BookDB.Book.OWNED + " = ?" }
        val selectionArgs = owned?.let { arrayOf(if (it) "1" else "0") }
        return mResolver.changes(BookDB.Book.CONTENT_URI)
            .mapLatest {
                mResolver.query(uri, null, selection, selectionArgs, null)?.use { Book.allFrom(it) }.orEmpty()
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * A single book (or null if it doesn't exist), re-emitted on every change.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBook(id: Long): Flow<Book?> =
        mResolver.changes(BookDB.Book.CONTENT_URI)
            .mapLatest {
                mResolver.query(BookDB.Book.create(id), null, null, null, null)?.use { c ->
                    if (c.moveToFirst()) Book.oneFrom(c) else null
                }
            }
            .flowOn(Dispatchers.IO)

    private companion object {

        /**
         * Emits once immediately and then every time the content at (or below) [uri] changes.
         */
        fun ContentResolver.changes(uri: Uri): Flow<Unit> = callbackFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }
            registerContentObserver(uri, true, observer)
            trySend(Unit)
            awaitClose { unregisterContentObserver(observer) }
        }.conflate()
    }
}
