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


package com.alchemiasoft.common.util

import android.net.Uri

/**
 * Utility used to interact with and manipulate Uri(s).
 *
 * Created by Simone Casagranda on 24/01/15.
 */
object UriUtil {

    /**
     * Key used for entries limit in the uri request.
     */
    const val KEY_LIMIT = "limit"

    /**
     * Key used to pass the WHERE statement in the uri request.
     */
    const val KEY_WHERE = "where"

    /**
     * Key used to pass the WHERE arguments in the uri request.
     */
    const val KEY_WHERE_ARG = "where_arg"

    /**
     * Creates a new Uri for the given limit.
     *
     * @param source for the uri.
     * @param limit  of entries desired in the response.
     * @return an uri that can ask for a limited response.
     */
    fun withLimit(source: Uri, limit: Int): Uri {
        require(limit > 0) { "Cannot create an uri based on $source with limit=$limit" }
        return source.buildUpon().appendQueryParameter(KEY_LIMIT, limit.toString()).build()
    }

    /**
     * Returns the Maximum number of entries desired in the response.
     *
     * @param uri      used to extract the limit value.
     * @param fallback value in case of the limit key is missing.
     * @return found value or the fallback one.
     */
    fun getLimit(uri: Uri, fallback: String? = null): String? = uri.getQueryParameter(KEY_LIMIT) ?: fallback

    /**
     * Creates a new Uri for the given WHERE statement.
     *
     * @param source for the uri.
     * @param where  that has to be appended.
     * @return an uri that can apply a WHERE filter.
     */
    fun withWhere(source: Uri, where: String?): Uri = withParam(source, KEY_WHERE, where)

    /**
     * Creates a new Uri for the given param.
     *
     * @param source for the uri.
     * @param key    that has to be used for the query param.
     * @param param  that has to be appended.
     * @return an Uri that contains the given param as a query param.
     */
    fun withParam(source: Uri, key: String, param: String?): Uri {
        if (param.isNullOrEmpty()) {
            return source
        }
        return source.buildUpon().appendQueryParameter(key, param).build()
    }

    /**
     * Return the value associated with the given key.
     *
     * @param uri used to extract the argument.
     * @param key used to get the param.
     * @return the param or null.
     */
    fun getParam(uri: Uri, key: String): String? = uri.getQueryParameter(key)

    /**
     * Returns the WHERE argument.
     *
     * @param uri      used to extract the argument.
     * @param fallback value in case of missing value.
     * @return the found value or the fallback one.
     */
    fun getWhere(uri: Uri, fallback: String? = null): String? {
        val value = uri.getQueryParameter(KEY_WHERE)
        return if (value.isNullOrEmpty()) fallback else value
    }

    /**
     * Allows to add the WHERE arguments to the URI.
     *
     * @param source that has to be decorated with the given arguments.
     * @param args   those have to be appended.
     * @return the Uri with the appended arguments.
     */
    fun withWhereArgs(source: Uri, vararg args: String): Uri {
        if (args.isEmpty()) {
            return source
        }
        val builder = source.buildUpon()
        args.forEach { builder.appendQueryParameter(KEY_WHERE_ARG, it) }
        return builder.build()
    }

    /**
     * Extracts all the encountered WHERE arguments.
     *
     * @param uri used to extract the arguments.
     * @return an array containing the encountered arguments.
     */
    fun getWhereArgs(uri: Uri): Array<String> = uri.getQueryParameters(KEY_WHERE_ARG).toTypedArray()
}
