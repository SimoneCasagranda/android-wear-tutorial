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


package com.alchemiasoft.book.user

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences

/**
 * Entity that wraps the data related to the current user.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
class UserData private constructor(context: Context) {

    enum class SuggestionInterval(val seconds: Int) {
        NEVER(-1), NOW(0), TEN_SECS(10), TWENTY_SECS(20), THIRTY_SECS(30)
    }

    private val mPrefs: SharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val mValues = ContentValues()

    init {
        username(mPrefs.getString(KEY_USERNAME, null))
        suggestionInterval(intervalAt(mPrefs.getInt(KEY_SUGGESTION_INTERVAL, SuggestionInterval.NOW.ordinal)))
    }

    fun username(username: String?): UserData = apply { mValues.put(KEY_USERNAME, username) }

    fun username(): String? = mValues.getAsString(KEY_USERNAME)

    fun suggestionInterval(interval: SuggestionInterval): UserData = apply { mValues.put(KEY_SUGGESTION_INTERVAL, interval.ordinal) }

    fun suggestionInterval(): SuggestionInterval = intervalAt(mValues.getAsInteger(KEY_SUGGESTION_INTERVAL) ?: -1)

    private fun intervalAt(index: Int): SuggestionInterval =
        SuggestionInterval.entries.getOrElse(index) { SuggestionInterval.NEVER }

    companion object {

        private const val PREF_NAME = "userdata.pref"

        const val KEY_USERNAME = "username"
        const val KEY_SUGGESTION_INTERVAL = "suggestion_interval"

        @Volatile
        private var sUserData: UserData? = null

        /**
         * Allows to load the data available for the current user.
         *
         * @return the current active UserData.
         */
        fun load(context: Context): UserData =
            sUserData ?: synchronized(this) {
                sUserData ?: UserData(context).also { sUserData = it }
            }
    }
}
