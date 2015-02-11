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

package com.alchemiasoft.book.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;
import android.util.Log;

import com.alchemiasoft.book.R;
import com.alchemiasoft.book.activity.HomeActivity;
import com.alchemiasoft.common.content.BookDB;
import com.alchemiasoft.common.model.Book;
import com.alchemiasoft.common.sync.Event;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/**
 * Service that allows to buy
 * <p/>
 * Created by Simone Casagranda on 28/12/14.
 */
public class BookActionService extends IntentService {

    /**
     * Tag used for logging purposes.
     */
    private static final String TAG_LOG = BookActionService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 37;

    /**
     * Timeout for the GooglePlayClient connection in milliseconds.
     */
    private static final long GOOGLE_PLAY_TIMEOUT = 2000L;

    /**
     * Available params.
     */
    private static final String EXTRA_BOOK_ID = "com.alchemiasoft.book.service.BOOK_ID";
    private static final String EXTRA_NOTIFICATION_ID = "com.alchemiasoft.book.service.NOTIFICATION_ID";
    private static final String EXTRA_WEARABLE_INPUT = "com.alchemiasoft.book.service.WEARABLE_INPUT";
    private static final String EXTRA_ADD_NOTE = "com.alchemiasoft.book.service.ADD_NOTE";

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
        BUY, SELL, ADD_NOTE
    }

    public static final class IntentBuilder {

        private final Intent mIntent;

        private IntentBuilder(Context context, Book book, Action action) {
            mIntent = new Intent(context, BookActionService.class);
            mIntent.putExtra(EXTRA_BOOK_ID, book.getId());
            mIntent.setAction(action.name());
        }

        public static IntentBuilder buy(@NonNull Context context, @NonNull Book book) {
            return new IntentBuilder(context, book, Action.BUY);
        }

        public static IntentBuilder sell(@NonNull Context context, @NonNull Book book) {
            return new IntentBuilder(context, book, Action.SELL);
        }

        public static IntentBuilder addNote(@NonNull Context context, @NonNull Book book) {
            return new IntentBuilder(context, book, Action.ADD_NOTE);
        }

        public IntentBuilder notificationId(int id) {
            mIntent.putExtra(EXTRA_NOTIFICATION_ID, id);
            return this;
        }

        public IntentBuilder wearableInput() {
            mIntent.putExtra(EXTRA_WEARABLE_INPUT, true);
            return this;
        }

        public Intent build() {
            return mIntent;
        }
    }

    public static final class RemoteInputBuilder {

        private final Context mContext;
        private final RemoteInput.Builder mBuilder;

        private RemoteInputBuilder(@NonNull Context context, @NonNull String label) {
            mContext = context.getApplicationContext();
            mBuilder = new RemoteInput.Builder(EXTRA_ADD_NOTE).setLabel(label);
        }

        public static RemoteInputBuilder create(Context context) {
            return new RemoteInputBuilder(context, context.getString(R.string.add_notes));
        }

        public RemoteInputBuilder options(int options) {
            mBuilder.setChoices(mContext.getResources().getStringArray(options));
            return this;
        }

        public RemoteInput build() {
            return mBuilder.build();
        }

    }

    private GoogleApiClient mGoogleApiClient;

    public BookActionService() {
        super(TAG_LOG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Always disconnect the client
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, NOT_VALID_NOTIFICATION);
        // Cancelling any shown notification
        if (notificationId != NOT_VALID_NOTIFICATION) {
            Log.d(TAG_LOG, "Dismissing notification with id=" + notificationId);
            NotificationManagerCompat.from(this).cancel(notificationId);
        }
        final long bookId = intent.getLongExtra(EXTRA_BOOK_ID, NOT_VALID_BOOK);
        if (bookId != NOT_VALID_BOOK) {
            final ContentResolver cr = getContentResolver();
            final Action action = Action.valueOf(intent.getAction());
            Log.d(TAG_LOG, "Performing action=" + action + " on book with id=" + bookId);
            final ContentValues cv = new ContentValues();
            final Uri uri = BookDB.Book.create(bookId);
            switch (action) {
                case BUY:
                    cv.put(BookDB.Book.OWNED, 1);
                    if (cr.update(uri, cv, null, null) == 1 && intent.getBooleanExtra(EXTRA_WEARABLE_INPUT, false)) {
                        final Book book = getBook(bookId);
                        if (book != null) {
                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true).setContentTitle(getString(R.string.book_purchased)).setContentText(book.getTitle());
                            builder.setContentIntent(PendingIntent.getActivity(this, 0, HomeActivity.createFor(this, book), PendingIntent.FLAG_UPDATE_CURRENT));

                            // ONLY 4 WEARABLE(s)
                            final NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
                            wearableExtender.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.background));
                            // ACTION TO SELL A BOOK FROM A WEARABLE
                            final PendingIntent sellIntent = PendingIntent.getService(this, 0, BookActionService.IntentBuilder.sell(this, book).notificationId(NOTIFICATION_ID).wearableInput().build(), PendingIntent.FLAG_UPDATE_CURRENT);
                            wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_sell, getString(R.string.action_sell), sellIntent).build());
                            // Finally extending the notification
                            builder.extend(wearableExtender);

                            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
                        }
                    }
                    // We want to sync with the wearable
                    trySyncWithWearable(uri, cv);
                    break;
                case SELL:
                    cv.put(BookDB.Book.OWNED, 0);
                    if (cr.update(uri, cv, null, null) == 1 && intent.getBooleanExtra(EXTRA_WEARABLE_INPUT, false)) {
                        final Book book = getBook(bookId);
                        if (book != null) {
                            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true).setContentTitle(getString(R.string.book_sold)).setContentText(book.getTitle());
                            builder.setContentIntent(PendingIntent.getActivity(this, 0, HomeActivity.createFor(this, book), PendingIntent.FLAG_UPDATE_CURRENT));

                            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
                        }
                    }
                    // We want to sync with the wearable
                    trySyncWithWearable(uri, cv);
                    break;
                case ADD_NOTE:
                    final CharSequence notes = getExtraNotes(intent);
                    if (!TextUtils.isEmpty(notes)) {
                        cv.put(BookDB.Book.NOTES, notes.toString());
                        cr.update(uri, cv, null, null);
                        // We want to sync with the wearable
                        trySyncWithWearable(uri, cv);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void trySyncWithWearable(final Uri uri, ContentValues cv) {
        if (mGoogleApiClient != null) {
            if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.blockingConnect(GOOGLE_PLAY_TIMEOUT, TimeUnit.MILLISECONDS).isSuccess()) {
                Log.e(TAG_LOG, "Cannot connect to GoogleApiClient.");
                return;
            }
            Wearable.DataApi.putDataItem(mGoogleApiClient, Event.DataApi.Builder.create(uri, cv).asRequest());
        } else {
            Log.e(TAG_LOG, "GoogleApiClient not available.");
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

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private CharSequence getExtraNotes(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            final Bundle result = RemoteInput.getResultsFromIntent(intent);
            if (result != null) {
                return result.getCharSequence(EXTRA_ADD_NOTE);
            }
        }
        return null;
    }
}
