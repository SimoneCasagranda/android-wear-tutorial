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

import java.io.Closeable;
import java.io.IOException;

/**
 * Utility that can be used to work with I/O.
 * <p/>
 * Created by Simone Casagranda on 23/12/14.
 */
public class IOUtils {

    /**
     * Close a closeable without taking care of any error.
     *
     * @param closeable that has to be closed.
     */
    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Shh
            }
        }
    }
}

