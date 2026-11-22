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

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

/**
 * Definition for the Book's Database and Provider.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
object BookDB {

    const val NAME = "book.db"
    // 7: cover image URL. Upgrading recreates the table from books.json (see BookDBOpenHelper.onUpgrade).
    const val VERSION = 7

    const val AUTHORITY = "com.alchemiasoft.book.provider"
    const val VND = "/vnd.book."

    const val CONTENT_SCHEME = ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/"

    /**
     * Book's table in the Database.
     */
    object Book {

        const val _ID = BaseColumns._ID

        const val TABLE = "Book"

        const val SERVER_ID = "server_id"
        const val TITLE = "title"
        const val AUTHOR = "author"
        const val PAGES = "pages"
        const val SOURCE = "source"
        const val DESCRIPTION = "description"
        const val OWNED = "owned"
        const val NOTES = "notes"
        const val TAG = "tag"
        const val UPDATED_AT = "updated_at"
        const val COVER = "cover"

        const val CREATE_TABLE = "CREATE TABLE " + TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SERVER_ID + " TEXT NOT NULL, " + TITLE + " TEXT NOT NULL, " + AUTHOR + " TEXT, " + SOURCE + " TEXT, " + TAG + " TEXT, " +
                DESCRIPTION + " TEXT, " + PAGES + " INTEGER, " + NOTES + " TEXT, " + UPDATED_AT + " INTEGER DEFAULT 0, " + OWNED + " BOOLEAN, " +
                COVER + " TEXT);"
        const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE;"

        const val PATH = "book"
        @JvmField
        val CONTENT_URI: Uri = Uri.parse(CONTENT_SCHEME + PATH)
        const val CURSOR_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND + PATH
        const val CURSOR_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND + PATH
        const val ITEM_PATH = "$PATH/#"
        const val DIR_PATH = PATH

        fun create(): Uri = CONTENT_URI

        fun create(id: Long): Uri = Uri.withAppendedPath(CONTENT_URI, id.toString())
    }
}
