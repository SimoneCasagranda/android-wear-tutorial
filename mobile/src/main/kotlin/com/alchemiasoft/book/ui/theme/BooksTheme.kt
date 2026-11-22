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


package com.alchemiasoft.book.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The palette of the original app (Material "cyan" with a "teal" accent).
 */
object BooksColors {
    val Primary = Color(0xFF00BCD4)
    val PrimaryDark = Color(0xFF0097A7)
    val PrimaryLight = Color(0xFFB2EBF2)
    val Accent = Color(0xFF009688)
    val PrimaryText = Color(0xFF212121)
    val SecondaryText = Color(0xFF727272)
    val Divider = Color(0xFFB6B6B6)
}

private val LightColors = lightColorScheme(
    primary = BooksColors.Primary,
    onPrimary = Color.White,
    primaryContainer = BooksColors.PrimaryLight,
    onPrimaryContainer = BooksColors.PrimaryText,
    secondary = BooksColors.Accent,
    onSecondary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = BooksColors.PrimaryText,
    surface = Color.White,
    onSurface = BooksColors.PrimaryText,
    onSurfaceVariant = BooksColors.SecondaryText,
    outline = BooksColors.Divider,
)

@Composable
fun BooksTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
