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

package com.alchemiasoft.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.alchemiasoft.common.content.BookDB;
import com.alchemiasoft.common.util.UriUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UriUtil carries query details (limit, where, whereArgs, params) inside the URI; the sync layer relies on it.
 */
@RunWith(AndroidJUnit4.class)
public class UriUtilTest {

    @Test
    public void whereAndArgsRoundTrip() {
        Uri uri = BookDB.Book.create(7L);
        uri = UriUtil.withWhere(uri, BookDB.Book.OWNED + " = ?");
        uri = UriUtil.withWhereArgs(uri, "1", "x");
        assertEquals(BookDB.Book.OWNED + " = ?", UriUtil.getWhere(uri));
        assertArrayEquals(new String[]{"1", "x"}, UriUtil.getWhereArgs(uri));
        assertEquals("7", uri.getLastPathSegment());
    }

    @Test
    public void limitAndParamRoundTrip() {
        Uri uri = UriUtil.withLimit(BookDB.Book.CONTENT_URI, 5);
        uri = UriUtil.withParam(uri, "action", "3");
        assertEquals("5", UriUtil.getLimit(uri));
        assertEquals("3", UriUtil.getParam(uri, "action"));
        assertNull(UriUtil.getWhere(uri));
    }
}
