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


// The phone notification uses the GoogleApiClient-based Wearable APIs (deprecated, still shipped in play-services-wearable).
@file:Suppress("DEPRECATION")

package com.alchemiasoft.books.activity

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.alchemiasoft.books.R
import com.alchemiasoft.books.ui.BooksApp
import com.alchemiasoft.books.ui.BooksViewModel
import com.alchemiasoft.common.model.Book
import com.alchemiasoft.common.sync.Event
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable

/**
 * Activity that displays some of the info about all the available books.
 * The UI itself is built with Compose for Wear OS (see [BooksApp]).
 */
class BooksActivity : ComponentActivity(), GoogleApiClient.ConnectionCallbacks {

    private val mViewModel: BooksViewModel by viewModels()

    /**
     * Client used by the wearable api to notify about the current book.
     */
    private var mGoogleApiClient: GoogleApiClient? = null

    /**
     * Book the user is currently looking at.
     */
    private var mShownBookId: Long? = null

    /**
     * Callback waiting for the result of the free-form speech recognition.
     */
    private var mOnNotes: ((String) -> Unit)? = null

    private val mSpeechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val notes = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (result.resultCode == RESULT_OK && !notes.isNullOrEmpty()) {
            mOnNotes?.invoke(notes)
        }
        mOnNotes = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by mViewModel.state.collectAsStateWithLifecycle()
            BooksApp(
                state = state,
                onBookShown = ::onBookShown,
                onBuy = mViewModel::buy,
                onAddNotes = mViewModel::addNotes,
                onRequestNotes = ::requestNotes,
                onSetting = mViewModel::applySetting,
                onDone = ::finish,
            )
        }
        // Creating the GoogleApiClient for the Wearable api (if available)
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build()
        }
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

    override fun onConnected(bundle: Bundle?) {
        mShownBookId?.let(::notifyPhoneAbout)
    }

    override fun onConnectionSuspended(status: Int) {
    }

    private fun onBookShown(book: Book) {
        mShownBookId = book.id
        // Notifying that the user is looking at this book.
        if (mGoogleApiClient?.isConnected == true) {
            notifyPhoneAbout(book.id)
        }
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
     * Starts the free-form speech recognition used to dictate notes.
     */
    private fun requestNotes(onNotes: (String) -> Unit) {
        mOnNotes = onNotes
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.what_notes))
        mSpeechLauncher.launch(intent)
    }
}
