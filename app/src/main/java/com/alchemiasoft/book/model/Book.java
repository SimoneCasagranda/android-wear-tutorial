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

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.alchemiasoft.book.content.BookDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity that represents a Book.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class Book {

    public static final long NOT_VALID = -1L;

    private long mId = NOT_VALID;

    private String mTitle;
    private String mAuthor;
    private int mPages;
    private boolean mOwned;

    private Book() {
    }

    public static Book create(String title, String author, int pages, boolean owned) {
        final Book book = new Book();
        book.mTitle = title;
        book.mAuthor = author;
        book.mPages = pages;
        book.mOwned = owned;
        return book;
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

    public static Book oneFrom(@NonNull JSONObject json) {
        final Book book = new Book();
        book.mTitle = json.optString("title");
        book.mAuthor = json.optString("author");
        book.mPages = json.optInt("pages");
        return book;
    }

    public static List<Book> allFrom(@NonNull JSONArray jsonArr) {
        final List<Book> books = new ArrayList<>();
        for (int i = 0; i < jsonArr.length(); i++) {
            final Book book = oneFrom(jsonArr.optJSONObject(i));
            books.add(book);
        }
        return books;
    }

    public ContentValues toValues() {
        final ContentValues cv = new ContentValues();
        cv.put(BookDB.Book.TITLE, mTitle);
        cv.put(BookDB.Book.AUTHOR, mAuthor);
        cv.put(BookDB.Book.PAGES, mPages);
        cv.put(BookDB.Book.OWNED, mOwned);
        return cv;
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

    public void setId(long mId) {
        this.mId = mId;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public void setPages(int mPages) {
        this.mPages = mPages;
    }

    public void setOwned(boolean mOwned) {
        this.mOwned = mOwned;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(mTitle).append(" - ").append(mAuthor).append(" (").append(mPages).append(").");
        return sb.toString();
    }
}
