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


// The tutorial uses the GoogleApiClient-based Wearable APIs (deprecated, still shipped in play-services-wearable).
@file:Suppress("DEPRECATION")

package com.alchemiasoft.common.util

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

/**
 * Class that can be used to interact with the Wearable api(s).
 *
 * Created by Simone Casagranda on 17/02/15.
 */
object WearableUtil {

    /**
     * Try to get the current local node for the Wearable layer.
     * Be aware that is a blocking call that should not be called from the main thread.
     *
     * @param context used to access the Wearable layer through the Google Play Services.
     * @return the node associated with the current device.
     */
    fun getLocalNode(context: Context): Node {
        val client = GoogleApiClient.Builder(context).addApi(Wearable.API).build()
        try {
            client.blockingConnect()
            return getLocalNode(client)
        } finally {
            client.disconnect()
        }
    }

    /**
     * Try to get the current local node for the Wearable layer.
     * Be aware that is a blocking call that should not be called from the main thread.
     *
     * @param client used to access the Wearable layer.
     * @return the node associated with the current device.
     */
    fun getLocalNode(client: GoogleApiClient): Node = Wearable.NodeApi.getLocalNode(client).await().node

    /**
     * Allows to get a connected node at a particular position.
     *
     * @param client used to access the Wearable layer.
     * @param pos    that has to be matched.
     * @return the found node or null.
     */
    fun getConnectedNodeAt(client: GoogleApiClient, pos: Int): Node? =
        Wearable.NodeApi.getConnectedNodes(client).await().nodes.getOrNull(pos)
}