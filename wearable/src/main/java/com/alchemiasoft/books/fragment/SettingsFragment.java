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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alchemiasoft.books.R;
import com.alchemiasoft.books.service.BookService;
import com.alchemiasoft.common.content.BookDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment that displays how a WearableListView works.
 * <p/>
 * Created by Simone Casagranda on 27/01/15.
 */
public class SettingsFragment extends Fragment implements WearableListView.ClickListener {

    /**
     * DB Query params.
     */
    private static final String WHERE_OWNED = BookDB.Book.OWNED + " = ?";
    private static final String OWNED = String.valueOf(1);
    private static final String NOT_OWNED = String.valueOf(0);

    /**
     * Builder for the SettingsFragment.
     */
    public static final class Builder {

        private final Bundle mArgs = new Bundle();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public SettingsFragment build() {
            final SettingsFragment fragment = new SettingsFragment();
            fragment.setArguments(mArgs);
            return fragment;
        }
    }

    private WearableListView mWearableListView;
    private Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mWearableListView = (WearableListView) view.findViewById(R.id.list);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Used to allow the WearableListView to intercept all the touches
        // preventing the parent to handle them
        mWearableListView.setGreedyTouchMode(true);
        // Creating and setting the adapter used to show the available settings
        final String[] settings = getResources().getStringArray(R.array.settings);
        mAdapter = new Adapter(settings);
        mWearableListView.setAdapter(mAdapter);
        mWearableListView.setClickListener(this);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final Activity activity = getActivity();
        final int position = viewHolder.getPosition();
        switch (position) {
            case 0:
                // Sell all the owned books
                BookService.Invoker.get(activity).where(WHERE_OWNED, OWNED).owned(false).invoke();
                activity.finish();
                break;
            case 1:
                // Buy all the available books
                BookService.Invoker.get(activity).where(WHERE_OWNED, NOT_OWNED).owned(true).invoke();
                activity.finish();
                break;
            case 2:
                // Resetting the DB
                BookService.Invoker.get(activity).notes(null).owned(false).invoke();
                activity.finish();
                break;
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    private static class Holder extends WearableListView.ViewHolder {

        private TextView mTextView;

        private Holder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text);
        }
    }

    private static class Adapter extends WearableListView.Adapter {

        private List<String> mData = new ArrayList<>();

        public Adapter(String[] data) {
            this.mData = Arrays.asList(data);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settings, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
            final Holder holder = (Holder) viewHolder;
            holder.mTextView.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}
