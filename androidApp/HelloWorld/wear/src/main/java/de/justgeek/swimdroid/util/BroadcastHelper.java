package de.justgeek.swimdroid.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class BroadcastHelper {
    static final public String BROADCAST_MESSAGE_HANDLER = "de.justgeek.hellworld.service.swimEvent";
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    public void create(Context context, final BroadcastCallback callback) {
        broadcastManager = LocalBroadcastManager.getInstance(context);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                String data = intent.getStringExtra("data");
                callback.handleBroadcast(type, data);
            }
        };
    }

    public void connect(Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver((broadcastReceiver),
                new IntentFilter(BROADCAST_MESSAGE_HANDLER)
        );
    }

    public void disconnect(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    public void sendBroadcast(String type, String data) {
        Intent intent = new Intent(BROADCAST_MESSAGE_HANDLER);
        intent.putExtra("type", type);
        intent.putExtra("data", data);
        broadcastManager.sendBroadcast(intent);
    }

}
