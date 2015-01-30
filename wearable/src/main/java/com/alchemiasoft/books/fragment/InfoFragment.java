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

package com.alchemiasoft.books.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alchemiasoft.books.R;

/**
 * Fragment that displays a simple info place holder.
 * <p/>
 * Created by Simone Casagranda on 27/01/15.
 */
public class InfoFragment extends Fragment {

    /**
     * Params.
     */
    private static final String ARG_TEXT_RES_ID = "com.alchemiasoft.books.fragment.info.TEXT_RES_ID";

    /**
     * Builder for the InfoFragment.
     */
    public static final class Builder {

        private final Bundle mArgs = new Bundle();

        private Builder() {
        }

        public static Builder create(int textId) {
            final Builder builder = new Builder();
            builder.mArgs.putInt(ARG_TEXT_RES_ID, textId);
            return builder;
        }

        public InfoFragment build() {
            final InfoFragment fragment = new InfoFragment();
            fragment.setArguments(mArgs);
            return fragment;
        }
    }

    private TextView mTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_info, container, false);
        mTextView = (TextView) view.findViewById(R.id.text);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle args = getArguments();
        mTextView.setText(args.getInt(ARG_TEXT_RES_ID, R.string.unknown));
    }
}
