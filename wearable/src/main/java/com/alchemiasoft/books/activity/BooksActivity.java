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
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.CrossfadeDrawable;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.text.TextUtils;

import com.alchemiasoft.books.R;
import com.alchemiasoft.books.fragment.AddNoteFragment;
import com.alchemiasoft.books.fragment.BuyBookFragment;
import com.alchemiasoft.books.fragment.InfoFragment;
import com.alchemiasoft.books.fragment.SettingsFragment;
import com.alchemiasoft.common.sync.Event;
import com.alchemiasoft.common.util.UriUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import static com.alchemiasoft.common.content.BookDB.Book;

/**
 * Activity that displays some of the info about all the available books.
 */
public class BooksActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, GridViewPager.OnPageChangeListener, GoogleApiClient.ConnectionCallbacks {

    /**
     * Loader id.
     */
    private static final int LOADER_ID_SUGGESTIONS = 23;

    /**
     * Usually we don't want to display more than 5 rows in the 2D Picker.
     * This is done because we don't want to display tons of content to the user.
     */
    private static final int MAX_ROWS = 5;

    private static final int NOT_VALID = -1;

    /**
     * Fading values.
     */
    private static final float NO_FADE = 0f, PARTIAL_FADE = 0.8f;

    /**
     * Query params.
     */
    private static final String[] PROJECTION = {Book._ID, Book.TITLE, Book.AUTHOR, Book.DESCRIPTION, Book.NOTES, Book.TAG};
    private static final String SELECTION = Book.OWNED + " = ?";
    private static final String[] SELECTION_ARGS = {String.valueOf(0)};
    private static final String ORDER_BY = null;

    /**
     * UI references.
     */
    private DotsPageIndicator mPageIndicator;

    private GridViewPager mViewPager;
    private BooksGridPagerAdapter mAdapter;

    private CrossfadeDrawable mCrossfadeDrawable;

    /**
     * Client used by the wearable api to notify about the current book.
     */
    private GoogleApiClient mGoogleApiClient;

