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

package com.alchemiasoft.common.sync;

import android.util.Log;

import com.alchemiasoft.common.content.BookDB;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * Service that allows the Google Play Services to bind to our application.
 * In this way it's possible to synchronize data between the handheld and wearable devices.
 * <p/>
 * Created by Simone Casagranda on 08/02/15.
 */
public class BooksSyncService extends WearableListenerService {

    /**
     * Tag used for logging.
     */
    private static final String TAG_LOG = BooksSyncService.class.getSimpleName();

    /**
     * The update has always to be performed if the content is old.
     */
    private static final String WHERE_BEFORE = BookDB.Book.UPDATED_AT + " < ?";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.d(TAG_LOG, "onDataChanged(" + dataEvents + ")");
        // Extracting the DataEvent(s).
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        // Running through all the events
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                final Event.DataApi.Item item = Event.DataApi.Item.from(event.getDataItem());
                // TODO extract and append where/whereArgs
                getContentResolver().update(item.uri(), item.values(), WHERE_BEFORE, new String[]{String.valueOf(item.time())});
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG_LOG, "onMessageReceived(id=" + messageEvent.getSourceNodeId() + " & path=" + messageEvent.getPath() + ")");
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.d(TAG_LOG, "onPeerConnected(peer=" + peer.getId() + "|" + peer.getDisplayName() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.d(TAG_LOG, "onPeerDisconnected(peer=" + peer.getId() + "|" + peer.getDisplayName() + ")");
    }
}
