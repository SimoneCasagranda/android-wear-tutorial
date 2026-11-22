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


package com.alchemiasoft.common.content

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.alchemiasoft.common.lib.Constants
import com.alchemiasoft.common.model.Book
import com.alchemiasoft.common.util.ResUtil
import org.json.JSONArray

/**
 * SQLiteOpenHelper that creates the SQLite database for the Book application.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class BookDBOpenHelper(context: Context) : SQLiteOpenHelper(context, BookDB.NAME, null, BookDB.VERSION) {

    private val mContext: Context = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.beginTransaction()
            db.execSQL(BookDB.Book.CREATE_TABLE)
            val input = try {
                ResUtil.assetAsString(mContext, Constants.BOOKS_PATH)
            } catch (e: Exception) {
                Log.e(TAG_LOG, "Cannot read the input at assets/" + Constants.BOOKS_PATH)
                null
            }
            // Adding the default entries
            if (!input.isNullOrEmpty()) {
                Log.d(TAG_LOG, "Adding into the DB: $input")
                val books = Book.allFrom(JSONArray(input))
                // Adding the all the books as a batch
                for (book in books) {
                    db.insert(BookDB.Book.TABLE, null, book.toValues())
                }
            }
            db.setTransactionSuccessful()
            Log.i(TAG_LOG, "Successfully created " + BookDB.NAME)
        } catch (e: Exception) {
            Log.e(TAG_LOG, "Error creating " + BookDB.NAME + ": ", e)
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.beginTransaction()
            db.execSQL(BookDB.Book.DELETE_TABLE)
            onCreate(db)
            db.setTransactionSuccessful()
            Log.i(TAG_LOG, "Successfully upgraded " + BookDB.NAME)
        } catch (e: Exception) {
            Log.e(TAG_LOG, "Error creating " + BookDB.NAME + ": ", e)
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        /**
         * Tag used for logging.
         */
        private val TAG_LOG = BookDBOpenHelper::class.java.simpleName
    }
}
