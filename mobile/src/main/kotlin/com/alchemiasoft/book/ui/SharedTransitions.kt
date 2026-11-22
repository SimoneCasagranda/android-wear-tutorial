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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Scopes of the list <-> detail transition (provided by [HomeScreen]). Null outside of it, e.g. in previews.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Marks the cover of the given book as the element shared between the list card and the detail
 * header: when a book is opened its cover grows from the card into the header (and back).
 */
@Composable
fun Modifier.sharedCover(bookId: Long): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@sharedCover.sharedElement(rememberSharedContentState(key = "cover-$bookId"), visibilityScope)
    }
}

/**
 * Shared elements are drawn in an overlay above the screens while the transition runs. This keeps
 * the decorated content (e.g. the header's scrim and title) above the moving cover, fading with the screen.
 */
@Composable
fun Modifier.aboveSharedElements(): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        with(visibilityScope) {
            this@aboveSharedElements
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                .animateEnterExit(enter = fadeIn(), exit = fadeOut())
        }
    }
}
