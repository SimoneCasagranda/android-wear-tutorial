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

package com.alchemiasoft.book.user;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Entity that wraps the data related to the current user.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class UserData {

    private static final String PREF_NAME = "userdata.pref";

    private static final long DOES_NOT_EXPIRE = Long.MAX_VALUE;

    public static final String KEY_USERNAME = "username";

    private final Context mContext;
    private final SharedPreferences mPrefs;

    private final ContentValues mValues;

    private static UserData sUserData;
    private static final Object LOCK = new Object();

    private UserData(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mPrefs = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mValues = new ContentValues();
        username(mPrefs.getString(KEY_USERNAME, null));
    }

    /**
     * Allows to load the data available for the current user.
     *
     * @param context
     * @return the current active UserData.
     */
    public static UserData load(Context context) {
        // Double check locking
        if (sUserData == null) {
            synchronized (LOCK) {
                if (sUserData == null) {
                    sUserData = new UserData(context);
                }
            }
        }
        return sUserData;
    }

    public UserData username(String username) {
        mValues.put(KEY_USERNAME, username);
        return this;
    }

    public String username() {
        return mValues.getAsString(KEY_USERNAME);
    }
}
