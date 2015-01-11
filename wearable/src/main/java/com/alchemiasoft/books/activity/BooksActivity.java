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

package com.alchemiasoft.books.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Pair;
import android.widget.TextView;

import com.alchemiasoft.books.R;
import com.alchemiasoft.common.content.BookDB;

import java.lang.ref.WeakReference;

/**
 * Activity that displays some of the info about all the available books.
 */
public class BooksActivity extends Activity {

    /**
     * UI references
     */
    private TextView mInfoTextView;

    /**
     * AsyncTask used to load the info.
     */
    private BooksInfoTask mInfoTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mInfoTextView = (TextView) stub.findViewById(R.id.books_info);
                // Loading the books info through an AsyncTask just to show
                // something on the wearable device
                // N.B.: (this will change in part 4).
                mInfoTask = new BooksInfoTask(BooksActivity.this);
                mInfoTask.execute();
            }
        });
    }

    /**
     * AsyncTask that loads asynchronously the books info.
     */
    private static final class BooksInfoTask extends AsyncTask<Void, Void, Pair<Integer, Integer>> {

        private final String[] PROJECTION = {BookDB.Book.OWNED};

        private final WeakReference<BooksActivity> mActivityRef;

        private final Context mAppContext;

        private BooksInfoTask(BooksActivity activity) {
            mActivityRef = new WeakReference<>(activity);
            mAppContext = activity.getApplicationContext();
        }

        @Override
        protected Pair<Integer, Integer> doInBackground(Void... params) {
            final ContentResolver resolver = mAppContext.getContentResolver();
            int totalCount = 0, ownedCount = 0;
            final Cursor c = resolver.query(BookDB.Book.CONTENT_URI, PROJECTION, null, null, null);
            try {
                totalCount = c.getCount();
                final int ownedIndex = c.getColumnIndex(BookDB.Book.OWNED);
                while (c.moveToNext()) {
                    if (c.getInt(ownedIndex) > 0) {
                        ownedCount++;
                    }
                }
            } finally {
                c.close();
            }
            return new Pair<>(totalCount, ownedCount);
        }

        @Override
        protected void onPostExecute(Pair<Integer, Integer> pair) {
            super.onPostExecute(pair);
            final BooksActivity activity = getActivity();
            if (activity != null) {
                String info = activity.getString(R.string.format_available_books, pair.first, pair.second);
                activity.mInfoTextView.setText(info);
            }
        }

        private BooksActivity getActivity() {
            return mActivityRef == null ? null : mActivityRef.get();
        }
    }
}
