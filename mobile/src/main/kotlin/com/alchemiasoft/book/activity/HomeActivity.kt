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


// The tutorial uses the GoogleApiClient-based Wearable APIs (deprecated, still shipped in play-services-wearable).
@file:Suppress("DEPRECATION")

package com.alchemiasoft.book.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alchemiasoft.book.R
import com.alchemiasoft.book.fragment.BookDetailFragment
import com.alchemiasoft.book.fragment.BooksFragment
import com.alchemiasoft.book.receiver.SuggestionReceiver
import com.alchemiasoft.common.model.Book
import com.alchemiasoft.common.sync.Event
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

/**
 * Activity that decides weather to display available books or owned books.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class HomeActivity : AppCompatActivity(), MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        applySystemBarInsets(toolbar, findViewById(R.id.content))
        toolbar.setOnMenuItemClickListener { menuItem ->
            val fm = supportFragmentManager
            when (menuItem.itemId) {
                R.id.action_all_books -> {
                    if (fm.findFragmentByTag(TAG_ALL_BOOKS) == null) {
                        fm.beginTransaction().replace(R.id.content, BooksFragment.create(false), TAG_ALL_BOOKS).commit()
                    } else {
                        Log.d(TAG_LOG, "All books fragment is already attached.")
                    }
                    true
                }
                R.id.action_my_books -> {
                    if (fm.findFragmentByTag(TAG_OWNED_BOOKS) == null) {
                        fm.beginTransaction().replace(R.id.content, BooksFragment.create(true), TAG_OWNED_BOOKS).commit()
                    } else {
                        Log.d(TAG_LOG, "Owned books fragment is already attached.")
                    }
                    true
                }
                R.id.action_settings -> {
                    Toast.makeText(this, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_suggestion -> {
                    // Scheduling a suggestion
                    SuggestionReceiver.scheduleSuggestion(this)
                    true
                }
                else -> {
                    Log.w(TAG_LOG, "MenuItem: title=" + menuItem.title + " & itemId=" + menuItem.itemId + " is not handled.")
                    false
                }
            }
        }
        toolbar.inflateMenu(R.menu.menu_home)
        // Checking if first instance and then attach the first fragment
        if (savedInstanceState == null) {
            attachBookFragment(intent.getLongExtra(KEY_BOOK_ID, NOT_VALID))
        }
        // Since Android 13 notifications (book suggestions, purchase confirmations) need a runtime permission
        requestNotificationPermissionIfNeeded()
        // Creating the GoogleApiClient for the Wearable api (if available)
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onStart() {
        super.onStart()
        // Trying to bind to the GoogleApiClient
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        // Disconnecting the client and releasing the wearable updates
        mGoogleApiClient?.let {
            Wearable.MessageApi.removeListener(it, this)
            it.disconnect()
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        // The Client is connected => we can register for the wearable updates
        mGoogleApiClient?.let { Wearable.MessageApi.addListener(it, this) }
    }

    override fun onConnectionSuspended(cause: Int) {
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val receiver = Event.MessageApi.Receiver.from(messageEvent)
        if (receiver.action == Event.MessageApi.OPEN) {
            attachBookFragment(receiver.bookId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        attachBookFragment(intent.getLongExtra(KEY_BOOK_ID, NOT_VALID))
    }

    private fun attachBookFragment(bookId: Long) {
        if (bookId == NOT_VALID) {
            supportFragmentManager.beginTransaction().replace(R.id.content, BooksFragment.create(false), TAG_ALL_BOOKS).commit()
            // Scheduling a suggestion
            SuggestionReceiver.scheduleSuggestion(this)
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.content, BookDetailFragment.create(bookId)).commit()
        }
    }

    companion object {

        /**
         * Tag used for logging purposes.
         */
        private val TAG_LOG = HomeActivity::class.java.simpleName

        /**
         * Tag(s) used for fragment transactions.
         */
        private const val TAG_ALL_BOOKS = "all_books"
        private const val TAG_OWNED_BOOKS = "owned_books"

        /**
         * Key for the book Id.
         */
        private const val KEY_BOOK_ID = "com.alchemiasoft.book.activity.BOOK_ID"
        private const val NOT_VALID = -1L

        /**
         * Intent that should be used to launch HomeActivity for a given book.
         *
         * @param context reference.
         * @param book    that has to be passed.
         * @return an intent ready to be used to pass info to the HomeActivity.
         */
        fun createFor(context: Context, book: Book): Intent =
            Intent(context, HomeActivity::class.java).putExtra(KEY_BOOK_ID, book.id)

        /**
         * Since Android 15 apps are drawn edge-to-edge: keep the toolbar below the status bar
         * and the content above the navigation bar.
         */
        private fun applySystemBarInsets(toolbar: View, content: View) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                v.setPadding(bars.left, bars.top, bars.right, 0)
                content.setPadding(bars.left, 0, bars.right, bars.bottom)
                insets
            }
        }
    }
}
