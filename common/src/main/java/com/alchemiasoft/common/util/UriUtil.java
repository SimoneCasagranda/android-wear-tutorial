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

package com.alchemiasoft.common.util;

import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Utility used to interact with and manipulate Uri(s).
 * <p/>
 * Created by Simone Casagranda on 24/01/15.
 */
public final class UriUtil {

    /**
     * Key used for entries limit in the uri request.
     */
    public static final String KEY_LIMIT = "limit";

    /**
     * Creates a new Uri for the given limit.
     *
     * @param source for the uri.
     * @param limit  of entries desired in the response.
     * @return an uri that can ask for a limited response.
     */
    public static Uri withLimit(@NonNull Uri source, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Cannot create an uri based on " + source + " with limit=" + limit);
        }
        return source.buildUpon().appendQueryParameter(KEY_LIMIT, String.valueOf(limit)).build();
    }

    /**
     * Returns the Maximum number of entries desired in the response.
     *
     * @param uri used to extract the limit value.
     * @return null in case of failure otherwise the limit value.
     */
    public static String getLimit(@NonNull Uri uri) {
        return getLimit(uri, null);
    }

    /**
     * Returns the Maximum number of entries desired in the response.
     *
     * @param uri      used to extract the limit value.
     * @param fallback value in case of the limit key is missing.
     * @return found value or the fallback one.
     */
    public static String getLimit(@NonNull Uri uri, String fallback) {
        final String value = uri.getQueryParameter(KEY_LIMIT);
        return value == null ? fallback : value;
    }
}