    private int mOldRow = NOT_VALID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        // Getting UI references
        mPageIndicator = (DotsPageIndicator) findViewById(R.id.pager_indicator);
        mViewPager = (GridViewPager) findViewById(R.id.pager);
        // Drawable used to make the UI more captivating
        mCrossfadeDrawable = new CrossfadeDrawable();
        mCrossfadeDrawable.setFading(getResources().getDrawable(R.drawable.fading_background));
        findViewById(android.R.id.content).setBackground(mCrossfadeDrawable);
        // Creating and Setting the Pager adapter
        mAdapter = new BooksGridPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        // Connecting the GridViewPager to the indicator
        mPageIndicator.setPager(mViewPager);
        // Adding the page change listener
        mViewPager.setOnPageChangeListener(this);
        // Creating the GoogleApiClient for the Wearable api (if available)
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build();
        }
        // Initializing the loader
        getLoaderManager().initLoader(LOADER_ID_SUGGESTIONS, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Trying to bind to the GoogleApiClient
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Releasing the client when not needed
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(LOADER_ID_SUGGESTIONS);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mOldRow != NOT_VALID) {
            long bookId = mAdapter.getBookId(mOldRow);
            if (bookId >= 0) {
                Event.MessageApi.Sender.create(mGoogleApiClient, bookId).action(Event.MessageApi.OPEN).asyncSend();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int status) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID_SUGGESTIONS:
                return new CursorLoader(this, UriUtil.withLimit(Book.CONTENT_URI, MAX_ROWS), PROJECTION, SELECTION, SELECTION_ARGS, ORDER_BY);
            default:
                throw new IllegalArgumentException("Loader id=" + id + " is not supported!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        mViewPager.setCurrentItem(0, 0);
        onPageSelected(0, 0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onPageScrolled(int row, int column, float rowOffset, float columnOffset, int rowOffsetPixels, int columnOffsetPixels) {
        mPageIndicator.onPageScrolled(row, column, rowOffset, columnOffset, rowOffsetPixels, columnOffsetPixels);
    }

    @Override
    public void onPageSelected(int row, int column) {
        if (mOldRow != row) {
            mOldRow = row;
            mCrossfadeDrawable.setBase(mAdapter.getBaseDrawable(row));
            // Notifying that the user is looking at the X row.
            final long bookId = mAdapter.getBookId(row);
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && bookId >= 0L) {
                Event.MessageApi.Sender.create(mGoogleApiClient, bookId).action(Event.MessageApi.OPEN).asyncSend();
            }
        }
        mCrossfadeDrawable.setProgress(mAdapter.isCard(row, column) ? NO_FADE : PARTIAL_FADE);
        mPageIndicator.onPageSelected(row, column);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mPageIndicator.onPageScrollStateChanged(state);
    }

    /**
     * Adapter used to display books in pages. Following the Android Wear Design Guidelines,
     * all the elements will be displayed per row and in the columns the user will be able to find
     * detailed info and button actions.
     * <p/>
     * e.g.:
     * column 1    column 2    column3     column N
     * _____________________________________________
     * row 1  | title 1     info 1      action 3    action N
     * row 2  | title 2     ...
     * row N  |  ...
     */
    private static final class BooksGridPagerAdapter extends FragmentGridPagerAdapter {

        /**
         * Columns for valid cursor.
         */
        private static final int TITLE = 0;
        private static final int INFO = 1;
        private static final int NOTES = 2;
        private static final int BUY = 3;
        private static final int SETTINGS = 4;

        private static final int COLUMNS = 5;

        /**
         * Columns for empty.
         */
        private static final int EMPTY_INFO = 0;
        private static final int EMPTY_SETTINGS = 1;

        private static final int EMPTY_ROW_COUNT = 1;
        private static final int EMPTY_COLUMN_COUNT = 2;

        /**
         * Cursor used as books source.
         */
        private Cursor mCursor;

        /**
         * Activity reference.
         */
        private final Activity mActivity;

        private BooksGridPagerAdapter(FragmentActivity activity) {
            super(activity.getFragmentManager());
            mActivity = activity;
        }

        @Override
        public Fragment getFragment(int row, int column) {
            // Checking if it's empty
            if (isEmpty()) {
                switch (column) {
                    case EMPTY_INFO:
                        return InfoFragment.Builder.create(R.string.no_suggestions).build();
                    case EMPTY_SETTINGS:
                        return SettingsFragment.Builder.create().build();
                    default:
                        throw new IllegalArgumentException("getFragment(row=" + row + ", column=" + column + ")");
                }
            } else {
                // Positioning the cursor at the right row
                mCursor.moveToPosition(row);
                // Matching the fragment by column
                switch (column) {
                    case TITLE:
                        final String title = mCursor.getString(mCursor.getColumnIndex(Book.TITLE));
                        final String author = mCursor.getString(mCursor.getColumnIndex(Book.AUTHOR));
                        return CardFragment.create(title, author);
                    case INFO:
                        final String description = mCursor.getString(mCursor.getColumnIndex(Book.DESCRIPTION));
                        return CardFragment.create(mActivity.getString(R.string.description), description);
                    case NOTES:
                        final String notes = mCursor.getString(mCursor.getColumnIndex(Book.NOTES));
                        if (TextUtils.isEmpty(notes)) {
                            // Button to catch a note
                            return AddNoteFragment.Builder.create(mCursor.getLong(mCursor.getColumnIndex(Book._ID))).build();
                        } else {
                            return CardFragment.create(mActivity.getString(R.string.notes), notes);
                        }
                    case BUY:
                        // Button to buy
                        return BuyBookFragment.Builder.create(mCursor.getLong(mCursor.getColumnIndex(Book._ID))).build();
                    case SETTINGS:
                        return SettingsFragment.Builder.create().build();
                    default:
                        throw new IllegalArgumentException("getFragment(row=" + row + ", column=" + column + ")");
                }
            }
        }

        public boolean isCard(int row, int column) {
            if (isEmpty()) {
                return false;
            }
            if (column < 2) {
                return true;
            } else if (column > 2) {
                return false;
            } else {
                mCursor.moveToPosition(row);
                final String notes = mCursor.getString(mCursor.getColumnIndex(Book.NOTES));
                return !(TextUtils.isEmpty(notes));
            }
        }

        @Override
        public int getRowCount() {
            return isEmpty() ? EMPTY_ROW_COUNT : mCursor.getCount();
        }

        @Override
        public int getColumnCount(int row) {
            return isEmpty() ? EMPTY_COLUMN_COUNT : COLUMNS;
        }

        public Cursor swapCursor(Cursor cursor) {
            final Cursor oldCursor = mCursor;
            mCursor = cursor;
            notifyDataSetChanged();
            return oldCursor;
        }

        public boolean isEmpty() {
            return mCursor == null || mCursor.getCount() == 0;
        }

        public long getBookId(int row) {
            if (isEmpty()) {
                return NOT_VALID;
            }
            // Positioning the cursor at the right row
            mCursor.moveToPosition(row);
            return mCursor.getLong(mCursor.getColumnIndex(Book._ID));
        }

        public Drawable getBaseDrawable(int row) {
            if (isEmpty()) {
                return mActivity.getResources().getDrawable(R.drawable.tile_sad);
            }
            // Positioning the cursor at the right row
            mCursor.moveToPosition(row);
            final String tag = mCursor.getString(mCursor.getColumnIndex(Book.TAG));
            Drawable drawable = null;
            if (!TextUtils.isEmpty(tag)) {
                if (tag.equals("android")) {
                    drawable = mActivity.getResources().getDrawable(R.drawable.tile_android);
                } else if (tag.equals("javascript")) {
                    drawable = mActivity.getResources().getDrawable(R.drawable.tile_javascript);
                } else if (tag.equals("java")) {
                    drawable = mActivity.getResources().getDrawable(R.drawable.tile_java);
                } else if (tag.equals("git")) {
                    drawable = mActivity.getResources().getDrawable(R.drawable.tile_git);
                }
            }
            if (drawable == null) {
                drawable = mActivity.getResources().getDrawable(R.drawable.tile_android);
            }
            return drawable;
        }
    }
}
