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


package com.alchemiasoft.common.model

import android.content.ContentValues
import android.database.Cursor
import com.alchemiasoft.common.content.BookDB
import org.json.JSONArray
import org.json.JSONObject

/**
 * Entity that represents a Book.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class Book private constructor() {

    var id: Long = NOT_VALID
        private set
    var serverId: String? = null
        private set
    var title: String? = null
        private set
    var author: String? = null
        private set
    var source: String? = null
        private set
    var description: String? = null
        private set
    var pages: Int = 0
        private set
    var notes: String? = null
        private set
    var isOwned: Boolean = false
        private set
    var updatedAt: Long = 0L
        private set
    var tag: String? = null
        private set

    fun toValues(): ContentValues = ContentValues().apply {
        put(BookDB.Book.SERVER_ID, serverId)
        put(BookDB.Book.TITLE, title)
        put(BookDB.Book.AUTHOR, author)
        put(BookDB.Book.SOURCE, source)
        put(BookDB.Book.DESCRIPTION, description)
        put(BookDB.Book.PAGES, pages)
        put(BookDB.Book.NOTES, notes)
        put(BookDB.Book.OWNED, isOwned)
        put(BookDB.Book.TAG, tag)
    }

    override fun toString(): String = "$title - $author ($pages--$serverId)."

    companion object {

        const val NOT_VALID = -1L

        fun oneFrom(c: Cursor): Book = Book().apply {
            c.ifColumn(BookDB.Book.SERVER_ID) { serverId = c.getString(it) }
            c.ifColumn(BookDB.Book._ID) { id = c.getLong(it) }
            c.ifColumn(BookDB.Book.TITLE) { title = c.getString(it) }
            c.ifColumn(BookDB.Book.AUTHOR) { author = c.getString(it) }
            c.ifColumn(BookDB.Book.SOURCE) { source = c.getString(it) }
            c.ifColumn(BookDB.Book.DESCRIPTION) { description = c.getString(it) }
            c.ifColumn(BookDB.Book.PAGES) { pages = c.getInt(it) }
            c.ifColumn(BookDB.Book.NOTES) { notes = c.getString(it) }
            c.ifColumn(BookDB.Book.OWNED) { isOwned = c.getInt(it) == 1 }
            c.ifColumn(BookDB.Book.UPDATED_AT) { updatedAt = c.getLong(it) }
            c.ifColumn(BookDB.Book.TAG) { tag = c.getString(it) }
        }

        fun allFrom(c: Cursor): List<Book> {
            val books = mutableListOf<Book>()
            while (c.moveToNext()) {
                books += oneFrom(c)
            }
            return books
        }

        fun oneFrom(json: JSONObject): Book = Book().apply {
            serverId = json.optString("serverId")
            title = json.optString("title")
            author = json.optString("author")
            source = json.optString("source")
            description = json.optString("description")
            pages = json.optInt("pages")
            tag = json.optString("tag")
        }

        fun allFrom(jsonArr: JSONArray): List<Book> = List(jsonArr.length()) { oneFrom(jsonArr.optJSONObject(it)) }

        private inline fun Cursor.ifColumn(name: String, block: (Int) -> Unit) {
            val index = getColumnIndex(name)
            if (index > -1) {
                block(index)
            }
        }
    }
}
