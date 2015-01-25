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

package com.alchemiasoft.books.fragment;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Fragment that allows to add some notes to a book.
 * <p/>
 * Created by Simone Casagranda on 25/01/15.
 */
public class TakeNoteFragment extends Fragment {

    /**
     * Arguments params.
     */
    private static final String ARG_ID = "com.alchemiasoft.books.fragment.book.ID";

    /**
     * Allows to build BookInfoCardFragment in an extensible way.
     */
    public static final class Builder {

        private final Bundle mArgs = new Bundle();

        private Builder() {
        }

        public static Builder create(long id) {
            final Builder builder = new Builder();
            builder.mArgs.putLong(ARG_ID, id);
            return builder;
        }

        public TakeNoteFragment build() {
            final TakeNoteFragment fragment = new TakeNoteFragment();
            fragment.setArguments(mArgs);
            return fragment;
        }
    }
}
