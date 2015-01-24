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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import com.alchemiasoft.books.R;
import com.alchemiasoft.common.util.UriUtil;

import static com.alchemiasoft.common.content.BookDB.Book;

/**
 * Activity that displays some of the info about all the available books.
 */
public class BooksActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Loader id.
     */
    private static final int LOADER_ID_SUGGESTIONS = 23;

    /**
     * Usually we don't want to display more than 5 rows in the 2D Picker.
     * This is done because we don't want to display tons of content to the user.
     */
    private static final int MAX_ROWS = 5;

    /**
     * Query params.
     */
    private static final String[] PROJECTION = {Book._ID, Book.TITLE, Book.AUTHOR, Book.DESCRIPTION, Book.NOTES};
    private static final String SELECTION = Book.OWNED + " = ?";
    private static final String[] SELECTION_ARGS = {String.valueOf(0)};
    private static final String ORDER_BY = null;

    private GridViewPager mViewPager;
    private BooksGridPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        // Getting UI references
        mViewPager = (GridViewPager) findViewById(R.id.pager);
        // Creating and Setting the Pager adapter
        mAdapter = new BooksGridPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLoaderManager().initLoader(LOADER_ID_SUGGESTIONS, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(LOADER_ID_SUGGESTIONS);
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
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
         * Columns
         */
        private static final int TITLE = 0;
        private static final int DESCRIPTION = 1;
        private static final int NOTES = 2;

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
            // Positioning the cursor at the right row
            mCursor.moveToPosition(row);
            // Matching the fragment by column
            switch (column) {
                case TITLE:
                    final String title = mCursor.getString(mCursor.getColumnIndex(Book.TITLE));
                    final String author = mCursor.getString(mCursor.getColumnIndex(Book.AUTHOR));
                    return CardFragment.create(title, author);
                case DESCRIPTION:
                    final String description = mCursor.getString(mCursor.getColumnIndex(Book.DESCRIPTION));
                    return CardFragment.create(mActivity.getString(R.string.description), description);
                case NOTES:
                    return CardFragment.create("Row:" + row, "Column: " + column);
                default:
                    throw new IllegalArgumentException("getFragment(row=" + row + ", column=" + column + ")");
            }
        }

        @Override
        public int getRowCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public int getColumnCount(int row) {
            return 3;
        }

        public Cursor swapCursor(Cursor cursor) {
            final Cursor oldCursor = mCursor;
            mCursor = cursor;
            notifyDataSetChanged();
            return oldCursor;
        }
    }
}
