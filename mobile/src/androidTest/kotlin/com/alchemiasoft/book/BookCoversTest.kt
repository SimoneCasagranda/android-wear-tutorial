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

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.asImage
import coil3.test.FakeImageLoaderEngine
import com.alchemiasoft.book.activity.HomeActivity
import com.alchemiasoft.book.ui.TAG_ALL_LIST
import com.alchemiasoft.book.ui.initials
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Book covers in the list: loaded with Coil for books that have one, initials otherwise.
 * A fake Coil engine records the requested URLs and answers with a solid colour (no network).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class BookCoversTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val requestedUrls = CopyOnWriteArrayList<Any>()

    /**
     * Installs the fake image loader before the activity starts.
     */
    @get:Rule(order = 0)
    val fakeImageLoader = object : ExternalResource() {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @OptIn(DelicateCoilApi::class)
        override fun before() {
            val engine = FakeImageLoaderEngine.Builder().default(ColorDrawable(Color.RED).asImage()).build()
            scope.launch { engine.requests.collect { requestedUrls += it.request.data } }
            SingletonImageLoader.setUnsafe(ImageLoader.Builder(context).components { add(engine) }.build())
        }

        @OptIn(DelicateCoilApi::class)
        override fun after() {
            scope.cancel()
            SingletonImageLoader.reset()
        }
    }

    @get:Rule(order = 1)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 2)
    val compose = createAndroidComposeRule<HomeActivity>()

    private val books: List<Book> by lazy {
        context.contentResolver.query(BookDB.Book.CONTENT_URI, null, null, null, null)!!.use { Book.allFrom(it) }
    }

    @Test
    fun seededBooksComeWithCoverUrls() {
        // 20 of the 25 seeded books have an Open Library cover.
        assertEquals(20, books.count { it.cover != null })
        assertEquals(true, books.filter { it.cover != null }.all { it.cover!!.startsWith("https://covers.openlibrary.org/") })
    }

    @Test
    fun coverIsLoadedForABookThatHasOne() {
        val book = books.first { it.cover != null }
        compose.waitUntilAtLeastOneExists(hasContentDescription(context.getString(R.string.cover_description, book.title)), TIMEOUT)
        compose.waitUntil(TIMEOUT) { book.cover in requestedUrls }
    }

    @Test
    fun bookWithoutCoverShowsItsInitials() {
        val book = books.first { it.cover == null }
        val card = hasTestTag("book_${book.id}")
        compose.waitUntilAtLeastOneExists(card and hasText(initials(book.title)), TIMEOUT)
        compose.waitUntilDoesNotExist(hasContentDescription(context.getString(R.string.cover_description, book.title)), TIMEOUT)
        assertEquals(false, requestedUrls.any { it == null || it == "" })
    }

    @Test
    fun detailShowsTheCoverOfABookThatHasOne() {
        val book = books.first { it.cover != null }
        openDetail(book)
        // The cover is the background of the detail header.
        compose.waitUntilAtLeastOneExists(hasTestTag("detail_cover_${book.id}"), TIMEOUT)
        compose.onNode(hasTestTag("detail_cover_${book.id}") and hasContentDescription(context.getString(R.string.cover_description, book.title)))
            .assertExists()
        compose.waitUntil(TIMEOUT) { book.cover in requestedUrls }
    }

    @Test
    fun detailHasNoCoverForABookWithoutOne() {
        val book = books.first { it.cover == null }
        openDetail(book)
        // Plain header: no cover background. Once the list has faded out its initials tile is gone too.
        compose.onNode(hasTestTag("detail_cover_${book.id}")).assertDoesNotExist()
        compose.waitUntilDoesNotExist(hasTestTag("cover_${book.id}"), TIMEOUT)
    }

    /**
     * Opens the detail page of the given book from the list and waits until it is shown.
     */
    private fun openDetail(book: Book) {
        compose.waitUntilAtLeastOneExists(hasTestTag("book_${book.id}"), TIMEOUT)
        compose.onNode(hasTestTag("book_${book.id}")).performClick()
        compose.waitUntilAtLeastOneExists(hasText(context.getString(R.string.source, book.source)), TIMEOUT)
    }

    @Test
    fun backFromTheDetailReturnsToTheSameScrollPosition() {
        // A book far enough down the list that it is off screen at the start.
        val book = books.last()
        compose.waitUntilAtLeastOneExists(hasTestTag(TAG_ALL_LIST), TIMEOUT)
        compose.onNode(hasTestTag(TAG_ALL_LIST)).performScrollToNode(hasTestTag("book_${book.id}"))
        openDetail(book)
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        // The list comes back where it was: the card is shown without scrolling again.
        compose.waitUntilAtLeastOneExists(hasTestTag(TAG_ALL_LIST), TIMEOUT)
        compose.waitUntilAtLeastOneExists(hasTestTag("book_${book.id}"), TIMEOUT)
        compose.onNode(hasTestTag("book_${book.id}")).assertIsDisplayed()
    }

    @Test
    fun initialsUseTheFirstTwoWords() {
        assertEquals("VC", initials("Version Control with Git"))
        assertEquals("AG", initials("Android: Guida per lo Sviluppatore"))
        assertEquals("5A", initials("50 Android Hacks"))
        assertEquals("", initials(null))
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
