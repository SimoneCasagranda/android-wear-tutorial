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

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alchemiasoft.book.R
import com.alchemiasoft.book.fragment.base.RecyclerViewFragment
import com.alchemiasoft.book.service.BookActionService
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.model.Book

/**
 * Fragment that displays books to the users.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class BooksFragment : RecyclerViewFragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private val mAdapter = BooksAdapter { book ->
        // Called when the user selects a book.
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.content, BookDetailFragment.create(book.id))?.commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEmptyText(getString(R.string.no_books))
        // Set up adapter and recycler view
        setRecyclerAdapter(mAdapter)
        recyclerView.layoutManager = StaggeredGridLayoutManager(COLUMN_COUNT, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.setHasFixedSize(true)
        // We don't need any scroll update
        setScrollInterceptor { true }
    }

    override fun onResume() {
        super.onResume()
        // Initialising the loader
        LoaderManager.getInstance(this).initLoader(ID_LOADER_BOOKS, arguments, this)
    }

    override fun onPause() {
        super.onPause()
        LoaderManager.getInstance(this).destroyLoader(ID_LOADER_BOOKS)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        require(id == ID_LOADER_BOOKS) { "loader id=$id is not supported!" }
        val owned = args?.getBoolean(KEY_OWNED) ?: false
        return if (owned) {
            CursorLoader(requireContext(), BookDB.Book.CONTENT_URI, null, SELECTION, SELECT_OWNED, null)
        } else {
            CursorLoader(requireContext(), BookDB.Book.CONTENT_URI, null, null, null, null)
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mAdapter.swap(Book.allFrom(data))
        if (isResumed) {
            setContentShown(true)
        } else {
            setContentShownNoAnimation(true)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mAdapter.swap(null)
    }

    /**
     * Holder that has got references to the UI for a book entry.
     */
    private class BookHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val authorTextView: TextView = itemView.findViewById(R.id.author)
        val ownedView: View = itemView.findViewById(R.id.owned_book)
        val overflowView: View = itemView.findViewById(R.id.menu_overflow)
    }

    /**
     * Adapter that takes care of displaying the right data in the BookHolder(s)
     * based on the current/expected Book entries.
     */
    private class BooksAdapter(private val onBookSelected: (Book) -> Unit) : RecyclerView.Adapter<BookHolder>() {

        /**
         * Books currently shown.
         */
        private val mBooks = mutableListOf<Book>()

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BookHolder =
            BookHolder(View.inflate(viewGroup.context, R.layout.item_book, null))

        override fun onBindViewHolder(bookHolder: BookHolder, pos: Int) {
            val book = mBooks[pos]
            bookHolder.titleTextView.text = book.title
            bookHolder.authorTextView.text = book.author
            if (book.isOwned) {
                bookHolder.ownedView.setBackgroundResource(R.drawable.owned_book)
            } else {
                bookHolder.ownedView.background = null
            }
            bookHolder.itemView.setOnClickListener { onBookSelected(book) }
            bookHolder.overflowView.setOnClickListener { v ->
                val popup = PopupMenu(v.context, v, Gravity.BOTTOM)
                popup.menuInflater.inflate(R.menu.menu_book, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    val context = v.context
                    when (menuItem.itemId) {
                        R.id.action_buy -> {
                            if (book.isOwned) {
                                Toast.makeText(context, context.getString(R.string.book_already_owned), Toast.LENGTH_SHORT).show()
                            } else {
                                context.startService(BookActionService.IntentBuilder.buy(context, book).build())
                            }
                            true
                        }
                        R.id.action_sell -> {
                            if (!book.isOwned) {
                                Toast.makeText(context, context.getString(R.string.book_not_owned), Toast.LENGTH_SHORT).show()
                            } else {
                                context.startService(BookActionService.IntentBuilder.sell(context, book).build())
                            }
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        override fun getItemCount(): Int = mBooks.size

        override fun getItemId(pos: Int): Long = mBooks[pos].id

        @SuppressLint("NotifyDataSetChanged") // The whole list is replaced on every load.
        fun swap(books: List<Book>?) {
            mBooks.clear()
            books?.let { mBooks.addAll(it) }
            notifyDataSetChanged()
        }
    }

    companion object {

        /**
         * Key used to pass the owner filter. This parameter is completely optional.
         */
        private const val KEY_OWNED = "com.alchemiasoft.book.OWNED"

        /**
         * Number of columns shown to the user.
         */
        private const val COLUMN_COUNT = 2

        /**
         * Id used for the book(s) loader.
         */
        private const val ID_LOADER_BOOKS = 23

        private const val SELECTION = BookDB.Book.OWNED + " = ?"
        private val SELECT_OWNED = arrayOf("1")

        /**
         * Creates a new instance of BooksFragment.
         *
         * @param owned true to get only the owned books, false otherwise.
         * @return an instance of BooksFragment ready to be attached.
         */
        fun create(owned: Boolean): BooksFragment = BooksFragment().apply {
            arguments = bundleOf(KEY_OWNED to owned)
        }
    }
}
