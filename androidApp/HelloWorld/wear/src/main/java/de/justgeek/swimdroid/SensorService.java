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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.justgeek.swimdroid.processing.Lap;
import de.justgeek.swimdroid.processing.LapDirection;
import de.justgeek.swimdroid.processing.counters.LapCounter;
import de.justgeek.swimdroid.processing.detectors.LapClassifier;
import de.justgeek.swimdroid.util.DataLogger;

public class SensorService extends IntentService implements SensorEventListener {

    static final public String BROADCAST_MESSAGE_HANDLER = "de.justgeek.hellworld.service.swimEvent";
    static final public String BROADCAST_TYPE_KEY = "de.justgeek.hellworld.service.swimEventType";
    static final public String BROADCAST_NEW_LAP_KEY = "de.justgeek.hellworld.service.lapData";
    static final public String BROADCAST_SESSION_KEY = "de.justgeek.hellworld.service.sessionData";
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        storeSensorEvent(event);
        detectLap(event);
    }

    private void storeSensorEvent(SensorEvent event) {
        DataLogger logger = sensorData.get(event.sensor.getName());
        logger.storeSensorEvent(event, measureStartTime);
    }

    private void detectLap(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            long timestamp = convertTimestampToMS(event.timestamp);
            lapClassifier.updateAverages(event.values, timestamp);
            LapDirection direction = lapClassifier.getDirection();

            if (lapCounter.update(event.values, direction, timestamp)) {
                Lap lastLap = lapCounter.getLastLapData();
                if (lastLap != null) {
                    lapEnded(lastLap);
                }
            }
        }
    }

    private long convertTimestampToMS(long timestamp) {
        return (timestamp - measureStartTime) / 1000000l;
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
        sessionEnded(lapCounter.toString());
//        stopSelf();
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

    public void sendBroadcast(String type, String data) {
        Intent intent = new Intent(BROADCAST_MESSAGE_HANDLER);
        intent.putExtra("type", type);
        intent.putExtra("data", data);
        broadcastManager.sendBroadcast(intent);
    }

    private void lapEnded(Lap lap) {
        log("Broadcasting lap data");
        sendBroadcast("lap", lap.toString());
    }

    private void sessionEnded(String jsonData) {
        log("Broadcasting session data");
        sendBroadcast("session", lapCounter.toString());
    }


    private void log(String data) {
        Log.v(TAG, data);
    }

    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }
}
