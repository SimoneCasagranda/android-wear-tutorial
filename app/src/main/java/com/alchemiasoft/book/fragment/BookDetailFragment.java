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

package com.alchemiasoft.book.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alchemiasoft.book.R;
import com.alchemiasoft.book.content.BookDB;
import com.alchemiasoft.book.model.Book;
import com.alchemiasoft.book.service.BookActionService;

/**
 * Fragment that shows a book in full details.
 * <p/>
 * Created by Simone Casagranda on 27/12/14.
 */
public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Argument used to set/get the book id.
     */
    private static final String ARG_BOOK_ID = "com.alchemiasoft.book.fragment.BOOK_ID";

    /**
     * Loader id for book.
     */
    private static final int ID_LOADER_BOOK = 23;

    /**
     * Creates a new BookDetailFragment for the given book id.
     *
     * @param bookId of the book that has to be shown.
     * @return the BookDetailFragment ready to be attached.
     */
    public static BookDetailFragment create(long bookId) {
        final BookDetailFragment fragment = new BookDetailFragment();
        final Bundle args = new Bundle();
        args.putLong(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView mTitleTextView, mAuthorTextView, mPagesTextView;
    private TextView mSourceTextView, mDescriptionTextView, mNotesTextView;
    private Button mActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_book, container, false);
        mTitleTextView = (TextView) view.findViewById(R.id.title);
        mAuthorTextView = (TextView) view.findViewById(R.id.author);
        mSourceTextView = (TextView) view.findViewById(R.id.source);
        mDescriptionTextView = (TextView) view.findViewById(R.id.description);
        mPagesTextView = (TextView) view.findViewById(R.id.pages);
        mNotesTextView = (TextView) view.findViewById(R.id.notes);
        mActionButton = (Button) view.findViewById(R.id.action);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(ID_LOADER_BOOK, getArguments(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getLoaderManager().destroyLoader(ID_LOADER_BOOK);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_BOOK:
                return new CursorLoader(getActivity(), BookDB.Book.create(args.getLong(ARG_BOOK_ID)), null, null, null, null);
            default:
                throw new IllegalArgumentException("Id=" + id + " is not supported.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            return;
        }
        final Book book = Book.oneFrom(data);
        mTitleTextView.setText(book.getTitle());
        mAuthorTextView.setText(book.getAuthor());
        mSourceTextView.setText(getString(R.string.source, book.getSource()));
        mDescriptionTextView.setText(book.getDescrition());
        mNotesTextView.setText(book.getNotes());
        mPagesTextView.setText(getString(R.string.pages, (book.getPages() >= 0 ? book.getPages() : getString(R.string.unknown))));
        mActionButton.setText(book.isOwned() ? R.string.action_sell : R.string.action_buy);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionButton.setEnabled(false);
                final Activity activity = getActivity();
                final BookActionService.IntentBuilder builder = book.isOwned() ? BookActionService.IntentBuilder.sell(activity, book) : BookActionService.IntentBuilder.buy(activity, book);
                activity.startService(builder.build());
            }
        });
        mActionButton.setEnabled(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
