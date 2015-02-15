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

package com.alchemiasoft.common.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.alchemiasoft.common.content.BookDB;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

/**
 * Class that allows to represent a book event.
 * This event can be an action or a data update.
 * <p/>
 * Created by Simone Casagranda on 09/02/15.
 */
public final class Event {

    public static final class DataApi {

        public static final class Builder {

            private final Uri mUri;
            private final ContentValues mValues;

            private Builder(Uri uri, ContentValues values) {
                mUri = uri;
                mValues = values;
            }

            public static Builder create(Uri uri, ContentValues values) {
                return new Builder(uri, values);
            }

            public PutDataRequest asRequest() {
                final PutDataMapRequest dataMapRequest = PutDataMapRequest.create(mUri.getPath());
                final DataMap dataMap = dataMapRequest.getDataMap();
                if (mValues.containsKey(BookDB.Book.NOTES)) {
                    dataMap.putString(BookDB.Book.NOTES, mValues.getAsString(BookDB.Book.NOTES));
                }
                if (mValues.containsKey(BookDB.Book.OWNED)) {
                    dataMap.putInt(BookDB.Book.OWNED, mValues.getAsInteger(BookDB.Book.OWNED));
                }
                if (mValues.containsKey(BookDB.Book.UPDATED_AT)) {
                    dataMap.putLong(BookDB.Book.UPDATED_AT, mValues.getAsLong(BookDB.Book.UPDATED_AT));
                }
                if (mValues.containsKey(BookDB.Book.TAG)) {
                    dataMap.putString(BookDB.Book.TAG, mValues.getAsString(BookDB.Book.TAG));
                }
                return dataMapRequest.asPutDataRequest();
            }
        }

        public static final class Item {

            private final DataMap mDataMap;
            private final Uri mUri;

            private Item(DataItem item) {
                mDataMap = DataMapItem.fromDataItem(item).getDataMap();
                mUri = item.getUri();
            }

            public static Item from(DataItem item) {
                return new Item(item);
            }

            public Uri uri() {
                // Readability versus efficiency (this is a tutorial not production code)
                return Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + BookDB.AUTHORITY + mUri.getPath());
            }

            public ContentValues values() {
                final ContentValues values = new ContentValues();
                if (mDataMap.containsKey(BookDB.Book.NOTES)) {
                    values.put(BookDB.Book.NOTES, mDataMap.getString(BookDB.Book.NOTES));
                }
                if (mDataMap.containsKey(BookDB.Book.OWNED)) {
                    values.put(BookDB.Book.OWNED, mDataMap.getInt(BookDB.Book.OWNED));
                }
                if (mDataMap.containsKey(BookDB.Book.UPDATED_AT)) {
                    values.put(BookDB.Book.UPDATED_AT, mDataMap.getLong(BookDB.Book.UPDATED_AT));
                }
                if (mDataMap.containsKey(BookDB.Book.TAG)) {
                    values.put(BookDB.Book.TAG, mDataMap.getString(BookDB.Book.TAG));
                }
                return values;
            }

            public long time() {
                return mDataMap.getLong(BookDB.Book.UPDATED_AT, 0L);
            }
        }
    }

    public static final class MessageApi {

    }

}
