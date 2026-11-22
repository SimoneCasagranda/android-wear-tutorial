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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemiasoft.book.R
import com.alchemiasoft.book.ui.theme.BooksColors
import com.alchemiasoft.common.model.Book

/**
 * Two-column staggered grid of book cards, or an empty message.
 */
@Composable
fun BookList(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onBuyClick: (Book) -> Unit,
    onSellClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (books.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_books), style = MaterialTheme.typography.headlineSmall)
        }
        return
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(COLUMN_COUNT),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(6.dp),
        verticalItemSpacing = 6.dp,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(books, key = { it.id }) { book ->
            BookCard(book, onClick = { onBookClick(book) }, onBuyClick = { onBuyClick(book) }, onSellClick = { onSellClick(book) })
        }
    }
}

@Composable
private fun BookCard(book: Book, onClick: () -> Unit, onBuyClick: () -> Unit, onSellClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().testTag("book_${book.id}"),
    ) {
        // Same layout as the original card: menu top-right, "owned" marker bottom-right.
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(start = 8.dp, top = 12.dp, end = 44.dp, bottom = 8.dp)) {
                Text(book.title.orEmpty(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    book.author.orEmpty(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(Modifier.align(Alignment.TopEnd)) {
                BookMenu(onBuyClick, onSellClick)
            }
            if (book.isOwned) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(10.dp)
                        .background(BooksColors.Accent)
                        .semantics { contentDescription = OWNED_DESCRIPTION },
                )
            }
        }
    }
}

@Composable
private fun BookMenu(onBuyClick: () -> Unit, onSellClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painterResource(R.drawable.ic_action_overflow),
                contentDescription = stringResource(R.string.more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.action_buy)) }, onClick = {
                expanded = false
                onBuyClick()
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.action_sell)) }, onClick = {
                expanded = false
                onSellClick()
            })
        }
    }
}

private const val COLUMN_COUNT = 2

/**
 * Accessibility description of the "owned" marker (also used by the UI tests).
 */
const val OWNED_DESCRIPTION = "Owned"
