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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alchemiasoft.books.R;
import com.alchemiasoft.common.content.BookDB;

/**
 * Fragment that allows to buy a book.
 * <p/>
 * Created by Simone Casagranda on 24/01/15.
 */
public class BuyBookFragment extends Fragment implements DelayedConfirmationView.DelayedConfirmationListener {

    /**
     * Tag used for logging.
     */
    private static final String TAG_LOG = BuyBookFragment.class.getSimpleName();

    /**
     * Timeout delay for confirmation.
     */
    private static final long DELAY_TIMEOUT = 2000L;

    /**
     * Arguments params.
     */
    private static final String ARG_ID = "com.alchemiasoft.books.fragment.book.ID";

    /**
     * Allows to build BookInfoCardFragment in an extensible way.
     */
    public static final class Builder {

        private final Bundle mArgs = new Bundle();

        private Builder() {
        }

        public static Builder create(long id) {
            final Builder builder = new Builder();
            builder.mArgs.putLong(ARG_ID, id);
            return builder;
        }

        public BuyBookFragment build() {
            final BuyBookFragment fragment = new BuyBookFragment();
            fragment.setArguments(mArgs);
            return fragment;
        }
    }

    private DelayedConfirmationView mConfirmationView;

    private boolean mIsAnimating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_buy_book, container, false);
        mConfirmationView = (DelayedConfirmationView) view.findViewById(R.id.confirm_buy);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // The entire "screen" is listening for the click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAnimating) {
                    mConfirmationView.setImageResource(R.drawable.ic_action_buy);
                    mIsAnimating = false;
                    return;
                }
                mIsAnimating = true;
                mConfirmationView.setImageResource(R.drawable.ic_full_cancel);
                mConfirmationView.setTotalTimeMs(DELAY_TIMEOUT);
                mConfirmationView.start();
            }
        });
        // Registering the listener triggered by the DelayedConfirmationView
        mConfirmationView.setListener(this);
    }

    @Override
    public void onTimerFinished(View view) {
        mIsAnimating = false;
        final Activity activity = getActivity();
        if (activity == null) {
            Log.e(TAG_LOG, "Fragment not attached anymore to the activity (Skipping action).");
            return;
        }
        // Updating the book state
        final long bookId = getArguments().getLong(ARG_ID);
        final PurchaseBookTask task = new PurchaseBookTask(activity, bookId);
        task.execute();
        // Starting the confirmation screen
        Intent intent = new Intent(activity, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.item_purchased));
        startActivity(intent);
        activity.finish();
    }

    @Override
    public void onTimerSelected(View view) {
        mConfirmationView.reset();
    }

    /**
     * AsyncTask that perform the purchase action on the book.
     */
    static final class PurchaseBookTask extends AsyncTask<Void, Void, Void> {

        private final ContentResolver mResolver;

        private final long mBookId;

        PurchaseBookTask(Context context, long mBookId) {
            this.mResolver = context.getContentResolver();
            this.mBookId = mBookId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final ContentValues values = new ContentValues();
            values.put(BookDB.Book.OWNED, 1);
            final int count = mResolver.update(BookDB.Book.create(mBookId), values, null, null);
            Log.d(TAG_LOG, "Book purchase with id=" + mBookId + (count == 1 ? "[success]" : "[fail]"));
            return null;
        }
    }
}
