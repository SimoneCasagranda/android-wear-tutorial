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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.alchemiasoft.book.R
import com.alchemiasoft.book.ui.theme.BooksColors
import com.alchemiasoft.common.model.Book

/**
 * The book's cover loaded with Coil (3:4). The initials placeholder underneath shows while the
 * image loads, if it fails, and for books without a cover. The caller decides the size.
 */
@Composable
internal fun BookCover(book: Book, modifier: Modifier = Modifier, initialsSize: TextUnit = 32.sp) {
    Box(
        modifier
            .aspectRatio(COVER_ASPECT_RATIO)
            .sharedCover(book.id)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .testTag("cover_${book.id}"),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials(book.title), fontSize = initialsSize, fontWeight = FontWeight.Bold, color = BooksColors.PrimaryDark)
        val cover = book.cover
        if (cover != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current).data(cover).memoryCacheKey(cover).build(),
                contentDescription = stringResource(R.string.cover_description, book.title.orEmpty()),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Up to two initials from the title's words, e.g. "Version Control with Git" -> "VC".
 */
internal fun initials(title: String?): String =
    title.orEmpty().split(' ', ':', '-').filter { it.firstOrNull()?.isLetterOrDigit() == true }.take(2)
        .joinToString("") { it.first().uppercase() }

/**
 * Width / height of the cover (book covers are roughly 3:4).
 */
private const val COVER_ASPECT_RATIO = 0.75f
