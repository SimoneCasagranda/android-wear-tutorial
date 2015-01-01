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

package com.alchemiasoft.book.util;

import android.app.AlarmManager;
import android.content.Context;

/**
 * Utility that allows to get the AlarmManager.
 * <p/>
 * Created by Simone Casagranda on 27/12/14.
 */
public class AlarmUtil {

    private AlarmUtil() {
        throw new RuntimeException("Use static methods instead of trying to instantiate this utility.");
    }

    public static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}
