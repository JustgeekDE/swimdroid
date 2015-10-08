package de.justgeek.swimdroid;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TrackerActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    SensorService sensorService;
    boolean mBound = false;
    private ImageButton button;
    private TextView lapCounterField;
    private TextView lapTimeField;
    private boolean recording = false;
    private BroadcastReceiver broadcastReceiver;
    private int laps = 0;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        setAmbientEnabled();
        lapCounterField = (TextView) findViewById(R.id.lapCounter);
        lapTimeField = (TextView) findViewById(R.id.lapTime);
        button = (ImageButton) findViewById(R.id.startButton);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String lapTime = intent.getStringExtra(SensorService.SERVICE_MESSAGE);
                laps += 1;
                String lapCount = String.valueOf(laps);
                lapCounterField.setText(lapCount);
                lapTimeField.setText(lapTime);

            }
        };

        bindService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(SensorService.SERVICE_HANDLER)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        unbindService();
        sensorService.stopSelf();
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
            sensorService.startRecording();
        } else {
            Handler handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {
                    sensorService.stopRecording();
                }
            };
            handler.post(r);
        }
    }

    protected void bindService() {
        // Bind to LocalService
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void unbindService() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

}
