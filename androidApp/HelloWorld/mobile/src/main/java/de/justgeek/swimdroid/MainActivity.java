package de.justgeek.swimdroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import de.justgeek.common.util.BroadcastCallback;
import de.justgeek.common.util.BroadcastHelper;

public class MainActivity extends AppCompatActivity implements BroadcastCallback, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "swimdroid.phone.main";
    private BroadcastHelper broadcastHelper = new BroadcastHelper();
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastHelper.create(this, this);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        broadcastHelper.connect(this);
    }

    @Override
    protected void onStop() {
        // broadcast stop event
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

        broadcastHelper.disconnect(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        log("data changed");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/laps") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    log("Got new data" + dataMap.getString("total"));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }


    public void buttonClicked(View view) {
        System.out.println("Button clicked");

    }

    @Override
    public void handleBroadcast(String type, String data) {
        log("Got new broadcast: " + type);

    }

    private void log(String data) {
        Log.d(TAG, data);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
