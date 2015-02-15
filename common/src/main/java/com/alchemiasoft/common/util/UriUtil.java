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
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
     * Key used to pass the WHERE statement in the uri request.
     */
    public static final String KEY_WHERE = "where";

    /**
     * Key used to pass the WHERE arguments in the uri request.
     */
    public static final String KEY_WHERE_ARG = "where_arg";

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

    /**
     * Creates a new Uri for the given WHERE statement.
     *
     * @param source for the uri.
     * @param where  that has to be appended.
     * @return an uri that can apply a WHERE filter.
     */
    public static Uri withWhere(@NonNull Uri source, @Nullable String where) {
        if (TextUtils.isEmpty(where)) {
            return source;
        }
        return source.buildUpon().appendQueryParameter(KEY_WHERE, where).build();
    }

    /**
     * Returns the WHERE argument.
     *
     * @param uri used to extract the argument.
     * @return the found value or null.
     */
    public static String getWhere(@NonNull Uri uri) {
        return getWhere(uri, null);
    }

    /**
     * Returns the WHERE argument.
     *
     * @param uri      used to extract the argument.
     * @param fallback value in case of missing value.
     * @return the found value or the fallback one.
     */
    public static String getWhere(@NonNull Uri uri, String fallback) {
        final String value = uri.getQueryParameter(KEY_WHERE);
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    /**
     * Allows to add the WHERE arguments to the URI.
     *
     * @param source that has to be decorated with the given arguments.
     * @param args   those have to be appended.
     * @return the Uri with the appended arguments.
     */
    public static Uri withWhereArgs(@NonNull Uri source, @Nullable String... args) {
        if (args == null || args.length == 0) {
            return source;
        }
        final Uri.Builder builder = source.buildUpon();
        for (int i = 0; i < args.length; i++) {
            builder.appendQueryParameter(KEY_WHERE_ARG, args[i]);

        }
        return builder.build();
    }

    /**
     * Extracts all the encountered WHERE arguments.
     *
     * @param uri used to extract the arguments.
     * @return an array containing the encountered arguments.
     */
    public static String[] getWhereArgs(@NonNull Uri uri) {
        return (String[]) uri.getQueryParameters(KEY_WHERE_ARG).toArray();
    }
}
