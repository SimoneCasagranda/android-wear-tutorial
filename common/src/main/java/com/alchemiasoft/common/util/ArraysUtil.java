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

import android.support.annotation.NonNull;

import java.lang.reflect.Array;

/**
 * Utility that allows to manipulate arrays.
 * <p/>
 * Created by Simone Casagranda on 15/02/15.
 */
public class ArraysUtil {

    private ArraysUtil() {
        throw new RuntimeException("Use static methods instead of trying to instantiate this util");
    }

    /**
     * Concatenate two given non-null arrays.
     *
     * @param first  array that will appear starting from 0.
     * @param second array that will appear just after the first one.
     * @param <T>    type of elements.
     * @return an array that is made of the concatenation of the two.
     */
    public static <T> T[] concatenate(@NonNull T[] first, @NonNull T[] second) {
        final T[] result = (T[]) Array.newInstance(first.getClass().getComponentType(), first.length + second.length);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
