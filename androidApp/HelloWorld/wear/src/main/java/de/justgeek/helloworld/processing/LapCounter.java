package de.justgeek.helloworld.processing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.justgeek.helloworld.processing.Filters.NoiseFilter;


public class LapCounter {
    List<Lap> laps = new ArrayList<>();
    NoiseFilter dataFilter = new NoiseFilter(9, 3);

    private Lap currentLap = null;

    public boolean update(LapDirection currentDirection, long timestamp) {
        if (currentDirection == LapDirection.UNDEFINED) {
            return false;
        }

        currentDirection = LapDirection.fromInt((int) dataFilter.update(currentDirection.toInt()));

        if (currentLap == null) {
            currentLap = new Lap(currentDirection, timestamp);
            return false;
        }
        if (currentLap.getDirection() != currentDirection) {
            currentLap.stop(timestamp);
            laps.add(currentLap);
            currentLap = new Lap(currentDirection, timestamp);
            return true;
        }
        return false;
    }

    public Lap getLastLapData() {
        if (laps.size() > 0) {
            return laps.get(laps.size() - 1);
        }
        return null;
    }

    @Override
    public String toString() {
        if (currentLap != null) {
            currentLap.stop(System.currentTimeMillis());
        }
        laps.add(currentLap);

        Map<String, Object> data = new HashMap<>();
        data.put("lapCount", Integer.valueOf(laps.size()));
        data.put("laps", laps);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();;
        return gson.toJson(data);
    }
}
