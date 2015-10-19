package de.justgeek.swimdroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.util.Date;

import de.justgeek.common.models.Session;
import de.justgeek.common.models.SessionHistory;
import de.justgeek.common.util.BroadcastCallback;
import de.justgeek.common.util.BroadcastHelper;

public class MainActivity extends Activity implements BroadcastCallback, DataApi.DataListener,
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

        setTextFieldValue(R.id.sessionDate, toDateString(1445291375l * 1000l));
        setTextFieldValue(R.id.sessionTime, toTimeString(1445291375l * 1000l) + " - " +toTimeString(1445291375l * 1000l));


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
        switch(type) {
            case "history":
                SessionHistory sessionHistory = SessionHistory.fromString(data);
                updateDisplay(sessionHistory);
                break;
        }
    }

    private void updateDisplay(SessionHistory sessionHistory) {
        Session session = sessionHistory.getLastSession();
        if(session!= null){
            setTextFieldValue(R.id.sessionDate, toDateString(session.getStart()));
            setTextFieldValue(R.id.sessionTime, toTimeString(session.getStart()) + " - " + toTimeString(session.getEnd()));
            setTextFieldValue(R.id.lengthCounter, "Lengths: " + session.getLengthCount());
            setTextFieldValue(R.id.activeTime, String.format("Time: %.2f minutes", session.activeTime()/60000.0f));
            setTextFieldValue(R.id.distance, String.format("Distance %dm: ", session.distance()));
            setTextFieldValue(R.id.fastestLength, String.format("Fastest length: %.2fs", session.fastestLength()/1000.0f));
            setTextFieldValue(R.id.slowestLength, String.format("Slowest lengths: %.2fs", session.slowestLength()/1000.0f));
            setTextFieldValue(R.id.speed, String.format("Average speed %.2f: ", session.averageSpeed()));
            setTextFieldValue(R.id.strokeCount, "Total strokes: " + session.strokes());
        }
    }

    private void setTextFieldValue(int id, String newValue) {
        TextView textField = (TextView) findViewById(id);
        textField.setText(newValue);
    }

    private String toDateString(long timestamp){
        DateFormat formater = DateFormat.getDateInstance();
        return formater.format(new Date(timestamp));
    }

    private String toTimeString(long timestamp){
        DateFormat formater = DateFormat.getTimeInstance();
        return formater.format(new Date(timestamp));
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
