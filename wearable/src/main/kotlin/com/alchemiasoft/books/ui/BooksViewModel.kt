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


package com.alchemiasoft.books.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alchemiasoft.books.service.BookService
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.data.BookRepository
import com.alchemiasoft.common.model.Book
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * State holder for the watch UI: the (few) books the user doesn't own yet, plus the actions on them.
 */
class BooksViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface State {
        data object Loading : State
        data class Loaded(val books: List<Book>) : State
    }

    val state: StateFlow<State> = BookRepository(application)
        // Usually we don't want to display more than 5 rows: we don't want to display tons of content to the user.
        .observeBooks(owned = false, limit = MAX_ROWS)
        .map<List<Book>, State> { State.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State.Loading)

    fun buy(book: Book) = BookService.Invoker.get(getApplication()).bookId(book.id).owned(true).invoke()

    fun addNotes(book: Book, notes: String) = BookService.Invoker.get(getApplication()).bookId(book.id).notes(notes).invoke()

    /**
     * The settings column (it shows how a list works on a watch).
     */
    fun applySetting(setting: Setting) {
        val invoker = BookService.Invoker.get(getApplication())
        when (setting) {
            Setting.SELL_OWNED -> invoker.where(WHERE_OWNED, "1").owned(false)
            Setting.BUY_ALL -> invoker.where(WHERE_OWNED, "0").owned(true)
            Setting.RESET -> invoker.notes(null).owned(false)
        }.invoke()
    }

    /**
     * Order matches R.array.settings.
     */
    enum class Setting { SELL_OWNED, BUY_ALL, RESET }

    private companion object {
        const val MAX_ROWS = 5
        const val WHERE_OWNED = BookDB.Book.OWNED + " = ?"
    }
}
