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


package com.alchemiasoft.common.sync

import android.util.Log
import com.alchemiasoft.common.content.BookDB
import com.alchemiasoft.common.util.ArraysUtil
import com.alchemiasoft.common.util.WearableUtil
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService

/**
 * Service that allows the Google Play Services to bind to our application.
 * In this way it's possible to synchronize data between the handheld and wearable devices.
 *
 * Created by Simone Casagranda on 08/02/15.
 */
class BooksSyncService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Log.d(TAG_LOG, "onDataChanged($dataEvents)")
        // Extracting (freezing) the DataEvent(s): the buffer is released when this method returns.
        val events = dataEvents.map { it.freeze() }
        // Running through all the events
        for (event in events) {
            if (event.type != DataEvent.TYPE_CHANGED) {
                continue
            }
            // Checking if it's the same node that has fired the event
            val node = event.dataItem.uri.host
            val localNode = WearableUtil.getLocalNode(this).id
            if (node == localNode) {
                Log.d(TAG_LOG, "Skipping Event because fired from the same receiver.")
                continue
            }
            val item = Event.DataApi.Item.from(event.dataItem)
            // In a real application case you can create a builder for these kind of operations,
            // it definitely keeps the code cleaner, more readable and maintainable.
            val timeArg = arrayOf(item.time.toString())
            val where = item.where
            val (selection, selectionArgs) = if (where.isNullOrEmpty()) {
                WHERE_BEFORE to timeArg
            } else {
                "($where) AND $WHERE_BEFORE" to ArraysUtil.concatenate(item.whereArgs, timeArg)
            }
            contentResolver.update(item.uri, item.values, selection, selectionArgs)
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG_LOG, "onMessageReceived(id=" + messageEvent.sourceNodeId + " & path=" + messageEvent.path + ")")
    }

    @Deprecated("Deprecated in Play Services; kept for the tutorial's logging.")
    override fun onPeerConnected(peer: Node) {
        @Suppress("DEPRECATION")
        super.onPeerConnected(peer)
        Log.d(TAG_LOG, "onPeerConnected(peer=" + peer.id + "|" + peer.displayName + ")")
    }

    @Deprecated("Deprecated in Play Services; kept for the tutorial's logging.")
    override fun onPeerDisconnected(peer: Node) {
        @Suppress("DEPRECATION")
        super.onPeerDisconnected(peer)
        Log.d(TAG_LOG, "onPeerDisconnected(peer=" + peer.id + "|" + peer.displayName + ")")
    }

    companion object {
        /**
         * Tag used for logging.
         */
        private val TAG_LOG = BooksSyncService::class.java.simpleName

        /**
         * The update has always to be performed if the content is old.
         */
        private const val WHERE_BEFORE = BookDB.Book.UPDATED_AT + " < ?"
    }
}
