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


// The legacy Wear support library (FragmentGridPagerAdapter, DelayedConfirmationView) works with framework fragments.
@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.alchemiasoft.books.fragment

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.wearable.view.DelayedConfirmationView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alchemiasoft.books.R
import com.alchemiasoft.books.service.BookService

/**
 * Fragment that allows to add some notes to a book.
 *
 * Created by Simone Casagranda on 25/01/15.
 */
class AddNoteFragment : Fragment(), DelayedConfirmationView.DelayedConfirmationListener {

    /**
     * Allows to build AddNoteFragment in an extensible way.
     */
    class Builder private constructor() {

        private val mArgs = Bundle()

        fun build(): AddNoteFragment = AddNoteFragment().apply { arguments = mArgs }

        companion object {
            fun create(id: Long): Builder = Builder().apply { mArgs.putLong(ARG_ID, id) }
        }
    }

    private lateinit var mConfirmationView: DelayedConfirmationView
    private lateinit var mAddNotesTextView: TextView
    private var mIsAnimating = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_add_note, container, false)
        mConfirmationView = view.findViewById(R.id.confirm_notes)
        mAddNotesTextView = view.findViewById(R.id.add_notes_label)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The entire "screen" is listening for the click
        view.setOnClickListener {
            if (mIsAnimating) {
                mConfirmationView.setImageResource(R.drawable.ic_action_notes)
                mIsAnimating = false
                return@setOnClickListener
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.what_notes))
            startActivityForResult(intent, REQUEST_NOTES_CODE)
        }
        mConfirmationView.setTotalTimeMs(DELAY_TIMEOUT)
        // Registering the listener triggered by the DelayedConfirmationView
        mConfirmationView.setListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_NOTES_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        val voiceNotes = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: return
        mAddNotesTextView.text = voiceNotes
        mIsAnimating = true
        mConfirmationView.setImageResource(R.drawable.ic_full_cancel)
        mConfirmationView.start()
    }

    override fun onTimerFinished(view: View) {
        val activity = activity
        if (activity == null) {
            Log.e(TAG_LOG, "Fragment not attached anymore to the activity (Skipping action).")
            return
        }
        // Updating the book state
        val bookId = arguments.getLong(ARG_ID)
        val notes = mAddNotesTextView.text.toString()
        // Scheduling the job in on the BookService
        BookService.Invoker.get(activity).bookId(bookId).notes(notes).invoke()
        // We want to close the wear app
        activity.finish()
    }

    override fun onTimerSelected(view: View) {
        mAddNotesTextView.setText(R.string.add_notes)
        mConfirmationView.reset()
    }

    private companion object {
        /**
         * Tag used for logging.
         */
        val TAG_LOG: String = AddNoteFragment::class.java.simpleName

        /**
         * Arguments params.
         */
        const val ARG_ID = "com.alchemiasoft.books.fragment.book.ID"

        /**
         * Code use to request the free-form speech
         */
        const val REQUEST_NOTES_CODE = 23

        /**
         * Timeout delay for confirmation.
         */
        const val DELAY_TIMEOUT = 2000L
    }
}
