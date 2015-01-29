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

package com.alchemiasoft.books.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

import static com.alchemiasoft.common.content.BookDB.Book;

/**
 * IntentService that takes care of executing in the background any Book update.
 * <p/>
 * Created by Simone Casagranda on 28/01/15.
 */
public class BookService extends IntentService {

    /**
     * Tag used for logging.
     */
    private static final String TAG_LOG = BookService.class.getSimpleName();

    /**
     * Params.
     */
    private static final String KEY_PARAM_BOOK_ID = "com.alchemiasoft.books.service.book.PARAM_BOOK_ID";
    private static final String KEY_PARAM_DB_VALUES = "com.alchemiasoft.books.service.book.PARAM_DB_VALUES";
    private static final String KEY_PARAM_WHERE = "com.alchemiasoft.books.service.book.WHERE";
    private static final String KEY_PARAM_WHERE_ARGS = "com.alchemiasoft.books.service.book.WHERE_ARGS";

    /**
     * Not valid ID for a book entry.
     */
    private static final long NOT_VALID = -1;

    /**
     * Builder that allows to create in a simple manner the Intents with whom the
     * BookService should be invoked.
     */
    public static class Invoker {

        private final Context mContext;

        /**
         * Meta-data used to identify target and operation.
         */
        private final Bundle mMetaData = new Bundle();

        /**
         * Params that will be passed directly to the ContentProvider.
         */
        private final ContentValues mDBValues = new ContentValues();

        private Invoker(Context context) {
            this.mContext = context.getApplicationContext();
        }

        public static Invoker get(Context context) {
            return new Invoker(context);
        }

        public Invoker bookId(long id) {
            mMetaData.putLong(KEY_PARAM_BOOK_ID, id);
            return this;
        }

        public Invoker where(String where, String... whereArgs) {
            mMetaData.putString(KEY_PARAM_WHERE, where);
            mMetaData.putStringArray(KEY_PARAM_WHERE_ARGS, whereArgs);
            return this;
        }

        public Invoker notes(String notes) {
            mDBValues.put(Book.NOTES, notes);
            return this;
        }

        public Invoker owned(boolean owned) {
            mDBValues.put(Book.OWNED, owned ? 1 : 0);
            return this;
        }

        public void invoke() {
            final Intent intent = new Intent(mContext, BookService.class);
            intent.putExtras(mMetaData);
            intent.putExtra(KEY_PARAM_DB_VALUES, mDBValues);
            mContext.startService(intent);
        }
    }

    public BookService() {
        super(BookService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG_LOG, "New intent: " + intent);
        final ContentValues values = intent.getParcelableExtra(KEY_PARAM_DB_VALUES);
        if (values == null || values.size() == 0) {
            Log.e(TAG_LOG, "Trying to update the DB with values=" + values);
        }
        final ContentResolver resolver = getContentResolver();
        final long bookId = intent.getLongExtra(KEY_PARAM_BOOK_ID, NOT_VALID);
        if (bookId != NOT_VALID) {
            final Uri uri = Book.create(bookId);
            final int updated = resolver.update(uri, values, null, null);
            Log.d(TAG_LOG, "Updated " + updated + " with Uri=" + uri);
        } else {
            final Uri uri = Book.CONTENT_URI;
            final String where = intent.getStringExtra(KEY_PARAM_WHERE);
            final String[] whereArgs = intent.getStringArrayExtra(KEY_PARAM_WHERE_ARGS);
            final int updated = resolver.update(uri, values, null, null);
            Log.d(TAG_LOG, "Updated " + updated + " with Uri=" + uri + ", where=" + where + " <= " + Arrays.toString(whereArgs));
        }
    }
}
