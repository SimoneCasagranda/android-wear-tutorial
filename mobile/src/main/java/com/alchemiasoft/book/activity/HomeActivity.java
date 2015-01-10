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

package com.alchemiasoft.book.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.alchemiasoft.book.R;
import com.alchemiasoft.book.fragment.BookDetailFragment;
import com.alchemiasoft.book.fragment.BooksFragment;
import com.alchemiasoft.common.model.Book;
import com.alchemiasoft.book.receiver.SuggestionReceiver;

/**
 * Activity that decides weather to display available books or owned books.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class HomeActivity extends ActionBarActivity {

    /**
     * Tag used for logging purposes.
     */
    private static final String TAG_LOG = HomeActivity.class.getSimpleName();

    /**
     * Tag(s) used for fragment transactions.
     */
    private static final String TAG_ALL_BOOKS = "all_books";
    private static final String TAG_OWNED_BOOKS = "owned_books";

    /**
     * Key for the book Id.
     */
    private static final String KEY_BOOK_ID = "com.alchemiasoft.book.activity.BOOK_ID";
    private static final long NOT_VALID = -1L;

    /**
     * Intent that should be used to launch HomeActivity for a given book.
     *
     * @param context reference.
     * @param book    that has to be passed.
     * @return an intent ready to be used to pass info to the HomeActivity.
     */
    public static Intent createFor(@NonNull Context context, @NonNull Book book) {
        final Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(KEY_BOOK_ID, book.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final FragmentManager fm = getSupportFragmentManager();
                switch (menuItem.getItemId()) {
                    case R.id.action_all_books:
                        if (fm.findFragmentByTag(TAG_ALL_BOOKS) == null) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.content, BooksFragment.create(HomeActivity.this, false), TAG_ALL_BOOKS).commit();
                        } else {
                            Log.d(TAG_LOG, "All books fragment is already attached.");
                        }
                        return true;
                    case R.id.action_my_books:
                        if (fm.findFragmentByTag(TAG_OWNED_BOOKS) == null) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.content, BooksFragment.create(HomeActivity.this, true), TAG_OWNED_BOOKS).commit();
                        } else {
                            Log.d(TAG_LOG, "Owned books fragment is already attached.");
                        }
                        return true;
                    case R.id.action_settings:
                        Toast.makeText(HomeActivity.this, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_suggestion:
                        // Scheduling a suggestion
                        SuggestionReceiver.scheduleSuggestion(HomeActivity.this);
                        return true;
                    default:
                        Log.w(TAG_LOG, "MenuItem: title=" + menuItem.getTitle() + " & itemId=" + menuItem.getItemId() + " is not handled.");
                        return false;
                }
            }
        });
        toolbar.inflateMenu(R.menu.menu_home);
        // Checking if first instance and then attach the first fragment
        if (savedInstanceState == null) {
            attachBookFragment(getIntent().getLongExtra(KEY_BOOK_ID, NOT_VALID));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        attachBookFragment(intent.getLongExtra(KEY_BOOK_ID, NOT_VALID));
    }

    private void attachBookFragment(final long bookId) {
        if (bookId == NOT_VALID) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, BooksFragment.create(this, false), TAG_ALL_BOOKS).commit();
            // Scheduling a suggestion
            SuggestionReceiver.scheduleSuggestion(this);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, BookDetailFragment.create(bookId)).commit();
        }
    }
}
