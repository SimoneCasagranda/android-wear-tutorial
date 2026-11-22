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


// The legacy Wear support library (FragmentGridPagerAdapter) works with framework fragments.
@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.alchemiasoft.books.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import com.alchemiasoft.books.R

/**
 * Fragment that displays a simple info place holder.
 *
 * Created by Simone Casagranda on 27/01/15.
 */
class InfoFragment : Fragment() {

    /**
     * Builder for the InfoFragment.
     */
    class Builder private constructor() {

        private val mArgs = Bundle()

        fun build(): InfoFragment = InfoFragment().apply { arguments = mArgs }

        companion object {
            fun create(@StringRes textId: Int): Builder = Builder().apply { mArgs.putInt(ARG_TEXT_RES_ID, textId) }
        }
    }

    private lateinit var mTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        mTextView = view.findViewById(R.id.text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTextView.setText(arguments?.getInt(ARG_TEXT_RES_ID, R.string.unknown) ?: R.string.unknown)
    }

    private companion object {
        /**
         * Params.
         */
        const val ARG_TEXT_RES_ID = "com.alchemiasoft.books.fragment.info.TEXT_RES_ID"
    }
}
