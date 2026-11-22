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


// The watch UI is built on the legacy Wear support library (framework fragments/loaders, GridViewPager)
// and the GoogleApiClient-based Wearable APIs: both deprecated, both kept on purpose for the tutorial.
@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.alchemiasoft.books.activity

import android.app.Activity
import android.app.Fragment
import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.wearable.view.CardFragment
import android.support.wearable.view.CrossfadeDrawable
import android.support.wearable.view.DotsPageIndicator
import android.support.wearable.view.FragmentGridPagerAdapter
import android.support.wearable.view.GridViewPager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.alchemiasoft.books.R
import com.alchemiasoft.books.fragment.AddNoteFragment
import com.alchemiasoft.books.fragment.BuyBookFragment
import com.alchemiasoft.books.fragment.InfoFragment
import com.alchemiasoft.books.fragment.SettingsFragment
import com.alchemiasoft.common.content.BookDB.Book
import com.alchemiasoft.common.sync.Event
import com.alchemiasoft.common.util.UriUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable

/**
 * Activity that displays some of the info about all the available books.
 */
class BooksActivity : FragmentActivity(), LoaderManager.LoaderCallbacks<Cursor>, GridViewPager.OnPageChangeListener,
    GoogleApiClient.ConnectionCallbacks {

    /**
     * UI references.
     */
    private lateinit var mPageIndicator: DotsPageIndicator
    private lateinit var mViewPager: GridViewPager
    private lateinit var mAdapter: BooksGridPagerAdapter
    private lateinit var mCrossfadeDrawable: CrossfadeDrawable

    /**
     * Client used by the wearable api to notify about the current book.
     */
    private var mGoogleApiClient: GoogleApiClient? = null

    private var mOldRow = NOT_VALID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)
        // Getting UI references
        mPageIndicator = findViewById(R.id.pager_indicator)
        mViewPager = findViewById(R.id.pager)
        // Drawable used to make the UI more captivating
        mCrossfadeDrawable = CrossfadeDrawable()
        mCrossfadeDrawable.setFading(ContextCompat.getDrawable(this, R.drawable.fading_background))
        findViewById<android.view.View>(android.R.id.content).background = mCrossfadeDrawable
        // Creating and Setting the Pager adapter
        mAdapter = BooksGridPagerAdapter(this)
        mViewPager.adapter = mAdapter
        // Connecting the GridViewPager to the indicator
        mPageIndicator.setPager(mViewPager)
        // Adding the page change listener
        mViewPager.setOnPageChangeListener(this)
        // Creating the GoogleApiClient for the Wearable api (if available)
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build()
        }
        // Initializing the loader
        loaderManager.initLoader(LOADER_ID_SUGGESTIONS, null, this)
    }

    override fun onStart() {
        super.onStart()
        // Trying to bind to the GoogleApiClient
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        // Releasing the client when not needed
        mGoogleApiClient?.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        loaderManager.destroyLoader(LOADER_ID_SUGGESTIONS)
    }

    override fun onConnected(bundle: Bundle?) {
        if (mOldRow != NOT_VALID) {
            notifyPhoneAbout(mAdapter.getBookId(mOldRow))
        }
    }

    override fun onConnectionSuspended(status: Int) {
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        require(id == LOADER_ID_SUGGESTIONS) { "Loader id=$id is not supported!" }
        return CursorLoader(this, UriUtil.withLimit(Book.CONTENT_URI, MAX_ROWS), PROJECTION, SELECTION, SELECTION_ARGS, ORDER_BY)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mAdapter.swapCursor(data)
        mViewPager.setCurrentItem(0, 0)
        onPageSelected(0, 0)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // While the activity is being destroyed the pager's fragments are already gone: notifying the
        // legacy FragmentGridPagerAdapter at that point crashes (NPE in applyItemPosition), so just drop the cursor.
        mAdapter.swapCursor(null, notify = !isDestroyed)
    }

    override fun onPageScrolled(row: Int, column: Int, rowOffset: Float, columnOffset: Float, rowOffsetPixels: Int, columnOffsetPixels: Int) {
        mPageIndicator.onPageScrolled(row, column, rowOffset, columnOffset, rowOffsetPixels, columnOffsetPixels)
    }

    override fun onPageSelected(row: Int, column: Int) {
        if (mOldRow != row) {
            mOldRow = row
            mCrossfadeDrawable.setBase(mAdapter.getBaseDrawable(row))
            // Notifying that the user is looking at the X row.
            if (mGoogleApiClient?.isConnected == true) {
                notifyPhoneAbout(mAdapter.getBookId(row))
            }
        }
        mCrossfadeDrawable.setProgress(if (mAdapter.isCard(row, column)) NO_FADE else PARTIAL_FADE)
        mPageIndicator.onPageSelected(row, column)
    }

    override fun onPageScrollStateChanged(state: Int) {
        mPageIndicator.onPageScrollStateChanged(state)
    }

    /**
     * Asks the phone to open the given book.
     */
    private fun notifyPhoneAbout(bookId: Long) {
        val client = mGoogleApiClient ?: return
        if (bookId >= 0L) {
            Event.MessageApi.Sender.create(client, bookId).action(Event.MessageApi.OPEN).asyncSend()
        }
    }

    /**
     * Adapter used to display books in pages. Following the Android Wear Design Guidelines,
     * all the elements will be displayed per row and in the columns the user will be able to find
     * detailed info and button actions.
     *
     * e.g.:
     *          column 1    column 2    column3     column N
     *          _____________________________________________
     * row 1  | title 1     info 1      action 3    action N
     * row 2  | title 2     ...
     * row N  |  ...
     */
    private class BooksGridPagerAdapter(private val mActivity: Activity) : FragmentGridPagerAdapter(mActivity.fragmentManager) {

        /**
         * Cursor used as books source.
         */
        private var mCursor: Cursor? = null

        override fun getFragment(row: Int, column: Int): Fragment {
            val cursor = mCursor
            // Checking if it's empty
            if (cursor == null || cursor.count == 0) {
                return when (column) {
                    EMPTY_INFO -> InfoFragment.Builder.create(R.string.no_suggestions).build()
                    EMPTY_SETTINGS -> SettingsFragment.Builder.create().build()
                    else -> throw IllegalArgumentException("getFragment(row=$row, column=$column)")
                }
            }
            // Positioning the cursor at the right row
            cursor.moveToPosition(row)
            // Matching the fragment by column
            return when (column) {
                TITLE -> CardFragment.create(cursor.string(Book.TITLE), cursor.string(Book.AUTHOR))
                INFO -> CardFragment.create(mActivity.getString(R.string.description), cursor.string(Book.DESCRIPTION))
                NOTES -> {
                    val notes = cursor.string(Book.NOTES)
                    if (notes.isNullOrEmpty()) {
                        // Button to catch a note
                        AddNoteFragment.Builder.create(cursor.long(Book._ID)).build()
                    } else {
                        CardFragment.create(mActivity.getString(R.string.notes), notes)
                    }
                }
                // Button to buy
                BUY -> BuyBookFragment.Builder.create(cursor.long(Book._ID)).build()
                SETTINGS -> SettingsFragment.Builder.create().build()
                else -> throw IllegalArgumentException("getFragment(row=$row, column=$column)")
            }
        }

        fun isCard(row: Int, column: Int): Boolean {
            val cursor = mCursor
            if (cursor == null || cursor.count == 0) {
                return false
            }
            return when {
                column < NOTES -> true
                column > NOTES -> false
                else -> {
                    cursor.moveToPosition(row)
                    !cursor.string(Book.NOTES).isNullOrEmpty()
                }
            }
        }

        override fun getRowCount(): Int = if (isEmpty()) EMPTY_ROW_COUNT else mCursor!!.count

        override fun getColumnCount(row: Int): Int = if (isEmpty()) EMPTY_COLUMN_COUNT else COLUMNS

        fun swapCursor(cursor: Cursor?, notify: Boolean = true): Cursor? {
            val oldCursor = mCursor
            mCursor = cursor
            if (notify) {
                notifyDataSetChanged()
            }
            return oldCursor
        }

        fun isEmpty(): Boolean = mCursor.let { it == null || it.count == 0 }

        fun getBookId(row: Int): Long {
            val cursor = mCursor
            if (cursor == null || cursor.count == 0) {
                return NOT_VALID.toLong()
            }
            // Positioning the cursor at the right row
            cursor.moveToPosition(row)
            return cursor.long(Book._ID)
        }

        fun getBaseDrawable(row: Int): Drawable? {
            val cursor = mCursor
            if (cursor == null || cursor.count == 0) {
                return ContextCompat.getDrawable(mActivity, R.drawable.tile_sad)
            }
            // Positioning the cursor at the right row
            cursor.moveToPosition(row)
            val tile = when (cursor.string(Book.TAG)) {
                "javascript" -> R.drawable.tile_javascript
                "java" -> R.drawable.tile_java
                "git" -> R.drawable.tile_git
                else -> R.drawable.tile_android
            }
            return ContextCompat.getDrawable(mActivity, tile)
        }

        private fun Cursor.string(column: String): String? = getString(getColumnIndexOrThrow(column))

        private fun Cursor.long(column: String): Long = getLong(getColumnIndexOrThrow(column))

        private companion object {
            /**
             * Columns for valid cursor.
             */
            const val TITLE = 0
            const val INFO = 1
            const val NOTES = 2
            const val BUY = 3
            const val SETTINGS = 4
            const val COLUMNS = 5

            /**
             * Columns for empty.
             */
            const val EMPTY_INFO = 0
            const val EMPTY_SETTINGS = 1
            const val EMPTY_ROW_COUNT = 1
            const val EMPTY_COLUMN_COUNT = 2
        }
    }

    private companion object {

        /**
         * Loader id.
         */
        const val LOADER_ID_SUGGESTIONS = 23

        /**
         * Usually we don't want to display more than 5 rows in the 2D Picker.
         * This is done because we don't want to display tons of content to the user.
         */
        const val MAX_ROWS = 5

        const val NOT_VALID = -1

        /**
         * Fading values.
         */
        const val NO_FADE = 0f
        const val PARTIAL_FADE = 0.8f

        /**
         * Query params.
         */
        val PROJECTION = arrayOf(Book._ID, Book.TITLE, Book.AUTHOR, Book.DESCRIPTION, Book.NOTES, Book.TAG)
        const val SELECTION = Book.OWNED + " = ?"
        val SELECTION_ARGS = arrayOf("0")
        val ORDER_BY: String? = null
    }
}
