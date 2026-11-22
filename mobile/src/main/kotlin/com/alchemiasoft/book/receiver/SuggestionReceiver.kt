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


package com.alchemiasoft.book.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import com.alchemiasoft.book.service.SuggestionService
import com.alchemiasoft.book.user.UserData
import com.alchemiasoft.common.util.AlarmUtil

/**
 * BroadcastReceiver that enqueues a SuggestionService job when it's time to show the user a book
 * suggestion.
 *
 * Created by Simone Casagranda on 27/12/14.
 */
class SuggestionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Background services cannot be started from a broadcast since Android 8.0: enqueue a job instead.
        SuggestionService.enqueue(context)
    }

    companion object {

        /**
         * Tag used for logging.
         */
        private const val TAG_LOG = "Suggestion"

        /**
         * Suggestion action.
         */
        private const val SUGGESTION_ACTION = "com.alchemiasoft.book.receiver.SUGGESTION"

        /**
         * Pending intent used for the Suggestion update.
         */
        private var sPendingIntent: PendingIntent? = null

        /**
         * Schedule a suggestion alarm.
         *
         * @return true if the alarm has been successfully scheduled, false otherwise.
         */
        fun scheduleSuggestion(context: Context): Boolean {
            val alarmManager = AlarmUtil.getAlarmManager(context)
            sPendingIntent?.let { alarmManager.cancel(it) }
            val interval = UserData.load(context).suggestionInterval()
            if (interval == UserData.SuggestionInterval.NEVER) {
                return false
            }
            // Implicit broadcasts are not delivered to manifest receivers since Android 8.0: target this receiver explicitly.
            val intent = Intent(context, SuggestionReceiver::class.java).setAction(SUGGESTION_ACTION)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            sPendingIntent = pendingIntent
            val refreshInMills = interval.seconds * DateUtils.SECOND_IN_MILLIS
            Log.d(TAG_LOG, "AlarmReceiver: scheduling suggestion in " + interval.seconds + " sec(s).")
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + refreshInMills, pendingIntent)
            return true
        }

        fun cancelSuggestion(context: Context) {
            sPendingIntent?.let {
                Log.d(TAG_LOG, "AlarmReceiver: cancelling pending suggestion.")
                AlarmUtil.getAlarmManager(context).cancel(it)
            }
        }
    }
}
