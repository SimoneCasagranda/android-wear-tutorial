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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SuccessConfirmationDialog
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.VerticalPageIndicator
import androidx.wear.compose.material3.confirmationDialogCurvedText
import com.alchemiasoft.books.R
import com.alchemiasoft.common.model.Book
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Root of the watch UI. Following the Wear design guidelines each book is a row (swipe vertically
 * between books) and its details and actions are columns (swipe horizontally):
 *
 *          column 1    column 2      column 3      column 4    column 5
 * row 1  | title 1     description   notes/add     buy         settings
 * row 2  | title 2     ...
 */
@Composable
fun BooksApp(
    state: BooksViewModel.State,
    onBookShown: (Book) -> Unit,
    onBuy: (Book) -> Unit,
    onAddNotes: (Book, String) -> Unit,
    onRequestNotes: ((String) -> Unit) -> Unit,
    onSetting: (BooksViewModel.Setting) -> Unit,
    onDone: () -> Unit,
) {
    MaterialTheme {
        // Lives above the rows: the bought book leaves the (not owned) list right away, taking its row with it.
        var purchased by remember { mutableStateOf(false) }
        val onBuyConfirmed: (Book) -> Unit = { book ->
            onBuy(book)
            purchased = true
        }
        AppScaffold {
            when (state) {
                BooksViewModel.State.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is BooksViewModel.State.Loaded ->
                    if (state.books.isEmpty()) {
                        EmptyRow(onSetting, onDone)
                    } else {
                        BookRows(state.books, onBookShown, onBuyConfirmed, onAddNotes, onRequestNotes, onSetting, onDone)
                    }
            }
            val style = ConfirmationDialogDefaults.curvedTextStyle
            val purchasedText = stringResource(R.string.item_purchased)
            SuccessConfirmationDialog(
                visible = purchased,
                // We want to close the wear app once the confirmation is gone
                onDismissRequest = onDone,
                curvedText = { confirmationDialogCurvedText(purchasedText, style) },
            )
        }
    }
}

@Composable
private fun BookRows(
    books: List<Book>,
    onBookShown: (Book) -> Unit,
    onBuy: (Book) -> Unit,
    onAddNotes: (Book, String) -> Unit,
    onRequestNotes: ((String) -> Unit) -> Unit,
    onSetting: (BooksViewModel.Setting) -> Unit,
    onDone: () -> Unit,
) {
    val rowsState = rememberPagerState(pageCount = { books.size })
    val currentBooks by rememberUpdatedState(books)
    val currentOnBookShown by rememberUpdatedState(onBookShown)
    // Notifying the phone about the book the user is looking at.
    LaunchedEffect(rowsState) {
        snapshotFlow { rowsState.currentPage }.distinctUntilChanged().collect { row ->
            currentBooks.getOrNull(row)?.let(currentOnBookShown)
        }
    }
    Box(Modifier.fillMaxSize()) {
        VerticalPager(state = rowsState, modifier = Modifier.testTag(TAG_ROWS), key = { books.getOrNull(it)?.id ?: it }) { row ->
            BookRow(books[row], onBuy, onAddNotes, onRequestNotes, onSetting, onDone)
        }
        if (books.size > 1) {
            VerticalPageIndicator(rowsState, Modifier.align(Alignment.CenterEnd))
        }
    }
}

/**
 * The columns of a single book.
 */
@Composable
private fun BookRow(
    book: Book,
    onBuy: (Book) -> Unit,
    onAddNotes: (Book, String) -> Unit,
    onRequestNotes: ((String) -> Unit) -> Unit,
    onSetting: (BooksViewModel.Setting) -> Unit,
    onDone: () -> Unit,
) {
    val columnsState = rememberPagerState(pageCount = { COLUMNS })
    val hasNotes = !book.notes.isNullOrEmpty()
    // Cards are shown on the plain tiles, actions on a darker (faded) background.
    val isCard = columnsState.currentPage < NOTES || (columnsState.currentPage == NOTES && hasNotes)
    Box(Modifier.fillMaxSize().tiledBackground(tileFor(book.tag), faded = !isCard)) {
        HorizontalPager(state = columnsState, modifier = Modifier.testTag("$TAG_COLUMNS${book.id}")) { column ->
            when (column) {
                TITLE -> BottomCard(title = book.title.orEmpty(), text = book.author.orEmpty())
                INFO -> ScrollingCard(title = stringResource(R.string.description), text = book.description.orEmpty())
                NOTES ->
                    if (hasNotes) {
                        ScrollingCard(title = stringResource(R.string.notes), text = book.notes.orEmpty())
                    } else {
                        AddNotesAction(onRequestNotes, onConfirmed = { notes ->
                            onAddNotes(book, notes)
                            // We want to close the wear app
                            onDone()
                        })
                    }
                BUY -> BuyAction(onConfirmed = { onBuy(book) })
                else -> SettingsList(onSetting, onDone)
            }
        }
        HorizontalPageIndicator(columnsState, Modifier.align(Alignment.BottomCenter))
    }
}

/**
 * Shown when there are no books to suggest: an info page and the settings.
 */
