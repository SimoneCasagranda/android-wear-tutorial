/*
 * Copyright 2014 Simone Casagranda.
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

package com.alchemiasoft.book.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * SwipeRefreshLayout implementation that allows to check if the content in a RecyclerView
 * can scroll up or not.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class SmartSwipeRefreshLayout extends SwipeRefreshLayout {

    public static interface ScrollInterceptor {
        boolean canChildScrollUp();
    }

    private ScrollInterceptor mScrollInterceptor;

    public SmartSwipeRefreshLayout(Context context) {
        super(context);
    }

    public SmartSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        return mScrollInterceptor != null ? mScrollInterceptor.canChildScrollUp() : super.canChildScrollUp();
    }

    public void setScrollInterceptor(ScrollInterceptor interceptor) {
        this.mScrollInterceptor = interceptor;
    }
}
