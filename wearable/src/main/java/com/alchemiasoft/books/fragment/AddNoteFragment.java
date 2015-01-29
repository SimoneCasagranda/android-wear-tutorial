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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alchemiasoft.books.R;
import com.alchemiasoft.books.service.BookService;

import java.util.List;

/**
 * Fragment that allows to add some notes to a book.
 * <p/>
 * Created by Simone Casagranda on 25/01/15.
 */
public class AddNoteFragment extends Fragment implements DelayedConfirmationView.DelayedConfirmationListener {

    /**
     * Tag used for logging.
     */
    private static final String TAG_LOG = AddNoteFragment.class.getSimpleName();

    /**
     * Arguments params.
     */
    private static final String ARG_ID = "com.alchemiasoft.books.fragment.book.ID";

    /**
     * Code use to request the free-form speech
     */
    private static final int REQUEST_NOTES_CODE = 23;

    /**
     * Timeout delay for confirmation.
     */
    private static final long DELAY_TIMEOUT = 2000L;

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

        public AddNoteFragment build() {
            final AddNoteFragment fragment = new AddNoteFragment();
            fragment.setArguments(mArgs);
            return fragment;
        }
    }

    private DelayedConfirmationView mConfirmationView;
    private TextView mAddNotesTextView;

    private boolean mIsAnimating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_note, container, false);
        mConfirmationView = (DelayedConfirmationView) view.findViewById(R.id.confirm_notes);
        mAddNotesTextView = (TextView) view.findViewById(R.id.add_notes_label);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // The entire "screen" is listening for the click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAnimating) {
                    mConfirmationView.setImageResource(R.drawable.ic_action_notes);
                    mIsAnimating = false;
                    return;
                }
                final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.what_notes));
                startActivityForResult(intent, REQUEST_NOTES_CODE);
            }
        });
        mConfirmationView.setTotalTimeMs(DELAY_TIMEOUT);
        // Registering the listener triggered by the DelayedConfirmationView
        mConfirmationView.setListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_NOTES_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    final String voiceNotes = results.get(0);
                    mAddNotesTextView.setText(voiceNotes);
                    mIsAnimating = true;
                    mConfirmationView.setImageResource(R.drawable.ic_full_cancel);
                    mConfirmationView.start();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onTimerFinished(View view) {
        final Activity activity = getActivity();
        if (activity == null) {
            Log.e(TAG_LOG, "Fragment not attached anymore to the activity (Skipping action).");
            return;
        }
        // Updating the book state
        final long bookId = getArguments().getLong(ARG_ID);
        final String notes = mAddNotesTextView.getText().toString();
        // Scheduling the job in on the BookService
        BookService.Invoker.get(activity).bookId(bookId).notes(notes).invoke();
        // We want to close the wear app
        activity.finish();
    }

    @Override
    public void onTimerSelected(View view) {
        mAddNotesTextView.setText(R.string.add_notes);
        mConfirmationView.reset();
    }
}