@Composable
private fun EmptyRow(onSetting: (BooksViewModel.Setting) -> Unit, onDone: () -> Unit) {
    val columnsState = rememberPagerState(pageCount = { EMPTY_COLUMNS })
    // No cards here: the background is always faded.
    Box(Modifier.fillMaxSize().tiledBackground(R.drawable.ic_sad, faded = true)) {
        HorizontalPager(state = columnsState) { column ->
            if (column == 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_suggestions), textAlign = TextAlign.Center, color = Color.White)
                }
            } else {
                SettingsList(onSetting, onDone)
            }
        }
        HorizontalPageIndicator(columnsState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun BottomCard(title: String, text: String) {
    Box(Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
        TitleCard(onClick = {}, title = { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis) }) {
            Text(text, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ScrollingCard(title: String, text: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 12.dp, end = 12.dp, top = 28.dp, bottom = 28.dp)) {
        TitleCard(onClick = {}, title = { Text(title) }) {
            Text(text)
        }
    }
}

/**
 * Round action button with a delayed confirmation: tap to start a 2 second countdown, tap again to cancel.
 */
@Composable
private fun DelayedConfirmationAction(
    iconRes: Int,
    label: String,
    confirming: Boolean,
    onClick: () -> Unit,
    onCountdownFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    val currentOnFinished by rememberUpdatedState(onCountdownFinished)
    LaunchedEffect(confirming) {
        progress.snapTo(0f)
        if (confirming) {
            // Runs on Compose's frame clock: cancelled automatically if the user taps again.
            progress.animateTo(1f, tween(durationMillis = DELAY_TIMEOUT_MS, easing = LinearEasing))
            currentOnFinished()
        }
    }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.size(ACTION_SIZE),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (confirming) {
                            CancelIcon(Modifier.size(36.dp))
                        } else {
                            Icon(painterResource(iconRes), contentDescription = label, tint = Color.White)
                        }
                    }
                }
                if (confirming) {
                    CircularProgressIndicator(progress = { progress.value }, modifier = Modifier.size(ACTION_SIZE + 12.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(label, color = Color.White, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

@Composable
private fun BuyAction(onConfirmed: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }
    DelayedConfirmationAction(
        iconRes = R.drawable.ic_action_buy,
        label = stringResource(R.string.buy),
        confirming = confirming,
        onClick = { confirming = !confirming },
        onCountdownFinished = {
            confirming = false
            onConfirmed()
        },
        modifier = Modifier.testTag(TAG_BUY),
    )
}

@Composable
private fun AddNotesAction(onRequestNotes: ((String) -> Unit) -> Unit, onConfirmed: (String) -> Unit) {
    var notes by remember { mutableStateOf<String?>(null) }
    DelayedConfirmationAction(
        iconRes = R.drawable.ic_action_notes,
        label = notes ?: stringResource(R.string.add_notes),
        confirming = notes != null,
        // Tap: dictate a note; tap during the countdown: cancel it.
        onClick = { if (notes != null) notes = null else onRequestNotes { notes = it } },
        onCountdownFinished = { notes?.let(onConfirmed) },
        modifier = Modifier.testTag(TAG_ADD_NOTES),
    )
}

@Composable
private fun SettingsList(onSetting: (BooksViewModel.Setting) -> Unit, onDone: () -> Unit) {
    val labels = stringArrayResource(R.array.settings)
    TransformingLazyColumn(
        modifier = Modifier.fillMaxSize().testTag(TAG_SETTINGS),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        itemsIndexed(BooksViewModel.Setting.entries) { index, setting ->
            Button(
                onClick = {
                    onSetting(setting)
                    onDone()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(labels.getOrElse(index) { setting.name }) },
            )
        }
    }
}

@Composable
private fun CancelIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = size.minDimension / 8
        drawLine(Color.White, start = center.copy(x = 0f, y = 0f), end = center.copy(x = size.width, y = size.height), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(Color.White, start = center.copy(x = size.width, y = 0f), end = center.copy(x = 0f, y = size.height), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/**
 * Repeats the given image as background, optionally darkened like the original "fading" drawable.
 */
@Composable
private fun Modifier.tiledBackground(imageRes: Int, faded: Boolean): Modifier {
    val image: ImageBitmap = ImageBitmap.imageResource(imageRes)
    val brush = remember(image) { ShaderBrush(ImageShader(image, TileMode.Repeated, TileMode.Repeated)) }
    return this.background(brush).then(if (faded) Modifier.background(FadeColor) else Modifier)
}

private fun tileFor(tag: String?): Int = when (tag) {
    "javascript" -> R.drawable.ic_javascript
    "java" -> R.drawable.ic_java
    "git" -> R.drawable.ic_git
    else -> R.drawable.ic_android
}

/**
 * Columns for a book.
 */
private const val TITLE = 0
private const val INFO = 1
private const val NOTES = 2
private const val BUY = 3
private const val COLUMNS = 5
private const val EMPTY_COLUMNS = 2

/**
 * Timeout delay for the confirmations.
 */
private const val DELAY_TIMEOUT_MS = 2000

private val ACTION_SIZE = 110.dp
private val ActionBlue = Color(0xFF0A5AE1)
private val FadeColor = Color(0xCC464646)

const val TAG_ROWS = "book_rows"
const val TAG_COLUMNS = "book_columns_"
const val TAG_BUY = "buy_action"
const val TAG_ADD_NOTES = "add_notes_action"
const val TAG_SETTINGS = "settings_list"
