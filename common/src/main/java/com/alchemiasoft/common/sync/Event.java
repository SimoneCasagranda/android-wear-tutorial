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
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.alchemiasoft.common.content.BookDB;
import com.alchemiasoft.common.util.UriUtil;
import com.alchemiasoft.common.util.WearableUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class that allows to represent a book event.
 * This event can be an action or a data update.
 * <p/>
 * Created by Simone Casagranda on 09/02/15.
 */
public final class Event {

    /**
     * Entry point for the Events associated with the DataApi.
     */
    public static final class DataApi {

        public static final class Builder {

            private Uri mUri;
            private final ContentValues mValues;

            private Builder(Uri uri, ContentValues values) {
                mUri = uri;
                mValues = values;
            }

            public static Builder create(Uri uri, ContentValues values) {
                return new Builder(uri, values);
            }

            public Builder where(String where, String... args) {
                mUri = UriUtil.withWhere(mUri, where);
                mUri = UriUtil.withWhereArgs(mUri, args);
                return this;
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
            private final String mWhere;
            private final String[] mWhereArgs;

            private Item(DataItem item) {
                mDataMap = DataMapItem.fromDataItem(item).getDataMap();
                mUri = item.getUri();
                mWhere = UriUtil.getWhere(mUri);
                mWhereArgs = UriUtil.getWhereArgs(mUri);
            }

            public static Item from(DataItem item) {
                return new Item(item);
            }

            public Uri uri() {
                // Readability versus efficiency (this is a tutorial not production code)
                return Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + BookDB.AUTHORITY + mUri.getPath());
            }

            public String where() {
                return mWhere;
            }

            public String[] whereArgs() {
                return mWhereArgs;
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

    /**
     * Entry point for the Events associated with the Wearable message api.
     */
    public static final class MessageApi {

        private static final String ACTION = "action";

        public static final int OPEN = 23;

        @IntDef({OPEN})
        @Retention(RetentionPolicy.SOURCE)
        private @interface Action {
        }

        /**
         * Utility that allows to build and send a message through the WearableApi,
         * through a builder style.
         */
        public static class Sender {

            private static final byte[] DEFAULT_DATA = new byte[0];

            private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
            private static final ResultCallback<SendMessageResult> SILENT_CALLBACK = new ResultCallback<SendMessageResult>() {
                @Override
                public void onResult(SendMessageResult sendMessageResult) {
                }
            };

            private final GoogleApiClient mClient;
            private String mTarget;
            private long mBookId;
            private int mAction;

            private ResultCallback<SendMessageResult> mCallback;

            private Sender(GoogleApiClient client) {
                mClient = client;
            }

            /**
             * Creates an instance of Sender for the given book and GoogleApiClient.
             *
             * @param client used to send the message.
             * @param bookId that will be sent.
             * @return the Sender instance that supports method chaining.
             */
            public static Sender create(GoogleApiClient client, long bookId) {
                return new Sender(client).bookId(bookId).action(OPEN);
            }

            /**
             * Allows to specify a custom action.
             *
             * @param action associated with the current message.
             * @return the Sender to allow method chaining.
             */
            public Sender action(@Action int action) {
                mAction = action;
                return this;
            }

            /**
             * Allows to set the book id.
             *
             * @param bookId that has to be set.
             * @return the Sender to allow method chaining.
             */
            public Sender bookId(long bookId) {
                mBookId = bookId;
                return this;
            }

            /**
             * Allows to specify a particular target for the message.
             *
             * @param nodeId of the target.
             * @return the Sender to allow method chaining.
             */
            public Sender target(String nodeId) {
                mTarget = nodeId;
                return this;
            }

            /**
             * Allows to set a result Callback that will be notified of the sending status.
             *
             * @param callback that has to be notified.
             * @return the Sender to allow method chaining.
             */
            public Sender callback(ResultCallback<SendMessageResult> callback) {
                mCallback = callback;
                return this;
            }

            /**
             * Sends the message trying to fill missing parameters.
             */
            public void send() {
                if (TextUtils.isEmpty(mTarget)) {
                    final Node node = WearableUtil.getConnectedNoteAt(mClient, 0);
                    mTarget = node == null ? null : node.getId();
                }
                if (TextUtils.isEmpty(mTarget)) {
                    return;
                }
                if (mCallback == null) {
                    mCallback = SILENT_CALLBACK;
                }
                Uri uri = BookDB.Book.create(mBookId);
                uri = UriUtil.withParam(uri, ACTION, String.valueOf(mAction));
                Wearable.MessageApi.sendMessage(mClient, mTarget, uri.toString(), DEFAULT_DATA).setResultCallback(mCallback);
            }

            public void asyncSend() {
                EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        send();
                    }
                });
            }
        }

        /**
         * Allows to handle a MessageEvent granting the possibility to access restricted params.
         */
        public static class Receiver {

            private final Uri mUri;

            private Receiver(MessageEvent event) {
                mUri = Uri.parse(event.getPath());
            }

            /**
             * Creates a new Receiver for the given event.
             *
             * @param event that has to be parsed.
             * @return the Receiver instance for the given event.
             */
            public static Receiver from(MessageEvent event) {
                return new Receiver(event);
            }

            /**
             * @return the book id associated with the event.
             */
            public long bookId() {
                return Long.parseLong(mUri.getLastPathSegment());
            }

            /**
             * @return the action associated with the event.
             */
            public int action() {
                return Integer.parseInt(UriUtil.getParam(mUri, ACTION));
            }
        }
    }
}
