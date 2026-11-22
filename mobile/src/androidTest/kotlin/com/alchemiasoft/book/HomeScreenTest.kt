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
import android.content.ContentValues
import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.alchemiasoft.book.activity.HomeActivity
import com.alchemiasoft.book.ui.OWNED_DESCRIPTION
import com.alchemiasoft.book.ui.TAG_ALL_LIST
import com.alchemiasoft.book.ui.TAG_OWNED_LIST
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives the Compose phone UI end to end (list, detail, buy/sell through the services, filters).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<HomeActivity>()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var firstBook: Book

    @Before
    fun setUp() {
        setAllOwned(false)
        firstBook = context.contentResolver.query(BookDB.Book.CONTENT_URI, null, null, null, null)!!.use { c ->
            c.moveToFirst()
            Book.oneFrom(c)
        }
    }

    @After
    fun tearDown() = setAllOwned(false)

    @Test
    fun listShowsTheSeededBooks() {
        compose.waitUntilAtLeastOneExists(hasText(firstBook.title!!) and hasAnyAncestor(hasTestTag(TAG_ALL_LIST)), TIMEOUT)
    }

    @Test
    fun openingABookShowsItsDetailsAndBackReturnsToTheList() {
        compose.waitUntilAtLeastOneExists(hasText(firstBook.title!!), TIMEOUT)
        compose.onNodeWithText(firstBook.title!!).performClick()
        compose.waitUntilAtLeastOneExists(hasText(context.getString(R.string.source, firstBook.source)), TIMEOUT)
        compose.onNodeWithText(context.getString(R.string.pages, firstBook.pages.toString())).assertExists()
        compose.onNodeWithText(context.getString(R.string.action_buy).uppercase()).assertExists()

        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitUntilAtLeastOneExists(hasTestTag(TAG_ALL_LIST), TIMEOUT)
    }

    @Test
    fun buyingFromTheCardMenuAddsTheBookToMyBooks() {
        val card = hasTestTag("book_${firstBook.id}")
        compose.waitUntilAtLeastOneExists(card, TIMEOUT)
        compose.onNode(hasContentDescription(context.getString(R.string.more_options)) and hasAnyAncestor(card)).performClick()
        compose.onNodeWithText(context.getString(R.string.action_buy)).performClick()

        // BookActionService updates the provider; the list reacts and shows the owned marker
        // (the clickable card merges the marker's description into its own semantics node).
        compose.waitUntilAtLeastOneExists(card and hasContentDescription(OWNED_DESCRIPTION), TIMEOUT)

        compose.onNodeWithText(context.getString(R.string.owned_books).uppercase()).performClick()
        compose.waitUntilAtLeastOneExists(hasText(firstBook.title!!) and hasAnyAncestor(hasTestTag(TAG_OWNED_LIST)), TIMEOUT)
    }

    @Test
    fun myBooksIsEmptyWhenNothingIsOwned() {
        compose.waitUntilAtLeastOneExists(hasTestTag(TAG_ALL_LIST), TIMEOUT)
        compose.onNodeWithText(context.getString(R.string.owned_books).uppercase()).performClick()
        compose.waitUntilAtLeastOneExists(hasText(context.getString(R.string.no_books)), TIMEOUT)
    }

    @Test
    fun detailButtonBuysAndThenSells() {
        compose.waitUntilAtLeastOneExists(hasText(firstBook.title!!), TIMEOUT)
        compose.onNodeWithText(firstBook.title!!).performClick()
        compose.waitUntilAtLeastOneExists(hasText(context.getString(R.string.action_buy).uppercase()), TIMEOUT)
        compose.onNodeWithText(context.getString(R.string.action_buy).uppercase()).performClick()
        compose.waitUntilAtLeastOneExists(hasText(context.getString(R.string.action_sell).uppercase()), TIMEOUT)
        compose.onNodeWithText(context.getString(R.string.action_sell).uppercase()).performClick()
        compose.waitUntilAtLeastOneExists(hasText(context.getString(R.string.action_buy).uppercase()), TIMEOUT)
    }

    private fun setAllOwned(owned: Boolean) {
        context.contentResolver.update(BookDB.Book.CONTENT_URI, ContentValues().apply { put(BookDB.Book.OWNED, if (owned) 1 else 0) }, null, null)
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
