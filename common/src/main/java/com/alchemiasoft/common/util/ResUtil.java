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

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility that can be used to read different types of resources.
 * <p/>
 * Created by Simone Casagranda on 23/12/14.
 */
public class ResUtil {

    /**
     * Tag used for logging purposes.
     */
    private static final String TAG_LOG = ResUtil.class.getSimpleName();

    /**
     * Default buffer.
     */
    public static final int BUFFER = 4096;

    /**
     * Reads an asset as a String.
     *
     * @param context  used to access the asset resource.
     * @param fileName that has to be read.
     * @return the String representation of the file's content.
     * @throws IOException thrown if the file is not found.
     */
    public static String assetAsString(Context context, String fileName) throws IOException {
        String res = null;
        final InputStream is = context.getAssets().open(fileName);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final byte[] buf = new byte[BUFFER];
        int numRead;
        try {
            while ((numRead = is.read(buf)) >= 0) {
                os.write(buf, 0, numRead);
            }
            res = new String(os.toByteArray());
        } catch (IOException e) {
            Log.e(TAG_LOG, "Error reading resource from stream");
            e.printStackTrace();
        } finally {
            // Closing opened streams
            IOUtils.closeSilently(os);
            IOUtils.closeSilently(is);
        }
        return res;
    }
}
