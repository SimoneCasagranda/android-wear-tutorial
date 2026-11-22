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
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alchemiasoft.book.R
import com.alchemiasoft.book.receiver.SuggestionReceiver
import com.alchemiasoft.book.ui.HomeScreen
import com.alchemiasoft.book.ui.HomeViewModel
import com.alchemiasoft.book.ui.theme.BooksTheme
import com.alchemiasoft.common.model.Book
import com.alchemiasoft.common.sync.Event
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

/**
 * Activity that decides weather to display available books, owned books or a single book.
 * The UI itself is built with Jetpack Compose (see [HomeScreen]).
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class HomeActivity : ComponentActivity(), MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private val mViewModel: HomeViewModel by viewModels()

    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // White status bar icons on top of the (cyan) app bar.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContent {
            BooksTheme {
                HomeScreen(
                    viewModel = mViewModel,
                    onSettingsClick = { Toast.makeText(this, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show() },
                    onSuggestBookClick = { SuggestionReceiver.scheduleSuggestion(this) },
                )
            }
        }
        // Checking if first instance and then showing the right content
        if (savedInstanceState == null) {
            handleIntent(intent)
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
        // The watch tells us which book the user is looking at.
        val receiver = Event.MessageApi.Receiver.from(messageEvent)
        if (receiver.action == Event.MessageApi.OPEN) {
            mViewModel.openBook(receiver.bookId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val bookId = intent.getLongExtra(KEY_BOOK_ID, NOT_VALID)
        if (bookId == NOT_VALID) {
            mViewModel.showList(HomeViewModel.Filter.ALL)
            // Scheduling a suggestion
            SuggestionReceiver.scheduleSuggestion(this)
        } else {
            mViewModel.openBook(bookId)
        }
    }

    companion object {

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
    }
}
