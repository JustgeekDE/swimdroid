package de.justgeek.swimdroid.processing.counters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import de.justgeek.swimdroid.processing.models.PoolLength;
import de.justgeek.swimdroid.processing.LapDirection;
import de.justgeek.swimdroid.processing.detectors.BreakDetector;
import de.justgeek.swimdroid.processing.filters.NoiseFilter;
import de.justgeek.swimdroid.processing.models.Session;


public class LapCounter {
    public static final int MIN_LAP_DURATION = 20000;
    public static final int MIN_LAP_STROKES = 4;

//    List<PoolLength> laps = new ArrayList<>();
    Session session = new Session();
    NoiseFilter dataFilter = new NoiseFilter(9, 3);
    StrokeCounter strokeCounter = new StrokeCounter();
    BreakDetector breakDetector = new BreakDetector();

    private PoolLength currentLength = null;

    public boolean update(float[] sensorData, LapDirection currentDirection, long timestamp) {
        currentDirection = LapDirection.fromInt((int) dataFilter.update(currentDirection.toInt()));
        if (strokeCounter.update(sensorData, currentDirection, timestamp)) {
            breakDetector.update(timestamp);
        }

        if (currentDirection == LapDirection.UNDEFINED) {
            return false;
        }

        if ((currentLength == null) || (currentDirection != currentLength.getDirection())) {
            return startLap(currentDirection, timestamp, sensorData[0]);
        }
        return false;
    }

    public boolean isValidLength(PoolLength length) {
        if (length.duration() < MIN_LAP_DURATION) {
            return false;
        }
        if (length.getStrokes() < MIN_LAP_STROKES) {
            return false;
        }
        return true;
    }

    public boolean stopLap() {
        if (currentLength != null) {
            long breakTime = breakDetector.stop(strokeCounter.getLastActivity());
            currentLength.stop(strokeCounter.getLastActivity(), strokeCounter.getCount(), breakTime, session.getLengthCount());
            strokeCounter.resetCount();

            if (isValidLength(currentLength)) {
                session.addLength(currentLength);
                return true;
            }
        }
        return false;
    }

    public boolean startLap(LapDirection direction, long startTime, float startValue) {
        boolean notFirst = stopLap();
        currentLength = new PoolLength(direction, startTime);
        strokeCounter.resetCount(startValue);
        return notFirst;
    }

    @Override
    public String toString() {
        stopLap();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(session);
    }

    public Session getSession() {
        return session;
    }
}