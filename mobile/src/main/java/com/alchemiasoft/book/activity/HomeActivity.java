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

package com.alchemiasoft.book.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alchemiasoft.book.R;
import com.alchemiasoft.book.fragment.BookDetailFragment;
import com.alchemiasoft.book.fragment.BooksFragment;
import com.alchemiasoft.book.receiver.SuggestionReceiver;
import com.alchemiasoft.common.model.Book;
import com.alchemiasoft.common.sync.Event;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

/**
 * Activity that decides weather to display available books or owned books.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class HomeActivity extends AppCompatActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    /**
     * Tag used for logging purposes.
     */
    private static final String TAG_LOG = HomeActivity.class.getSimpleName();

    /**
     * Tag(s) used for fragment transactions.
     */
    private static final String TAG_ALL_BOOKS = "all_books";
    private static final String TAG_OWNED_BOOKS = "owned_books";

    /**
     * Key for the book Id.
     */
    private static final String KEY_BOOK_ID = "com.alchemiasoft.book.activity.BOOK_ID";
    private static final long NOT_VALID = -1L;

    /**
     * Intent that should be used to launch HomeActivity for a given book.
     *
     * @param context reference.
     * @param book    that has to be passed.
     * @return an intent ready to be used to pass info to the HomeActivity.
     */
    public static Intent createFor(@NonNull Context context, @NonNull Book book) {
        final Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(KEY_BOOK_ID, book.getId());
        return intent;
    }

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        applySystemBarInsets(toolbar, findViewById(R.id.content));
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final FragmentManager fm = getSupportFragmentManager();
                // R.id values are not compile-time constants anymore: if/else instead of switch.
                final int itemId = menuItem.getItemId();
                if (itemId == R.id.action_all_books) {
                    if (fm.findFragmentByTag(TAG_ALL_BOOKS) == null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, BooksFragment.create(HomeActivity.this, false), TAG_ALL_BOOKS).commit();
                    } else {
                        Log.d(TAG_LOG, "All books fragment is already attached.");
                    }
                    return true;
                } else if (itemId == R.id.action_my_books) {
                    if (fm.findFragmentByTag(TAG_OWNED_BOOKS) == null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, BooksFragment.create(HomeActivity.this, true), TAG_OWNED_BOOKS).commit();
                    } else {
                        Log.d(TAG_LOG, "Owned books fragment is already attached.");
                    }
                    return true;
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(HomeActivity.this, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.action_suggestion) {
                    // Scheduling a suggestion
                    SuggestionReceiver.scheduleSuggestion(HomeActivity.this);
                    return true;
                } else {
                    Log.w(TAG_LOG, "MenuItem: title=" + menuItem.getTitle() + " & itemId=" + menuItem.getItemId() + " is not handled.");
                    return false;
                }
            }
        });
        toolbar.inflateMenu(R.menu.menu_home);
        // Checking if first instance and then attach the first fragment
        if (savedInstanceState == null) {
            attachBookFragment(getIntent().getLongExtra(KEY_BOOK_ID, NOT_VALID));
        }
        // Since Android 13 notifications (book suggestions, purchase confirmations) need a runtime permission
        requestNotificationPermissionIfNeeded();
        // Creating the GoogleApiClient for the Wearable api (if available)
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build();
        }
    }

    /**
     * Since Android 15 apps are drawn edge-to-edge: keep the toolbar below the status bar
     * and the content above the navigation bar.
     */
    private static void applySystemBarInsets(final View toolbar, final View content) {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            final Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            content.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Trying to bind to the GoogleApiClient
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnecting the client and releasing the wearable updates
        if (mGoogleApiClient != null) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // The Client is connected => we can register for the wearable updates
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final Event.MessageApi.Receiver receiver = Event.MessageApi.Receiver.from(messageEvent);
        switch (receiver.action()) {
            case Event.MessageApi.OPEN:
                attachBookFragment(receiver.bookId());
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        attachBookFragment(intent.getLongExtra(KEY_BOOK_ID, NOT_VALID));
    }

    private void attachBookFragment(final long bookId) {
        if (bookId == NOT_VALID) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, BooksFragment.create(this, false), TAG_ALL_BOOKS).commit();
            // Scheduling a suggestion
            SuggestionReceiver.scheduleSuggestion(this);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, BookDetailFragment.create(bookId)).commit();
        }
    }
}
