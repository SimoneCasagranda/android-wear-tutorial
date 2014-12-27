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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Definition for the Book's Database and Provider.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public final class BookDB {

    public static final String NAME = "book.db";
    public static final int VERSION = 2;

    public static final String AUTHORITY = "com.alchemiasoft.book.provider";

    public static final String VND = "/vnd.book.";

    public static final String CONTENT_SCHEME = ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/";

    /**
     * Book's table in the Database.
     */
    public static final class Book implements BaseColumns {

        public static final String TABLE = "Book";

        public static final String TITLE = "title";
        public static final String AUTHOR = "author";
        public static final String PAGES = "pages";
        public static final String SOURCE = "source";
        public static final String DESCRIPTION = "description";
        public static final String OWNED = "owned";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE + " TEXT NOT NULL, " + AUTHOR + " TEXT, " + SOURCE + " TEXT, " + DESCRIPTION + " TEXT, " + PAGES + " INTEGER, " + OWNED + " BOOLEAN);";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE + ";";


        public static final String PATH = "book";
        public static final Uri CONTENT_URI = Uri.parse(CONTENT_SCHEME + PATH);
        public static final String CURSOR_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND + PATH;
        public static final String CURSOR_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND + PATH;
        public static final String ITEM_PATH = PATH + "/#";
        public static final String DIR_PATH = PATH;

        public static Uri create() {
            return CONTENT_URI;
        }

        public static Uri create(long id) {
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
        }
    }
}
