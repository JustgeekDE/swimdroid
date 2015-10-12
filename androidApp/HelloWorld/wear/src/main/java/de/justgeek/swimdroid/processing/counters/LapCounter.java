package de.justgeek.swimdroid.processing.counters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.justgeek.swimdroid.processing.Lap;
import de.justgeek.swimdroid.processing.LapDirection;
import de.justgeek.swimdroid.processing.detectors.BreakDetector;
import de.justgeek.swimdroid.processing.filters.NoiseFilter;


public class LapCounter {
    public static final int MIN_LAP_DURATION = 20000;
    public static final int MIN_LAP_STROKES = 4;

    List<Lap> laps = new ArrayList<>();
    NoiseFilter dataFilter = new NoiseFilter(9, 3);
    StrokeCounter strokeCounter = new StrokeCounter();
    BreakDetector breakDetector = new BreakDetector();

    private Lap currentLap = null;

    public boolean update(float[] sensorData, LapDirection currentDirection, long timestamp) {
        currentDirection = LapDirection.fromInt((int) dataFilter.update(currentDirection.toInt()));
        if (strokeCounter.update(sensorData, currentDirection, timestamp)) {
            breakDetector.update(timestamp);
        }

        if (currentDirection == LapDirection.UNDEFINED) {
            return false;
        }

        if ((currentLap == null) || (currentDirection != currentLap.getDirection())) {
            return startLap(currentDirection, timestamp, sensorData[0]);
        }
        return false;
    }

    public boolean isValidLap(Lap lap) {
        if (lap.duration() < MIN_LAP_DURATION) {
            return false;
        }
        if (lap.getStrokes() < MIN_LAP_STROKES) {
            return false;
        }
        return true;
    }

    public boolean stopLap() {
        if (currentLap != null) {
            long breakTime = breakDetector.stop(strokeCounter.getLastActivity());
            currentLap.stop(strokeCounter.getLastActivity(), strokeCounter.getCount(), breakTime);
            strokeCounter.resetCount();

            if (isValidLap(currentLap)) {
                laps.add(currentLap);
                return true;
            }
        }
        return false;
    }

    public boolean startLap(LapDirection direction, long startTime, float startValue) {
        boolean notFirst = stopLap();
        currentLap = new Lap(direction, startTime);
        strokeCounter.resetCount(startValue);
        return notFirst;
    }

    public Lap getLastLapData() {
        if (laps.size() > 0) {
            return laps.get(laps.size() - 1);
        }
        return null;
    }

    @Override
    public String toString() {
        stopLap();

        Map<String, Object> data = new HashMap<>();
        data.put("lapCount", Integer.valueOf(laps.size()));
        data.put("laps", laps);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(data);
    }
}