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


package com.alchemiasoft.common

import android.database.MatrixCursor
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Book is the bridge between books.json, the database and the UI.
 */
@RunWith(AndroidJUnit4::class)
class BookTest {

    private val json = JSONObject()
        .put("serverId", "srv-1")
        .put("title", "Kotlin in Action")
        .put("author", "Dmitry Jemerov")
        .put("source", "Manning")
        .put("description", "A book about Kotlin")
        .put("pages", 360)
        .put("tag", "kotlin")
        .put("cover", "https://example.com/cover.jpg")

    @Test
    fun parsesJson() {
        val book = Book.oneFrom(json)
        assertEquals(Book.NOT_VALID, book.id)
        assertEquals("srv-1", book.serverId)
        assertEquals("Kotlin in Action", book.title)
        assertEquals("Dmitry Jemerov", book.author)
        assertEquals("Manning", book.source)
        assertEquals("A book about Kotlin", book.description)
        assertEquals(360, book.pages)
        assertEquals("kotlin", book.tag)
        assertEquals("https://example.com/cover.jpg", book.cover)
        assertFalse(book.isOwned)
        assertNull(book.notes)
        assertEquals("Kotlin in Action - Dmitry Jemerov (360--srv-1).", book.toString())
    }

    @Test
    fun parsesJsonArray() {
        val books = Book.allFrom(JSONArray().put(json).put(JSONObject().put("title", "Second")))
        assertEquals(listOf("Kotlin in Action", "Second"), books.map { it.title })
        // A book without a cover in the JSON has a null cover (not an empty string).
        assertNull(books[1].cover)
    }

    @Test
    fun toValuesContainsDatabaseColumns() {
        val values = Book.oneFrom(json).toValues()
        assertEquals("srv-1", values.getAsString(BookDB.Book.SERVER_ID))
        assertEquals("Kotlin in Action", values.getAsString(BookDB.Book.TITLE))
        assertEquals(360, values.getAsInteger(BookDB.Book.PAGES))
        assertEquals("https://example.com/cover.jpg", values.getAsString(BookDB.Book.COVER))
        assertEquals(false, values.getAsBoolean(BookDB.Book.OWNED))
        assertTrue(values.containsKey(BookDB.Book.NOTES))
    }

    @Test
    fun readsCursorRowsAndToleratesMissingColumns() {
        val cursor = MatrixCursor(arrayOf(BookDB.Book._ID, BookDB.Book.TITLE, BookDB.Book.OWNED, BookDB.Book.UPDATED_AT))
        cursor.addRow(arrayOf<Any>(4L, "Owned book", 1, 99L))
        cursor.addRow(arrayOf<Any>(5L, "Not owned", 0, 0L))
        val books = Book.allFrom(cursor)
        assertEquals(2, books.size)
        assertEquals(4L, books[0].id)
        assertTrue(books[0].isOwned)
        assertEquals(99L, books[0].updatedAt)
        assertFalse(books[1].isOwned)
        // Columns that are not in the cursor keep their defaults
        assertNull(books[0].author)
        assertEquals(0, books[0].pages)
    }
}
