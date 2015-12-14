package de.justgeek.common.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class Session {
    public static final float MS_PER_HOUR = 1000.0f * 60.0f * 60.0f;
    public static final float M_PER_KM = 1000.0f;

    long start = 0;
    long end = 0;
    int lengthOfPool = 50;
    List<PoolLength> lengths;

    public Session() {
        start = System.currentTimeMillis();
        lengths = new ArrayList<>();
    }

    public static Session fromString(String data) {
        Gson gson = new GsonBuilder().create();
        Session session = gson.fromJson(data, Session.class);
        return session;
    }

    public void addLength(PoolLength length) {
        lengths.add(length);

        end = System.currentTimeMillis();
    }

    public int getLengthCount() {
        return lengths.size() + 1;
    }

    public PoolLength getLastLength() {
        if (lengths.size() > 0) {
            return lengths.get(lengths.size() - 1);
        }
        return null;
    }

    public long getStart() {
        return start;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public boolean isValid() {
        return (lengths.size() > 0);
    }

    public long activeTime() {
        long totalActiveTime = 0;
        for (PoolLength length : lengths) {
            totalActiveTime += length.activeTime();
        }
        return totalActiveTime;
    }

    public PoolLength fastestLength() {
        if (lengths.size() < 1) {
            return null;
        }
        PoolLength fastest = lengths.get(0);

        for (PoolLength length : lengths) {
            if (length.activeTime() < fastest.activeTime()) {
                fastest = length;
            }
        }
        return fastest;
    }

    public PoolLength slowestLength() {
        if (lengths.size() < 1) {
            return null;
        }
        PoolLength slowest = lengths.get(0);

        for (PoolLength length : lengths) {
            if (length.activeTime() > slowest.activeTime()) {
                slowest = length;
            }
        }
        return slowest;
    }

    public int distance() {
        return lengthOfPool * getLengthCount();
    }

    public float averageSpeed() {
        return (lengthOfPool * MS_PER_HOUR) / (averageDuration() * M_PER_KM);
    }

    public long estimatePoolLength() {
        lengthOfPool = 50;

        if(averageSpeed() > 4.5f) {
            lengthOfPool = 25;
        }
        return lengthOfPool;
    }


    public int strokes() {
        int totalStrokes = 0;
        for (PoolLength length : lengths) {
            totalStrokes += length.getStrokes();
        }
        return totalStrokes;
    }

    public long getEnd() {
        return end;
    }

    public List<PoolLength> getLengths() {
        return lengths;
    }

    public long getPoolLength() {
        return lengthOfPool;
    }

    public float averageDuration() {
        return activeTime() / getLengthCount();
    }

    public float averageStrokes() {
        return strokes() / getLengthCount();
    }

    public double getCalories() {
        return Math.round(10.5 * 80 * (activeTime()/MS_PER_HOUR));
    }
}
