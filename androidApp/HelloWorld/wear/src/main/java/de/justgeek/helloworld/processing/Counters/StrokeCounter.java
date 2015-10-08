package de.justgeek.helloworld.processing;


import de.justgeek.helloworld.processing.Detectors.StrokeDetector;
import de.justgeek.helloworld.processing.Filters.ModuloFilter;
import de.justgeek.helloworld.util.DataLogger;

public class StrokeCounter {
    private StrokeDetector detector = new StrokeDetector();
    private long count = 0;
    private long lastActivity = 0;

    private DataLogger logger;
    ModuloFilter dataFilter = new ModuloFilter(5, 180);

    public StrokeCounter() {
        logger = new DataLogger("strokes");
    }

    private void log(float[] sensorData, long timestamp, int updated, int lapDir) {
        String data = String.format("%d,%f,%f,%f,%d,%d\n", timestamp, sensorData[0], dataFilter.update(sensorData[0]), detector.getAverage(), updated, lapDir);
        logger.store(data);

    }

    public boolean update(float[] sensorData, LapDirection lapDirection, long timestamp) {
        int lapInt = (lapDirection.toInt()  * 15) + 150;

        if (lapDirection == LapDirection.UNDEFINED) {
            log(sensorData, timestamp, 0, lapInt);
            return false;
        }
        boolean strokeDetected = check(sensorData, timestamp, detector);
        int strokeIndicator = strokeDetected ? 170 : 0;
        log(sensorData, timestamp, strokeIndicator, lapInt);
        return strokeDetected;
    }

    public void resetCount(float startValue) {
        detector.reset(startValue);
    }

    private boolean check(float[] sensorData, long timestamp, StrokeDetector detector) {
        if (detector.updateAverages(sensorData, timestamp)) {
            lastActivity = timestamp;
            count++;
            return true;
        }
        return false;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public long getCount() {
        return count;
    }

    public void resetCount() {
        count = 0;
    }
}