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


package com.alchemiasoft.books

import android.content.ContentValues
import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alchemiasoft.books.activity.BooksActivity
import com.alchemiasoft.books.ui.TAG_BUY
import com.alchemiasoft.books.ui.TAG_COLUMNS
import com.alchemiasoft.books.ui.TAG_ROWS
import com.alchemiasoft.books.ui.TAG_SETTINGS
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives the Compose for Wear OS UI: rows of books, columns of details/actions, buying from the watch.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class BooksScreenTest {

    @get:Rule
    val compose = createAndroidComposeRule<BooksActivity>()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var books: List<Book>

    @Before
    fun setUp() {
        context.contentResolver.update(
            BookDB.Book.CONTENT_URI,
            ContentValues().apply {
                put(BookDB.Book.OWNED, 0)
                putNull(BookDB.Book.NOTES)
            },
            null, null,
        )
        books = context.contentResolver.query(BookDB.Book.CONTENT_URI, null, null, null, null)!!.use { Book.allFrom(it) }
    }

    @Test
    fun firstRowShowsTheFirstBook() {
        compose.waitUntilAtLeastOneExists(hasText(books[0].title!!), TIMEOUT)
    }

    @Test
    fun swipingUpShowsTheNextBook() {
        compose.waitUntilAtLeastOneExists(hasTestTag(TAG_ROWS), TIMEOUT)
        compose.onNodeWithTag(TAG_ROWS).performTouchInput { swipeUp() }
        compose.waitUntilAtLeastOneExists(hasText(books[1].title!!), TIMEOUT)
    }

    @Test
    fun columnsShowDescriptionAndSettings() {
        val columns = TAG_COLUMNS + books[0].id
        compose.waitUntilAtLeastOneExists(hasTestTag(columns), TIMEOUT)
        compose.onNodeWithTag(columns).performTouchInput { swipeLeft() }
        compose.waitUntilAtLeastOneExists(hasText(context.getString(R.string.description)), TIMEOUT)
        repeat(3) { compose.onNodeWithTag(columns).performTouchInput { swipeLeft() } }
        compose.waitUntilAtLeastOneExists(hasTestTag(TAG_SETTINGS), TIMEOUT)
        compose.waitUntilAtLeastOneExists(hasText(context.resources.getStringArray(R.array.settings)[0]), TIMEOUT)
    }

    @Test
    fun buyingFromTheWatchMarksTheBookAsOwned() {
        val book = books[0]
        val columns = TAG_COLUMNS + book.id
        compose.waitUntilAtLeastOneExists(hasTestTag(columns), TIMEOUT)
        repeat(3) { compose.onNodeWithTag(columns).performTouchInput { swipeLeft() } }
        compose.waitUntilAtLeastOneExists(hasTestTag(TAG_BUY), TIMEOUT)
        compose.onNodeWithTag(TAG_BUY, useUnmergedTree = true).performClick()
        // After the 2 second countdown BookService updates the provider (waitUntil keeps the Compose clock running).
        compose.waitUntil(TIMEOUT) { isOwned(book.id) }
        assertTrue("Book was not bought", isOwned(book.id))
        // The success confirmation is shown (even though the book's row is gone) and then the app closes.
        compose.waitUntil(TIMEOUT) { compose.activity.isFinishing || compose.activity.isDestroyed }
    }

    private fun isOwned(bookId: Long): Boolean =
        context.contentResolver.query(BookDB.Book.create(bookId), null, null, null, null)!!.use { c ->
            c.moveToFirst() && Book.oneFrom(c).isOwned
        }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
