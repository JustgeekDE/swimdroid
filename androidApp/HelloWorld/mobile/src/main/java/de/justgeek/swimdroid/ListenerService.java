package de.justgeek.swimdroid;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import de.justgeek.common.util.BroadcastCallback;
import de.justgeek.common.util.BroadcastHelper;

public class ListenerService extends WearableListenerService implements BroadcastCallback {

    private static final String WEARABLE_DATA_PATH = "/swimdroid/session";
    private static final String LOG_TAG = "swimdroid.phone.listen";

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        String id = peer.getId();
        String name = peer.getDisplayName();

        Log.d(LOG_TAG, "Connected peer name & ID: " + name + "|" + id);
    }
    String nodeId;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        nodeId = messageEvent.getSourceNodeId();
        Log.d(LOG_TAG, "Got message from: " + nodeId);

        String data = new String(messageEvent.getData());
        String path = messageEvent.getPath();
        switch (path) {
            case "/sessionHistory":
                BroadcastHelper broadcastHelper = new BroadcastHelper();
                broadcastHelper.create(this, this);
                broadcastHelper.connect(this);
                broadcastHelper.sendBroadcast("history", data);
                broadcastHelper.disconnect(this);
        }
    }


    @Override
    public void handleBroadcast(String type, String data) {

    }
}
