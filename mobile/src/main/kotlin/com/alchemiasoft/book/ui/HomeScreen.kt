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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.CompositionLocalProvider
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
    val books by viewModel.books.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    // Hoisted so coming back from a book returns to the same scroll position (and the same card).
    val allBooksGrid = rememberLazyStaggeredGridState()
    val ownedBooksGrid = rememberLazyStaggeredGridState()

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
        // The list and the detail cross-fade while the tapped cover grows into the detail header.
        SharedTransitionLayout(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = selectedBookId,
                transitionSpec = { fadeIn(tween(TRANSITION_MS)) togetherWith fadeOut(tween(TRANSITION_MS)) },
                label = "list-detail",
            ) { bookId ->
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this,
                ) {
                    if (bookId == null) {
                        when (val state = books) {
                            HomeViewModel.BooksState.Loading -> Loading()
                            is HomeViewModel.BooksState.Loaded -> BookList(
                                books = state.books,
                                onBookClick = { viewModel.openBook(it.id) },
                                onBuyClick = viewModel::buy,
                                onSellClick = viewModel::sell,
                                gridState = if (filter == HomeViewModel.Filter.OWNED) ownedBooksGrid else allBooksGrid,
                                modifier = Modifier.testTag(if (filter == HomeViewModel.Filter.OWNED) TAG_OWNED_LIST else TAG_ALL_LIST),
                            )
                        }
                    } else {
                        // Use the book the list already has, so the detail is there on the first frame
                        // (the shared cover needs a target) while the provider query catches up.
                        val book = (detail as? HomeViewModel.DetailState.Loaded)?.book?.takeIf { it.id == bookId }
                            ?: (books as? HomeViewModel.BooksState.Loaded)?.books?.firstOrNull { it.id == bookId }
                        when {
                            book != null -> BookDetail(book = book, onBuyClick = viewModel::buy, onSellClick = viewModel::sell)
                            // The book doesn't exist (anymore): nothing to show, like the original fragment.
                            detail == HomeViewModel.DetailState.Missing(bookId) -> Unit
                            else -> Loading()
                        }
                    }
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

private const val TRANSITION_MS = 400

const val TAG_ALL_LIST = "all_books_list"
const val TAG_OWNED_LIST = "owned_books_list"
