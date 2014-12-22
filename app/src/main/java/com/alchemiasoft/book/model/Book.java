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

package com.alchemiasoft.book.model;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.alchemiasoft.book.content.BookDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity that represents a Book.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class Book {

    private long mId;

    private String mTitle;
    private String mAuthor;
    private int mPages;
    private boolean mOwned;

    private Book() {
    }

    public static Book oneFrom(@NonNull Cursor c) {
        final Book book = new Book();
        int index;
        if ((index = c.getColumnIndex(BookDB.Book._ID)) > -1) {
            book.mId = c.getLong(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.TITLE)) > -1) {
            book.mTitle = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.AUTHOR)) > -1) {
            book.mAuthor = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.PAGES)) > -1) {
            book.mPages = c.getInt(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.OWNED)) > -1) {
            book.mOwned = c.getInt(index) == 1 ? true : false;
        }
        return book;
    }

    public static List<Book> allFrom(@NonNull Cursor c) {
        final List<Book> books = new ArrayList<>();
        while (c.moveToNext()) {
            final Book book = oneFrom(c);
            books.add(book);
        }
        return books;
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public int getPages() {
        return mPages;
    }

    public boolean isOwned() {
        return mOwned;
    }
}
