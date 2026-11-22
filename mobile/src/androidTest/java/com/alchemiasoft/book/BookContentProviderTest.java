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

package com.alchemiasoft.book;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.alchemiasoft.common.content.BookDB;
import com.alchemiasoft.common.model.Book;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Checks that the app's BookContentProvider is seeded from books.json and persists writes.
 */
@RunWith(AndroidJUnit4.class)
public class BookContentProviderTest {

    private final Context mContext = ApplicationProvider.getApplicationContext();

    @Test
    public void applicationIdIsShared() {
        // Phone and watch must share the applicationId for the Wear Data Layer to pair them.
        assertEquals("com.alchemiasoft.android_wear_tutorial", mContext.getPackageName());
    }

    @Test
    public void seededBooksAreAvailable() {
        try (Cursor c = mContext.getContentResolver().query(BookDB.Book.CONTENT_URI, null, null, null, null)) {
            assertNotNull(c);
            assertTrue("Expected seeded books", c.getCount() > 0);
            assertTrue(c.moveToFirst());
            final Book book = Book.oneFrom(c);
            assertFalse(TextUtils.isEmpty(book.getTitle()));
            assertFalse(TextUtils.isEmpty(book.getAuthor()));
        }
    }

    @Test
    public void ownedFlagIsPersisted() {
        final ContentResolver cr = mContext.getContentResolver();
        final Book original;
        try (Cursor c = cr.query(BookDB.Book.CONTENT_URI, null, null, null, null)) {
            assertNotNull(c);
            assertTrue(c.moveToFirst());
            original = Book.oneFrom(c);
        }
        final Uri uri = BookDB.Book.create(original.getId());
        try {
            final ContentValues cv = new ContentValues();
            cv.put(BookDB.Book.OWNED, original.isOwned() ? 0 : 1);
            assertEquals(1, cr.update(uri, cv, null, null));
            try (Cursor c = cr.query(uri, null, null, null, null)) {
                assertNotNull(c);
                assertTrue(c.moveToFirst());
                assertEquals(!original.isOwned(), Book.oneFrom(c).isOwned());
            }
        } finally {
            final ContentValues restore = new ContentValues();
            restore.put(BookDB.Book.OWNED, original.isOwned() ? 1 : 0);
            cr.update(uri, restore, null, null);
        }
    }
}
