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

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.ConfirmationActivity
import android.support.wearable.view.DelayedConfirmationView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alchemiasoft.books.R
import com.alchemiasoft.books.service.BookService

/**
 * Fragment that allows to buy a book.
 *
 * Created by Simone Casagranda on 24/01/15.
 */
class BuyBookFragment : Fragment(), DelayedConfirmationView.DelayedConfirmationListener {

    /**
     * Allows to build BuyBookFragment in an extensible way.
     */
    class Builder private constructor() {

        private val mArgs = Bundle()

        fun build(): BuyBookFragment = BuyBookFragment().apply { arguments = mArgs }

        companion object {
            fun create(id: Long): Builder = Builder().apply { mArgs.putLong(ARG_ID, id) }
        }
    }

    private lateinit var mConfirmationView: DelayedConfirmationView
    private var mIsAnimating = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_buy_book, container, false)
        mConfirmationView = view.findViewById(R.id.confirm_buy)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The entire "screen" is listening for the click
        view.setOnClickListener {
            if (mIsAnimating) {
                mConfirmationView.setImageResource(R.drawable.ic_action_buy)
                mIsAnimating = false
                return@setOnClickListener
            }
            mIsAnimating = true
            mConfirmationView.setImageResource(R.drawable.ic_full_cancel)
            mConfirmationView.setTotalTimeMs(DELAY_TIMEOUT)
            mConfirmationView.start()
        }
        // Registering the listener triggered by the DelayedConfirmationView
        mConfirmationView.setListener(this)
    }

    override fun onTimerFinished(view: View) {
        mIsAnimating = false
        val activity = activity
        if (activity == null) {
            Log.e(TAG_LOG, "Fragment not attached anymore to the activity (Skipping action).")
            return
        }
        // Updating the book state
        val bookId = arguments.getLong(ARG_ID)
        BookService.Invoker.get(activity).bookId(bookId).owned(true).invoke()
        // Starting the confirmation screen
        val intent = Intent(activity, ConfirmationActivity::class.java)
            .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
            .putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.item_purchased))
        startActivity(intent)
        activity.finish()
    }

    override fun onTimerSelected(view: View) {
        mConfirmationView.reset()
    }

    private companion object {
        /**
         * Tag used for logging.
         */
        val TAG_LOG: String = BuyBookFragment::class.java.simpleName

        /**
         * Timeout delay for confirmation.
         */
        const val DELAY_TIMEOUT = 2000L

        /**
         * Arguments params.
         */
        const val ARG_ID = "com.alchemiasoft.books.fragment.book.ID"
    }
}
