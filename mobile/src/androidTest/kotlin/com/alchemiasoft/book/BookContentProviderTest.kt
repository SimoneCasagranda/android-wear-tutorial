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


package com.alchemiasoft.book

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Checks that the app's BookContentProvider is seeded from books.json and persists writes.
 */
@RunWith(AndroidJUnit4::class)
class BookContentProviderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun applicationIdIsShared() {
        // Phone and watch must share the applicationId for the Wear Data Layer to pair them.
        assertEquals("com.alchemiasoft.android_wear_tutorial", context.packageName)
    }

    @Test
    fun seededBooksAreAvailable() {
        context.contentResolver.query(BookDB.Book.CONTENT_URI, null, null, null, null).use { c ->
            assertNotNull(c)
            assertTrue("Expected seeded books", c!!.count > 0)
            assertTrue(c.moveToFirst())
            val book = Book.oneFrom(c)
            assertFalse(book.title.isNullOrEmpty())
            assertFalse(book.author.isNullOrEmpty())
        }
    }

    @Test
    fun ownedFlagIsPersisted() {
        val cr = context.contentResolver
        val original = cr.query(BookDB.Book.CONTENT_URI, null, null, null, null)!!.use { c ->
            assertTrue(c.moveToFirst())
            Book.oneFrom(c)
        }
        val uri = BookDB.Book.create(original.id)
        try {
            val cv = ContentValues().apply { put(BookDB.Book.OWNED, if (original.isOwned) 0 else 1) }
            assertEquals(1, cr.update(uri, cv, null, null))
            cr.query(uri, null, null, null, null)!!.use { c ->
                assertTrue(c.moveToFirst())
                val updated = Book.oneFrom(c)
                assertEquals(!original.isOwned, updated.isOwned)
                // The provider fills in UPDATED_AT when the caller doesn't (the sync layer relies on it).
                assertTrue(updated.updatedAt > 0)
            }
        } finally {
            cr.update(uri, ContentValues().apply { put(BookDB.Book.OWNED, if (original.isOwned) 1 else 0) }, null, null)
        }
    }

    @Test
    fun itemSelectionIsCombinedWithTheId() {
        val cr = context.contentResolver
        val first = cr.query(BookDB.Book.CONTENT_URI, null, null, null, null)!!.use { c ->
            assertTrue(c.moveToFirst())
            Book.oneFrom(c)
        }
        val uri = BookDB.Book.create(first.id)
        // Matching extra selection: the row is returned
        cr.query(uri, null, BookDB.Book.TITLE + " = ?", arrayOf(first.title!!), null)!!.use { assertEquals(1, it.count) }
        // Non matching extra selection: nothing is returned
        cr.query(uri, null, BookDB.Book.TITLE + " = ?", arrayOf("no such title"), null)!!.use { assertEquals(0, it.count) }
    }
}
