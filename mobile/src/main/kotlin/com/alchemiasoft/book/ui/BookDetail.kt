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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemiasoft.book.R
import com.alchemiasoft.book.ui.theme.BooksColors
import com.alchemiasoft.common.model.Book
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

/**
 * Full details of a book with the buy/sell action.
 */
@Composable
fun BookDetail(book: Book, onBuyClick: (Book) -> Unit, onSellClick: (Book) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        DetailHeader(book)
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.pages, if (book.pages >= 0) book.pages.toString() else stringResource(R.string.unknown)),
                modifier = Modifier.weight(1f).padding(8.dp),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Disabled after a tap until the updated book comes back from the provider (owned flips).
            var pending by remember(book.id, book.isOwned) { mutableStateOf(false) }
            Button(
                onClick = {
                    pending = true
                    if (book.isOwned) onSellClick(book) else onBuyClick(book)
                },
                enabled = !pending,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(stringResource(if (book.isOwned) R.string.action_sell else R.string.action_buy).uppercase())
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(
                book.description.orEmpty(),
                modifier = Modifier.padding(8.dp),
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val notes = book.notes
            if (!notes.isNullOrEmpty()) {
                Text(notes, modifier = Modifier.padding(8.dp), fontSize = 15.sp, color = BooksColors.Accent)
            }
        }
        Text(
            stringResource(R.string.source, book.source.orEmpty()),
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(8.dp),
            fontSize = 15.sp,
            color = Color.White,
        )
    }
}

/**
 * Title and author. When the book has a cover it fills the header as background (with a scrim to
 * keep the text readable); otherwise the header is the plain primary band of the original design.
 */
@Composable
private fun DetailHeader(book: Book) {
    val cover = book.cover
    if (cover == null) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(vertical = 8.dp)) {
            HeaderTexts(book)
        }
        return
    }
    Box(Modifier.fillMaxWidth().height(HEADER_HEIGHT).background(MaterialTheme.colorScheme.primary)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(cover)
                // While the bigger image loads, show the one the list already has in memory.
                .placeholderMemoryCacheKey(cover)
                .build(),
            contentDescription = stringResource(R.string.cover_description, book.title.orEmpty()),
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize().sharedCover(book.id).testTag("detail_cover_${book.id}"),
        )
        Box(
            Modifier
                .matchParentSize()
                .aboveSharedElements()
                .background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.8f))),
        )
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().aboveSharedElements().padding(bottom = 12.dp)) {
            HeaderTexts(book)
        }
    }
}

@Composable
private fun HeaderTexts(book: Book) {
    Text(
        book.title.orEmpty(),
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
    )
    Text(
        book.author.orEmpty(),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        fontSize = 16.sp,
        color = Color.White,
        textAlign = TextAlign.Center,
    )
}

private val HEADER_HEIGHT = 280.dp
