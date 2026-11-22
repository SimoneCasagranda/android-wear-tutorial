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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alchemiasoft.book.R

/**
 * Root of the phone UI: the toolbar plus either the list of books or the detail of the open book.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSettingsClick: () -> Unit,
    onSuggestBookClick: () -> Unit,
) {
    val selectedBookId by viewModel.selectedBookId.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    // Back from the detail goes back to the list.
    BackHandler(enabled = selectedBookId != null) { viewModel.closeBook() }

    Scaffold(
        topBar = {
            HomeTopBar(
                onAllBooksClick = { viewModel.showList(HomeViewModel.Filter.ALL) },
                onMyBooksClick = { viewModel.showList(HomeViewModel.Filter.OWNED) },
                onSettingsClick = onSettingsClick,
                onSuggestBookClick = onSuggestBookClick,
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (selectedBookId == null) {
                val books by viewModel.books.collectAsStateWithLifecycle()
                when (val state = books) {
                    HomeViewModel.BooksState.Loading -> Loading()
                    is HomeViewModel.BooksState.Loaded -> BookList(
                        books = state.books,
                        onBookClick = { viewModel.openBook(it.id) },
                        onBuyClick = viewModel::buy,
                        onSellClick = viewModel::sell,
                        modifier = Modifier.testTag(if (filter == HomeViewModel.Filter.OWNED) TAG_OWNED_LIST else TAG_ALL_LIST),
                    )
                }
            } else {
                val detail by viewModel.detail.collectAsStateWithLifecycle()
                when (val state = detail) {
                    HomeViewModel.DetailState.Loading -> Loading()
                    is HomeViewModel.DetailState.Loaded -> BookDetail(
                        book = state.book,
                        onBuyClick = viewModel::buy,
                        onSellClick = viewModel::sell,
                    )
                    // The book doesn't exist (anymore): nothing to show, like the original fragment.
                    HomeViewModel.DetailState.Missing -> Unit
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onAllBooksClick: () -> Unit,
    onMyBooksClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSuggestBookClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    TopAppBar(
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = onPrimary,
        ),
        actions = {
            TextButton(onClick = onAllBooksClick) {
                Text(stringResource(R.string.all_books).uppercase(), color = onPrimary)
            }
            TextButton(onClick = onMyBooksClick) {
                Text(stringResource(R.string.owned_books).uppercase(), color = onPrimary)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(painterResource(R.drawable.ic_action_overflow), contentDescription = stringResource(R.string.more_options), tint = onPrimary)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_settings)) },
                        onClick = {
                            menuExpanded = false
                            onSettingsClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_suggestion)) },
                        onClick = {
                            menuExpanded = false
                            onSuggestBookClick()
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

const val TAG_ALL_LIST = "all_books_list"
const val TAG_OWNED_LIST = "owned_books_list"
