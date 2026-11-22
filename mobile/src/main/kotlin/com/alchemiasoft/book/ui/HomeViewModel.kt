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


package com.alchemiasoft.book.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alchemiasoft.book.R
import com.alchemiasoft.book.service.BookActionService
import com.alchemiasoft.common.data.BookRepository
import com.alchemiasoft.common.model.Book
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * State holder for the phone UI: which list is shown (all / owned books) and which book is open.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application, private val savedState: SavedStateHandle) : AndroidViewModel(application) {

    enum class Filter { ALL, OWNED }

    sealed interface BooksState {
        data object Loading : BooksState
        data class Loaded(val books: List<Book>) : BooksState
    }

    sealed interface DetailState {
        data object Loading : DetailState
        data class Loaded(val book: Book) : DetailState
        data class Missing(val bookId: Long?) : DetailState
    }

    private val mRepository = BookRepository(application)

    val filter: StateFlow<Filter> = savedState.getStateFlow(KEY_FILTER, Filter.ALL)

    /**
     * Id of the book shown in detail, or null when the list is shown.
     */
    val selectedBookId: StateFlow<Long?> = savedState.getStateFlow(KEY_SELECTED_BOOK, null)

    val books: StateFlow<BooksState> = filter
        .flatMapLatest { filter -> mRepository.observeBooks(owned = if (filter == Filter.OWNED) true else null) }
        .map<List<Book>, BooksState> { BooksState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), BooksState.Loading)

    val detail: StateFlow<DetailState> = selectedBookId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(DetailState.Missing(null))
            } else {
                mRepository.observeBook(id).map { book -> if (book != null) DetailState.Loaded(book) else DetailState.Missing(id) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DetailState.Loading)

    fun showList(filter: Filter) {
        savedState[KEY_FILTER] = filter
        savedState[KEY_SELECTED_BOOK] = null
    }

    fun openBook(bookId: Long) {
        savedState[KEY_SELECTED_BOOK] = bookId
    }

    fun closeBook() {
        savedState[KEY_SELECTED_BOOK] = null
    }

    fun buy(book: Book) {
        val context = getApplication<Application>()
        if (book.isOwned) {
            Toast.makeText(context, context.getString(R.string.book_already_owned), Toast.LENGTH_SHORT).show()
        } else {
            context.startService(BookActionService.IntentBuilder.buy(context, book).build())
        }
    }

    fun sell(book: Book) {
        val context = getApplication<Application>()
        if (!book.isOwned) {
            Toast.makeText(context, context.getString(R.string.book_not_owned), Toast.LENGTH_SHORT).show()
        } else {
            context.startService(BookActionService.IntentBuilder.sell(context, book).build())
        }
    }

    private companion object {
        const val KEY_FILTER = "filter"
        const val KEY_SELECTED_BOOK = "selected_book"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
