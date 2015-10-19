package de.justgeek.swimdroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.justgeek.swimdroid.processing.models.PoolLength;
import de.justgeek.swimdroid.processing.models.Session;
import de.justgeek.swimdroid.processing.models.SessionHistory;
import de.justgeek.swimdroid.util.BroadcastCallback;
import de.justgeek.swimdroid.util.BroadcastHelper;


public class TrackerActivity extends WearableActivity implements BroadcastCallback {

    private static final String TAG = "swimdroid.activity.main";
    GoogleApiClient googleApiClient;
    boolean mBound = false;
    private ImageButton button;
    private TextView lapCounterField;
    private TextView lapTimeField;
    private boolean recording = false;
    private BroadcastHelper broadcastHelper = new BroadcastHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastHelper.create(this, this);
        setUpUi();

        // Build a new GoogleApiClient for the the Wearable API
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        startService();
    }

    private void setUpUi() {
        setContentView(R.layout.activity_tracker);
        setAmbientEnabled();
        lapCounterField = (TextView) findViewById(R.id.lapCounter);
        lapTimeField = (TextView) findViewById(R.id.lapTime);
        button = (ImageButton) findViewById(R.id.startButton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        broadcastHelper.connect(this);
    }

    @Override
    protected void onStop() {
        // broadcast stop event

        broadcastHelper.disconnect(this);
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
        if (isAmbient()) {
//            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//            mTextView.setTextColor(getResources().getColor(android.R.color.white));
//            mClockView.setVisibility(View.VISIBLE);

//            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
//            mContainerView.setBackground(null);
//            mTextView.setTextColor(getResources().getColor(android.R.color.black));
//            mClockView.setVisibility(View.GONE);
        }
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
            broadcastHelper.sendBroadcast("start", "");
        } else {
            log("Stopping");
            broadcastHelper.sendBroadcast("stop", "");
        }
    }

    protected void startService() {
        // Bind to LocalService
        Intent intent = new Intent(this, SensorService.class);
        startService(intent);
    }

    protected void stopService() {
    }

    private void log(String data) {
        Log.d(TAG, data);
    }

    private void syncSessionData(Session sessionData) {
        log("Storing lap data: "+sessionData.toString());
        sendData("/laps/"+sessionData.getStart(), sessionData.toString());
    }

    @Override
    public void handleBroadcast(String type, String data) {
        switch (type) {
            case "lap":
                setState(true);
                PoolLength lap = PoolLength.fromString(data);
                lapCounterField.setText(String.valueOf(lap.getNr()));
                lapTimeField.setText(String.valueOf(lap.activeTime()));
                break;
            case "session":
                Session sessionData = Session.fromString(data);
                syncSessionData(sessionData);
                break;
            case "total":
                sendData("/lap/total", data);
                break;
            default:
                break;
        }
    }

    public static final long CONNECTION_TIME_OUT_MS = 5000;

    private void sendData(final String path, final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                List<Node> nodes = result.getNodes();

                for (Node node : nodes) {
                    log("Sending message to " + node.getId());
                    Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, message.getBytes());
                }
                googleApiClient.disconnect();
            }
        }).start();
    }

}
