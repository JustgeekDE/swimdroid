package de.justgeek.swimdroid;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.justgeek.swimdroid.processing.Lap;
import de.justgeek.swimdroid.processing.LapDirection;
import de.justgeek.swimdroid.processing.counters.LapCounter;
import de.justgeek.swimdroid.processing.detectors.LapClassifier;
import de.justgeek.swimdroid.util.DataLogger;

public class SensorService extends IntentService implements SensorEventListener {

    static final public String SERVICE_HANDLER = "de.justgeek.hellworld.service.NEW_LAP";
    static final public String SERVICE_MESSAGE = "de.justgeek.hellworld.service.NEW_LAP_DATA";
    private static final String TAG = "SensorService";
    private static final String COUNT_KEY = "com.example.key.count";
    private final IBinder mBinder = new LocalBinder();
    private LocalBroadcastManager broadcastManager;
    private Map<String, DataLogger> sensorData = new HashMap<>();
    private SensorManager mSensorManager;
    private boolean running = false;
    private long measureStartTime = 0l;
    private LapClassifier lapClassifier;
    private LapCounter lapCounter;
    private GoogleApiClient mGoogleApiClient;
    private int count = 0;

    public SensorService() {
        this(TAG);
    }

    public SensorService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void createSensorFile(Long timestamp, String sensorName) {
        sensorData.put(sensorName, new DataLogger(sensorName, timestamp));
    }

    private void closeAllSensorFiles() {
        for (DataLogger logger : sensorData.values()) {
            logger.closeFile();
        }
        sensorData = new HashMap<String, DataLogger>();

    }

    private void storeSensorEvent(SensorEvent event) {
        DataLogger logger = sensorData.get(event.sensor.getName());
        logger.storeSensorEvent(event, measureStartTime);
    }

    private long convertTimestampToMS(long timestamp) {
        return (timestamp - measureStartTime) / 1000000l;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        storeSensorEvent(event);
        detectLap(event);
    }

    private void detectLap(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            long timestamp = convertTimestampToMS(event.timestamp);
            lapClassifier.updateAverages(event.values, timestamp);
            LapDirection direction = lapClassifier.getDirection();

            if (lapCounter.update(event.values, direction, timestamp)) {
                Lap lastLap = lapCounter.getLastLapData();
                if (lastLap != null) {
                    sendResult(Long.toString(lastLap.duration()));
                } else {
                    sendResult("00");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void storeLapData() {
        DataLogger logger = new DataLogger("laps");
        logger.store(lapCounter.toString());
        logger.closeFile();
    }

    public void stopRecording() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
        closeAllSensorFiles();
        storeLapData();
        running = false;
        sync();
        stopSelf();
    }

    public void startRecording() {
        if (!running) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Long currentTime = System.currentTimeMillis() / 1000;
            measureStartTime = SystemClock.elapsedRealtimeNanos();

            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sensor : sensors) {
                if (!sensor.isWakeUpSensor()) {
                    if (!mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "Failed to register for updates for sensor " + sensor.getName());
                        }
                    } else {
                        createSensorFile(currentTime, sensor.getName());
                    }
                }
            }
            running = true;
            lapClassifier = new LapClassifier();
            lapClassifier.start();
            lapCounter = new LapCounter();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    public void sendResult(String message) {
        Intent intent = new Intent(SERVICE_HANDLER);
        if (message != null)
            intent.putExtra(SERVICE_MESSAGE, message);
        broadcastManager.sendBroadcast(intent);
    }

    public void sync() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
        putDataMapReq.getDataMap().putInt(COUNT_KEY, count++);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

}
