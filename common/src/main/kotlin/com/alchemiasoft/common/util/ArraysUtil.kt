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

/**
 * Utility that allows to manipulate arrays.
 *
 * Created by Simone Casagranda on 15/02/15.
 */
object ArraysUtil {

    /**
     * Concatenate two given non-null arrays.
     *
     * @param first  array that will appear starting from 0.
     * @param second array that will appear just after the first one.
     * @return an array that is made of the concatenation of the two.
     */
    fun <T> concatenate(first: Array<T>, second: Array<T>): Array<T> = first + second
}
