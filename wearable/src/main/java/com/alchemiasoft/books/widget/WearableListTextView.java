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

package com.alchemiasoft.books.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView implementation that can react when leaving or reaching the center of a WearableListView.
 * <p/>
 * Created by Simone Casagranda on 27/01/15.
 */
public class WearableListTextView extends TextView implements WearableListView.OnCenterProximityListener {

    private static final float NO_ALPHA = 1f, PARTIAL_ALPHA = 0.35f;
    private static final float NO_X_TRANSLATION = 0f, X_TRANSLATION = 15f;

    public WearableListTextView(Context context) {
        super(context);
    }

    public WearableListTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WearableListTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WearableListTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate) {
            animate().alpha(NO_ALPHA).translationX(X_TRANSLATION).start();
        } else {
            setAlpha(NO_ALPHA);
            setTranslationX(X_TRANSLATION);
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate) {
            animate().alpha(PARTIAL_ALPHA).translationX(NO_X_TRANSLATION).start();
        } else {
            setAlpha(PARTIAL_ALPHA);
            setTranslationX(NO_X_TRANSLATION);
        }
    }
}
