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

package com.alchemiasoft.common.content;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;

/**
 * ContentProvider for the Book application.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class BookContentProvider extends ContentProvider {

    protected final static UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private SQLiteOpenHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDBOpenHelper(getContext());
        mUriMatcher.addURI(BookDB.AUTHORITY, BookDB.Book.ITEM_PATH, 0);
        mUriMatcher.addURI(BookDB.AUTHORITY, BookDB.Book.DIR_PATH, 1);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int code = mUriMatcher.match(uri);
        switch (code) {
            case 0:
                return BookDB.Book.CURSOR_ITEM_MIME_TYPE;
            case 1:
                return BookDB.Book.CURSOR_DIR_MIME_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int code = mUriMatcher.match(uri);
        Cursor cursor;
        switch (code) {
            case 0:
                selection = BookDB.Book._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = mDbHelper.getWritableDatabase().query(BookDB.Book.TABLE, projection, selection, selectionArgs, null, null, null);
                break;
            case 1:
                cursor = mDbHelper.getWritableDatabase().query(BookDB.Book.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Uri not valid for ContentProvider " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int code = mUriMatcher.match(uri);
        switch (code) {
            case 1:
                long id = mDbHelper.getWritableDatabase().insert(BookDB.Book.TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return BookDB.Book.create(id);
            default:
                throw new IllegalArgumentException("Uri not valid for ContentProvider " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int code = mUriMatcher.match(uri);
        int result;
        switch (code) {
            case 0:
                selection = BookDB.Book._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                result = mDbHelper.getWritableDatabase().delete(BookDB.Book.TABLE, selection, selectionArgs);
                break;
            case 1:
                result = mDbHelper.getWritableDatabase().delete(BookDB.Book.TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Uri not valid for ContentProvider " + uri);
        }
        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int code = mUriMatcher.match(uri);
        int result;
        switch (code) {
            case 0:
                selection = BookDB.Book._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                result = mDbHelper.getWritableDatabase().update(BookDB.Book.TABLE, values, selection, selectionArgs);
                break;
            case 1:
                result = mDbHelper.getWritableDatabase().update(BookDB.Book.TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Uri not valid for ContentProvider " + uri);
        }
        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public ContentProviderResult[] applyBatch(final ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
}
