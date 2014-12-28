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

package com.alchemiasoft.book.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.alchemiasoft.book.R;
import com.alchemiasoft.book.activity.HomeActivity;
import com.alchemiasoft.book.content.BookDB;
import com.alchemiasoft.book.model.Book;

/**
 * Service that allows to buy
 * <p/>
 * Created by Simone Casagranda on 28/12/14.
 */
public class PurchaseService extends IntentService {

    /**
     * Tag used for logging purposes.
     */
    private static final String TAG_LOG = PurchaseService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 37;

    /**
     * Available params.
     */
    private static final String KEY_BOOK_ID = "com.alchemiasoft.book.service.BOOK_ID";
    private static final String KEY_ACTION = "com.alchemiasoft.book.service.ACTION";
    private static final String KEY_NOTIFICATION_ID = "com.alchemiasoft.book.service.NOTIFICATION_ID";
    private static final String KEY_WEARABLE_INPUT = "com.alchemiasoft.book.service.WEARABLE_INPUT";

    /**
     * Not valid id for book.
     */
    private static final long NOT_VALID_BOOK = -1L;

    /**
     * Not valid id for notifications.
     */
    private static final int NOT_VALID_NOTIFICATION = -1;

    /**
     * Available actions.
     */
    private static enum Action {
        BUY, SELL
    }

    public static final class IntentBuilder {

        private final Intent mIntent;

        private IntentBuilder(Context context, Book book, Action action) {
            mIntent = new Intent(context, PurchaseService.class);
            mIntent.putExtra(KEY_BOOK_ID, book.getId());
            mIntent.putExtra(KEY_ACTION, action.ordinal());
        }

        public static IntentBuilder buy(@NonNull Context context, @NonNull Book book) {
            return new IntentBuilder(context, book, Action.BUY);
        }

        public static IntentBuilder sell(@NonNull Context context, @NonNull Book book) {
            return new IntentBuilder(context, book, Action.SELL);
        }

        public IntentBuilder notificationId(int id) {
            mIntent.putExtra(KEY_NOTIFICATION_ID, id);
            return this;
        }

        public IntentBuilder wearableInput() {
            mIntent.putExtra(KEY_WEARABLE_INPUT, true);
            return this;
        }

        public Intent build() {
            return mIntent;
        }
    }

    public PurchaseService() {
        super(TAG_LOG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, NOT_VALID_NOTIFICATION);
        // Cancelling any shown notification
        if (notificationId != NOT_VALID_NOTIFICATION) {
            Log.d(TAG_LOG, "Dismissing notification with id=" + notificationId);
            NotificationManagerCompat.from(this).cancel(notificationId);
        }
        final long bookId = intent.getLongExtra(KEY_BOOK_ID, NOT_VALID_BOOK);
        if (bookId != NOT_VALID_BOOK) {
            final ContentResolver cr = getContentResolver();
            final Action action = Action.values()[intent.getIntExtra(KEY_ACTION, 0)];
            Log.d(TAG_LOG, "Performing action=" + action + " on book with id=" + bookId);
            final ContentValues cv = new ContentValues();
            switch (action) {
                case BUY:
                    cv.put(BookDB.Book.OWNED, 1);
                    if (cr.update(BookDB.Book.create(bookId), cv, null, null) == 1 && intent.getBooleanExtra(KEY_WEARABLE_INPUT, false)) {
                        final Book book = getBook(bookId);
                        if (book != null) {
                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true).setContentTitle(getString(R.string.book_purchased)).setContentText(book.getTitle());
                            builder.setContentIntent(PendingIntent.getActivity(this, 0, HomeActivity.createFor(this, book), PendingIntent.FLAG_UPDATE_CURRENT));

                            // ONLY 4 WEARABLE(s)
                            final NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
                            // ACTION TO SELL A BOOK FROM A WEARABLE
                            final PendingIntent sellIntent = PendingIntent.getService(this, 0, PurchaseService.IntentBuilder.sell(this, book).notificationId(NOTIFICATION_ID).wearableInput().build(), PendingIntent.FLAG_UPDATE_CURRENT);
                            wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_sell, getString(R.string.action_sell), sellIntent).build());
                            // Finally extending the notification
                            builder.extend(wearableExtender);

                            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
                        }
                    }
                    break;
                case SELL:
                    cv.put(BookDB.Book.OWNED, 0);
                    if (cr.update(BookDB.Book.create(bookId), cv, null, null) == 1 && intent.getBooleanExtra(KEY_WEARABLE_INPUT, false)) {
                        final Book book = getBook(bookId);
                        if (book != null) {
                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true).setContentTitle(getString(R.string.book_sold)).setContentText(book.getTitle());
                            builder.setContentIntent(PendingIntent.getActivity(this, 0, HomeActivity.createFor(this, book), PendingIntent.FLAG_UPDATE_CURRENT));

                            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Book getBook(long bookId) {
        final Cursor c = getContentResolver().query(BookDB.Book.create(bookId), null, null, null, null);
        try {
            if (c.moveToNext()) {
                return Book.oneFrom(c);
            }
        } finally {
            c.close();
        }
        return null;
    }
}
