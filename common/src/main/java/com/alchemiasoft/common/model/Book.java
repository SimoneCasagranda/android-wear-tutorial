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

package com.alchemiasoft.common.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.alchemiasoft.common.content.BookDB;

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

    private String mServerId;
    private String mTitle;
    private String mAuthor;
    private String mSource;
    private String mDescription;
    private int mPages;
    private String mNotes;
    private boolean mOwned;
    private long mUpdatedAt;
    private String mTag;

    private Book() {
    }

    public static Book oneFrom(@NonNull Cursor c) {
        final Book book = new Book();
        int index;
        if ((index = c.getColumnIndex(BookDB.Book.SERVER_ID)) > -1) {
            book.mServerId = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book._ID)) > -1) {
            book.mId = c.getLong(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.TITLE)) > -1) {
            book.mTitle = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.AUTHOR)) > -1) {
            book.mAuthor = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.SOURCE)) > -1) {
            book.mSource = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.DESCRIPTION)) > -1) {
            book.mDescription = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.PAGES)) > -1) {
            book.mPages = c.getInt(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.NOTES)) > -1) {
            book.mNotes = c.getString(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.OWNED)) > -1) {
            book.mOwned = c.getInt(index) == 1 ? true : false;
        }
        if ((index = c.getColumnIndex(BookDB.Book.UPDATED_AT)) > -1) {
            book.mUpdatedAt = c.getLong(index);
        }
        if ((index = c.getColumnIndex(BookDB.Book.TAG)) > -1) {
            book.mTag = c.getString(index);
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
        book.mServerId = json.optString("serverId");
        book.mTitle = json.optString("title");
        book.mAuthor = json.optString("author");
        book.mSource = json.optString("source");
        book.mDescription = json.optString("description");
        book.mPages = json.optInt("pages");
        book.mTag = json.optString("tag");
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
        cv.put(BookDB.Book.SERVER_ID, mServerId);
        cv.put(BookDB.Book.TITLE, mTitle);
        cv.put(BookDB.Book.AUTHOR, mAuthor);
        cv.put(BookDB.Book.SOURCE, mSource);
        cv.put(BookDB.Book.DESCRIPTION, mDescription);
        cv.put(BookDB.Book.PAGES, mPages);
        cv.put(BookDB.Book.NOTES, mNotes);
        cv.put(BookDB.Book.OWNED, mOwned);
        cv.put(BookDB.Book.TAG, mTag);
        return cv;
    }

    public long getId() {
        return mId;
    }

    public String getServerId() {
        return mServerId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getSource() {
        return mSource;
    }

    public String getDescrition() {
        return mDescription;
    }

    public int getPages() {
        return mPages;
    }

    public boolean isOwned() {
        return mOwned;
    }

    public String getNotes() {
        return mNotes;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public String getTag() {
        return mTag;
    }

    public void setServerId(String serverId) {
        this.mServerId = serverId;
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

    public void setSource(String source) {
        this.mSource = source;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public void setPages(int mPages) {
        this.mPages = mPages;
    }

    public void setOwned(boolean mOwned) {
        this.mOwned = mOwned;
    }

    public void setNotes(String notes) {
        this.mNotes = notes;
    }

    public void setUpdatedAt(long time) {
        this.mUpdatedAt = time;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(mTitle).append(" - ").append(mAuthor).append(" (").append(mPages).append("--" + mServerId + ").");
        return sb.toString();
    }
}
