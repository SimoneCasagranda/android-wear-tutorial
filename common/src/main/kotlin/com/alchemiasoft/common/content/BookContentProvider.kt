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


package com.alchemiasoft.common.content

import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.alchemiasoft.common.util.ArraysUtil
import com.alchemiasoft.common.util.UriUtil

/**
 * ContentProvider for the Book application.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class BookContentProvider : ContentProvider() {

    private lateinit var mDbHelper: SQLiteOpenHelper

    override fun onCreate(): Boolean {
        mDbHelper = BookDBOpenHelper(context!!)
        return true
    }

    override fun getType(uri: Uri): String? = when (URI_MATCHER.match(uri)) {
        CODE_ITEM -> BookDB.Book.CURSOR_ITEM_MIME_TYPE
        CODE_DIR -> BookDB.Book.CURSOR_DIR_MIME_TYPE
        else -> null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
        val limit = UriUtil.getLimit(uri)
        val db = mDbHelper.writableDatabase
        val cursor = when (URI_MATCHER.match(uri)) {
            CODE_ITEM -> {
                val (where, whereArgs) = forItem(uri, selection, selectionArgs)
                db.query(BookDB.Book.TABLE, projection, where, whereArgs, null, null, null, limit)
            }
            CODE_DIR -> db.query(BookDB.Book.TABLE, projection, selection, selectionArgs, null, null, sortOrder, limit)
            else -> throw IllegalArgumentException("Uri not valid for ContentProvider $uri")
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        when (URI_MATCHER.match(uri)) {
            CODE_DIR -> {
                val id = mDbHelper.writableDatabase.insert(BookDB.Book.TABLE, null, values)
                context!!.contentResolver.notifyChange(uri, null)
                return BookDB.Book.create(id)
            }
            else -> throw IllegalArgumentException("Uri not valid for ContentProvider $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = mDbHelper.writableDatabase
        val result = when (URI_MATCHER.match(uri)) {
            CODE_ITEM -> {
                val (where, whereArgs) = forItem(uri, selection, selectionArgs)
                db.delete(BookDB.Book.TABLE, where, whereArgs)
            }
            CODE_DIR -> db.delete(BookDB.Book.TABLE, selection, selectionArgs)
            else -> throw IllegalArgumentException("Uri not valid for ContentProvider $uri")
        }
        if (result > 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return result
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val cv = values ?: ContentValues()
        val db = mDbHelper.writableDatabase
        val result = when (URI_MATCHER.match(uri)) {
            CODE_ITEM -> {
                val (where, whereArgs) = forItem(uri, selection, selectionArgs)
                db.update(BookDB.Book.TABLE, cv.withUpdatedAt(), where, whereArgs)
            }
            CODE_DIR -> db.update(BookDB.Book.TABLE, cv.withUpdatedAt(), selection, selectionArgs)
            else -> throw IllegalArgumentException("Uri not valid for ContentProvider $uri")
        }
        if (result > 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return result
    }

    override fun applyBatch(operations: ArrayList<ContentProviderOperation>): Array<ContentProviderResult?> {
        val db = mDbHelper.writableDatabase
        db.beginTransaction()
        try {
            val results = arrayOfNulls<ContentProviderResult>(operations.size)
            operations.forEachIndexed { i, operation -> results[i] = operation.apply(this, results, i) }
            db.setTransactionSuccessful()
            return results
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Restricts the given selection to the book identified by the item uri.
     */
    private fun forItem(uri: Uri, selection: String?, selectionArgs: Array<String>?): Pair<String, Array<String>> {
        val idArg = arrayOf(uri.lastPathSegment!!)
        return if (selection.isNullOrEmpty()) {
            BookDB.Book._ID + " = ?" to idArg
        } else {
            BookDB.Book._ID + " = ? AND (" + selection + ")" to ArraysUtil.concatenate(idArg, selectionArgs ?: emptyArray())
        }
    }

    /**
     * The sync layer relies on UPDATED_AT: it is set here when the caller didn't provide it.
     */
    private fun ContentValues.withUpdatedAt(): ContentValues = apply {
        if (!containsKey(BookDB.Book.UPDATED_AT)) {
            put(BookDB.Book.UPDATED_AT, System.currentTimeMillis())
        }
    }

    companion object {
        private const val CODE_ITEM = 0
        private const val CODE_DIR = 1

        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(BookDB.AUTHORITY, BookDB.Book.ITEM_PATH, CODE_ITEM)
            addURI(BookDB.AUTHORITY, BookDB.Book.DIR_PATH, CODE_DIR)
        }
    }
}
