package de.justgeek.swimdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import de.justgeek.swimdroid.processing.Lap;

public class TrackerActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "swimdroid.activity.main";
    GoogleApiClient googleApiClient;
    boolean mBound = false;
    private ImageButton button;
    private TextView lapCounterField;
    private TextView lapTimeField;
    private boolean recording = false;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private int laps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        setAmbientEnabled();
        lapCounterField = (TextView) findViewById(R.id.lapCounter);
        lapTimeField = (TextView) findViewById(R.id.lapTime);
        button = (ImageButton) findViewById(R.id.startButton);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        // Build a new GoogleApiClient for the the Wearable API
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                String data = intent.getStringExtra("data");
                switch(type) {
                    case "lap":
                        Lap lap = Lap.fromString(data);
                        laps += 1;
                        lapCounterField.setText(String.valueOf(laps));
                        lapTimeField.setText(String.valueOf(lap.activeTime()));
                        break;
                    case "session":
                        syncSessionData(data);
                        break;
                    default:
                        break;
                }

            }
        };

        startService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(SensorService.BROADCAST_MESSAGE_HANDLER)
        );
    }

    @Override
    protected void onStop() {
        // broadcast stop event

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        stopService();
        super.onStop();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
//        if (isAmbient()) {
//            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//            mTextView.setTextColor(getResources().getColor(android.R.color.white));
//            mClockView.setVisibility(View.VISIBLE);
//
//            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
//        } else {
//            mContainerView.setBackground(null);
//            mTextView.setTextColor(getResources().getColor(android.R.color.black));
//            mClockView.setVisibility(View.GONE);
//        }
    }

    private void setState(boolean recording) {
        this.recording = recording;

        if (recording) {
            button.setImageResource(R.drawable.cancel);
        } else {
            button.setImageResource(R.drawable.play);
        }
    }

    private boolean toggleState() {
        this.recording = !recording;
        setState(recording);
        return recording;
    }

    public void startTapped(View view) {
        boolean active = this.toggleState();

        if (active) {
            log("Starting");
            sendBroadcast("start", "");
        } else {
            log("Stopping");
            sendBroadcast("stop", "");
        }
    }

    protected void startService() {
        // Bind to LocalService
        Intent intent = new Intent(this, SensorService.class);
        startService(intent);
    }

    protected void stopService() {
    }

    public void sendBroadcast(String type, String data) {
        Intent intent = new Intent(SensorService.BROADCAST_MESSAGE_HANDLER);
        intent.putExtra("type", type);
        intent.putExtra("data", data);
        broadcastManager.sendBroadcast(intent);
    }


    private void log(String data) {
        Log.d(TAG, data);
    }

    @Override
    public void onConnected(Bundle bundle) {
        log("connected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        log("connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log("connection failed");
    }

    private void syncSessionData(String lapData){
        String DATA_PATH = "/swimdroid/session";

        DataMap dataMap = new DataMap();
        dataMap.putString("data", lapData);
        new SendToDataLayerThread(DATA_PATH, dataMap).start();
    }

    class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodes.getNodes()) {

                // Construct a DataRequest and send over the data layer
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient,request).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "DataMap: " + dataMap + " sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("myTag", "ERROR: failed to send DataMap");
                }
            }
        }
    }
}
