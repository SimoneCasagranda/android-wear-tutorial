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


// The legacy Wear support library (FragmentGridPagerAdapter, WearableListView) works with framework fragments.
@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.alchemiasoft.books.fragment

import android.app.Fragment
import android.os.Bundle
import android.support.wearable.view.WearableListView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alchemiasoft.books.R
import com.alchemiasoft.books.service.BookService
import com.alchemiasoft.common.content.BookDB

/**
 * Fragment that displays how a WearableListView works.
 *
 * Created by Simone Casagranda on 27/01/15.
 */
class SettingsFragment : Fragment(), WearableListView.ClickListener {

    /**
     * Builder for the SettingsFragment.
     */
    class Builder private constructor() {

        private val mArgs = Bundle()

        fun build(): SettingsFragment = SettingsFragment().apply { arguments = mArgs }

        companion object {
            fun create(): Builder = Builder()
        }
    }

    private lateinit var mWearableListView: WearableListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        mWearableListView = view.findViewById(R.id.list)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Used to allow the WearableListView to intercept all the touches
        // preventing the parent to handle them
        mWearableListView.setGreedyTouchMode(true)
        // Creating and setting the adapter used to show the available settings
        mWearableListView.adapter = Adapter(resources.getStringArray(R.array.settings).toList())
        mWearableListView.setClickListener(this)
    }

    override fun onClick(viewHolder: WearableListView.ViewHolder) {
        val activity = activity ?: return
        when (viewHolder.adapterPosition) {
            // Sell all the owned books
            0 -> BookService.Invoker.get(activity).where(WHERE_OWNED, OWNED).owned(false).invoke()
            // Buy all the available books
            1 -> BookService.Invoker.get(activity).where(WHERE_OWNED, NOT_OWNED).owned(true).invoke()
            // Resetting the DB
            2 -> BookService.Invoker.get(activity).notes(null).owned(false).invoke()
            else -> return
        }
        activity.finish()
    }

    override fun onTopEmptyRegionClick() {
    }

    private class Holder(itemView: View) : WearableListView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
    }

    private class Adapter(private val mData: List<String>) : WearableListView.Adapter() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WearableListView.ViewHolder =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false))

        override fun onBindViewHolder(viewHolder: WearableListView.ViewHolder, position: Int) {
            (viewHolder as Holder).textView.text = mData[position]
        }

        override fun getItemCount(): Int = mData.size
    }

    private companion object {
        /**
         * DB Query params.
         */
        const val WHERE_OWNED = BookDB.Book.OWNED + " = ?"
        const val OWNED = "1"
        const val NOT_OWNED = "0"
    }
}
