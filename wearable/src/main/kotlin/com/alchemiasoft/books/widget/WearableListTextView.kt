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


// WearableListView comes from the legacy Wear support library, kept on purpose for the tutorial UI.
@file:Suppress("DEPRECATION")

package com.alchemiasoft.books.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.wearable.view.WearableListView
import android.util.AttributeSet
import android.widget.TextView

/**
 * TextView implementation that can react when leaving or reaching the center of a WearableListView.
 *
 * Created by Simone Casagranda on 27/01/15.
 */
@SuppressLint("AppCompatCustomView") // The watch app doesn't use AppCompat.
class WearableListTextView : TextView, WearableListView.OnCenterProximityListener {

    // Explicit constructors (not @JvmOverloads) so each one keeps TextView's own default style handling.
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onCenterPosition(animate: Boolean) {
        if (animate) {
            animate().alpha(NO_ALPHA).translationX(X_TRANSLATION).start()
        } else {
            alpha = NO_ALPHA
            translationX = X_TRANSLATION
        }
    }

    override fun onNonCenterPosition(animate: Boolean) {
        if (animate) {
            animate().alpha(PARTIAL_ALPHA).translationX(NO_X_TRANSLATION).start()
        } else {
            alpha = PARTIAL_ALPHA
            translationX = NO_X_TRANSLATION
        }
    }

    private companion object {
        const val NO_ALPHA = 1f
        const val PARTIAL_ALPHA = 0.35f
        const val NO_X_TRANSLATION = 0f
        const val X_TRANSLATION = 15f
    }
}
