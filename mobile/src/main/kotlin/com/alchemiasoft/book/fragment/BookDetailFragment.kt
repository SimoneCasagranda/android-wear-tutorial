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


package com.alchemiasoft.book.fragment

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.alchemiasoft.book.R
import com.alchemiasoft.book.service.BookActionService
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book

/**
 * Fragment that shows a book in full details.
 *
 * Created by Simone Casagranda on 27/12/14.
 */
class BookDetailFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var mTitleTextView: TextView
    private lateinit var mAuthorTextView: TextView
    private lateinit var mPagesTextView: TextView
    private lateinit var mSourceTextView: TextView
    private lateinit var mDescriptionTextView: TextView
    private lateinit var mNotesTextView: TextView
    private lateinit var mActionButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_book, container, false)
        mTitleTextView = view.findViewById(R.id.title)
        mAuthorTextView = view.findViewById(R.id.author)
        mSourceTextView = view.findViewById(R.id.source)
        mDescriptionTextView = view.findViewById(R.id.description)
        mPagesTextView = view.findViewById(R.id.pages)
        mNotesTextView = view.findViewById(R.id.notes)
        mActionButton = view.findViewById(R.id.action)
        return view
    }

    override fun onResume() {
        super.onResume()
        LoaderManager.getInstance(this).initLoader(ID_LOADER_BOOK, arguments, this)
    }

    override fun onPause() {
        super.onPause()
        LoaderManager.getInstance(this).destroyLoader(ID_LOADER_BOOK)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        require(id == ID_LOADER_BOOK) { "Id=$id is not supported." }
        return CursorLoader(requireContext(), BookDB.Book.create(requireArguments().getLong(ARG_BOOK_ID)), null, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        if (!data.moveToNext()) {
            return
        }
        val book = Book.oneFrom(data)
        mTitleTextView.text = book.title
        mAuthorTextView.text = book.author
        mSourceTextView.text = getString(R.string.source, book.source)
        mDescriptionTextView.text = book.description
        mNotesTextView.text = book.notes
        mPagesTextView.text = getString(R.string.pages, if (book.pages >= 0) book.pages else getString(R.string.unknown))
        mActionButton.setText(if (book.isOwned) R.string.action_sell else R.string.action_buy)
        mActionButton.setOnClickListener {
            mActionButton.isEnabled = false
            val context = requireContext()
            val builder = if (book.isOwned) BookActionService.IntentBuilder.sell(context, book) else BookActionService.IntentBuilder.buy(context, book)
            context.startService(builder.build())
        }
        mActionButton.isEnabled = true
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }

    companion object {

        /**
         * Argument used to set/get the book id.
         */
        private const val ARG_BOOK_ID = "com.alchemiasoft.book.fragment.BOOK_ID"

        /**
         * Loader id for book.
         */
        private const val ID_LOADER_BOOK = 23

        /**
         * Creates a new BookDetailFragment for the given book id.
         *
         * @param bookId of the book that has to be shown.
         * @return the BookDetailFragment ready to be attached.
         */
        fun create(bookId: Long): BookDetailFragment = BookDetailFragment().apply {
            arguments = bundleOf(ARG_BOOK_ID to bookId)
        }
    }
}
