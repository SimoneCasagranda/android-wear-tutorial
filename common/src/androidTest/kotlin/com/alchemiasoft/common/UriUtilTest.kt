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


package com.alchemiasoft.common

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.util.ArraysUtil
import com.alchemiasoft.common.util.UriUtil
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UriUtil carries query details (limit, where, whereArgs, params) inside the URI; the sync layer relies on it.
 */
@RunWith(AndroidJUnit4::class)
class UriUtilTest {

    @Test
    fun whereAndArgsRoundTrip() {
        var uri = BookDB.Book.create(7L)
        uri = UriUtil.withWhere(uri, BookDB.Book.OWNED + " = ?")
        uri = UriUtil.withWhereArgs(uri, "1", "x")
        assertEquals(BookDB.Book.OWNED + " = ?", UriUtil.getWhere(uri))
        assertArrayEquals(arrayOf("1", "x"), UriUtil.getWhereArgs(uri))
        assertEquals("7", uri.lastPathSegment)
    }

    @Test
    fun limitAndParamRoundTrip() {
        var uri = UriUtil.withLimit(BookDB.Book.CONTENT_URI, 5)
        uri = UriUtil.withParam(uri, "action", "3")
        assertEquals("5", UriUtil.getLimit(uri))
        assertEquals("3", UriUtil.getParam(uri, "action"))
        assertNull(UriUtil.getWhere(uri))
    }

    @Test
    fun emptyWhereArgsLeaveUriUntouched() {
        val uri = BookDB.Book.create(3L)
        assertEquals(uri, UriUtil.withWhereArgs(uri))
        assertEquals(0, UriUtil.getWhereArgs(uri).size)
    }

    @Test
    fun concatenateKeepsOrder() {
        assertArrayEquals(arrayOf("a", "b", "c"), ArraysUtil.concatenate(arrayOf("a"), arrayOf("b", "c")))
    }
}
