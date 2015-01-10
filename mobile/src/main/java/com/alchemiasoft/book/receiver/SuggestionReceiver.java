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

package com.alchemiasoft.book.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.format.DateUtils;
import android.util.Log;

import com.alchemiasoft.book.service.SuggestionService;
import com.alchemiasoft.book.user.UserData;
import com.alchemiasoft.common.util.AlarmUtil;

/**
 * WakefulBroadcastReceiver that spawn a SuggestionService when it's time to show the user a book
 * suggestion.
 * <p/>
 * Created by Simone Casagranda on 27/12/14.
 */
public class SuggestionReceiver extends WakefulBroadcastReceiver {

    /**
     * Tag used for logging.
     */
    private static final String TAG_LOG = "Suggestion";

    /**
     * Suggestion action.
     */
    private static final String SUGGESTION_ACTION = "com.alchemiasoft.book.receiver.SUGGESTION";

    /**
     * Pending intent used for the Suggestion update.
     */
    private static PendingIntent sPendingIntent;

    /**
     * Schedule a suggestion alarm.
     *
     * @param context reference.
     * @return true if the alarm has been successfully scheduled, false otherwise.
     */
    public static boolean scheduleSuggestion(Context context) {
        final AlarmManager alarmManager = AlarmUtil.getAlarmManager(context);
        if (sPendingIntent != null) {
            alarmManager.cancel(sPendingIntent);
        }
        final UserData userData = UserData.load(context);
        final UserData.SuggestionInterval interval = userData.suggestionInterval();
        if (interval == UserData.SuggestionInterval.NEVER) {
            return false;
        }
        final Intent intent = new Intent(SUGGESTION_ACTION);
        sPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final long refreshInMills = interval.seconds() * DateUtils.SECOND_IN_MILLIS;
        Log.d(TAG_LOG, "AlarmReceiver: scheduling suggestion in " + interval.seconds() + " sec(s).");
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + refreshInMills, sPendingIntent);
        return true;
    }

    public static void cancelSuggestion(Context context) {
        if (sPendingIntent != null) {
            Log.d(TAG_LOG, "AlarmReceiver: cancelling pending suggestion.");
            final AlarmManager alarmManager = AlarmUtil.getAlarmManager(context);
            alarmManager.cancel(sPendingIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent serviceIntent = new Intent(context, SuggestionService.class);
        startWakefulService(context, serviceIntent);
    }
}
