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
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alchemiasoft.book.activity.HomeActivity
import com.alchemiasoft.book.receiver.SuggestionReceiver
import com.alchemiasoft.book.service.BookActionService
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Buying and selling go through BookActionService (an IntentService) and end up in the provider.
 */
@RunWith(AndroidJUnit4::class)
class BookActionServiceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun buyAndSellUpdateTheBook() {
        // Services can only be started while the app is in the foreground (Android 8+).
        ActivityScenario.launch(HomeActivity::class.java).use {
            val book = firstBook()
            val uri = BookDB.Book.create(book.id)
            try {
                context.contentResolver.update(uri, ContentValues().apply { put(BookDB.Book.OWNED, 0) }, null, null)

                context.startService(BookActionService.IntentBuilder.buy(context, book).build())
                assertTrue("Book was not bought", waitForOwned(book.id, true))

                context.startService(BookActionService.IntentBuilder.sell(context, book).build())
                assertTrue("Book was not sold", waitForOwned(book.id, false))
            } finally {
                context.contentResolver.update(uri, ContentValues().apply { put(BookDB.Book.OWNED, if (book.isOwned) 1 else 0) }, null, null)
            }
        }
    }

    @Test
    fun suggestionIsScheduledWithTheDefaultInterval() {
        // The default suggestion interval is NOW, so scheduling must succeed.
        assertEquals(true, SuggestionReceiver.scheduleSuggestion(context))
        SuggestionReceiver.cancelSuggestion(context)
    }

    private fun firstBook(): Book = context.contentResolver.query(BookDB.Book.CONTENT_URI, null, null, null, null)!!.use { c ->
        assertTrue(c.moveToFirst())
        Book.oneFrom(c)
    }

    private fun waitForOwned(bookId: Long, owned: Boolean, timeoutMs: Long = 5_000): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            val current = context.contentResolver.query(BookDB.Book.create(bookId), null, null, null, null)!!.use { c ->
                c.moveToFirst() && Book.oneFrom(c).isOwned == owned
            }
            if (current) {
                return true
            }
            SystemClock.sleep(100)
        }
        return false
    }
}
