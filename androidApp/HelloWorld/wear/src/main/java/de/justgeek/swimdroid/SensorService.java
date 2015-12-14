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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.justgeek.common.models.LapDirection;
import de.justgeek.common.models.PoolLength;
import de.justgeek.common.models.Session;
import de.justgeek.common.models.SessionHistory;
import de.justgeek.common.util.BroadcastCallback;
import de.justgeek.common.util.BroadcastHelper;
import de.justgeek.common.util.DataLogger;
import de.justgeek.swimdroid.processing.counters.LapCounter;
import de.justgeek.swimdroid.processing.detectors.LapClassifier;


public class SensorService extends IntentService implements SensorEventListener, BroadcastCallback {

    private static final String TAG = "SensorService";
    private final IBinder mBinder = new LocalBinder();
    private Map<String, DataLogger> sensorData = new HashMap<>();
    private SensorManager mSensorManager;
    private boolean running = false;
    private long measureStartTime = 0l;
    private LapClassifier lapClassifier;
    private LapCounter lapCounter;
    private WakeLock wakeLock;

    private BroadcastHelper broadcastHelper = new BroadcastHelper();


    public SensorService() {
        this(TAG);
    }

    public SensorService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastHelper.create(this.getApplicationContext(), this);

        PowerManager mgr = (PowerManager) this.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SwimDroid");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        broadcastHelper.connect(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        broadcastHelper.disconnect(this);
        super.onDestroy();
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
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {

            long timestamp = convertTimestampToMS(event.timestamp);
            lapClassifier.updateAverages(event.values, timestamp);
            LapDirection direction = lapClassifier.getDirection();

            if (lapCounter.update(event.values, direction, timestamp)) {
                PoolLength lastLap = lapCounter.getSession().getLastLength();
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
        Session session = lapCounter.getSession();

        if (session.isValid()) {
            session.estimatePoolLength();
            SessionHistory history = SessionHistory.load();
            history.addSession(lapCounter.getSession());
            history.store();

            DataLogger logger = new DataLogger("laps");
            logger.store(lapCounter.toString());
            logger.closeFile();
        }
    }

    public void stopRecording() {
        if (running) {
            wakeLock.release();
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mSensorManager.unregisterListener(this);
            closeAllSensorFiles();
            storeLapData();
            running = false;
            sessionEnded(lapCounter.toString());
            broadcastHelper.sendBroadcast("stopped", "done");
        }
    }

    public void startRecording() {
        if (!running) {
            wakeLock.acquire();
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

    private void lapEnded(PoolLength lap) {
        log("Broadcasting lap data");
        broadcastHelper.sendBroadcast("lap", lap.toString());
    }

    private void sessionEnded(String jsonData) {
        log("Broadcasting session data");
        broadcastHelper.sendBroadcast("session", lapCounter.toString());
    }


    private void log(String data) {
        Log.v(TAG, data);
    }

    @Override
    public void handleBroadcast(String type, String data) {
        switch (type) {
            case "start":
                startRecording();
                break;
            case "stop":
                stopRecording();
                break;
            default:
                break;
        }
    }

    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }
}
