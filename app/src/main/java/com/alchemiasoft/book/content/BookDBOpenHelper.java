/*
 * Copyright 2014 Simone Casagranda.
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

package com.alchemiasoft.book.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLiteOpenHelper that creates the SQLite database for the Book application.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class BookDBOpenHelper extends SQLiteOpenHelper {

    /**
     * Tag used for logging.
     */
    private static final String TAG_LOG = BookDBOpenHelper.class.getSimpleName();

    public BookDBOpenHelper(Context context) {
        super(context, BookDB.NAME, null, BookDB.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(BookDB.Book.CREATE_TABLE);

            db.setTransactionSuccessful();
            Log.i(TAG_LOG, "Successfully created " + BookDB.NAME);
        } catch (Exception e) {
            Log.e(TAG_LOG, "Error creating " + BookDB.NAME + ": ", e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();

            db.execSQL(BookDB.Book.DELETE_TABLE);

            onCreate(db);

            db.setTransactionSuccessful();
            Log.i(TAG_LOG, "Successfully upgraded " + BookDB.NAME);
        } catch (Exception e) {
            Log.e(TAG_LOG, "Error creating " + BookDB.NAME + ": ", e);
        } finally {
            db.endTransaction();
        }
    }
}
