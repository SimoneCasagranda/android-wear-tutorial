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

import android.content.Context
import java.io.IOException

/**
 * Utility that can be used to read different types of resources.
 *
 * Created by Simone Casagranda on 23/12/14.
 */
object ResUtil {

    /**
     * Reads an asset as a String.
     *
     * @param context  used to access the asset resource.
     * @param fileName that has to be read.
     * @return the String representation of the file's content.
     * @throws IOException thrown if the file is not found or cannot be read.
     */
    @Throws(IOException::class)
    fun assetAsString(context: Context, fileName: String): String =
        context.assets.open(fileName).bufferedReader().use { it.readText() }
}
