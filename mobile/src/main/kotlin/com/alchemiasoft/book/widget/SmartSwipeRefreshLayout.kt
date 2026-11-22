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


package com.alchemiasoft.book.widget

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * SwipeRefreshLayout implementation that allows to check if the content in a RecyclerView
 * can scroll up or not.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class SmartSwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SwipeRefreshLayout(context, attrs) {

    fun interface ScrollInterceptor {
        fun canChildScrollUp(): Boolean
    }

    var scrollInterceptor: ScrollInterceptor? = null

    override fun canChildScrollUp(): Boolean = scrollInterceptor?.canChildScrollUp() ?: super.canChildScrollUp()
}
