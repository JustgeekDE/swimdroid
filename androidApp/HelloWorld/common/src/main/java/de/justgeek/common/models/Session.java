package de.justgeek.common.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class Session {
    long start = 0;
    long end = 0;
    int lengthCount = 0;
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
        return (lengths.size()>0);
    }

    public long activeTime() {
        long totalActiveTime = 0;
        for (PoolLength length: lengths) {
            totalActiveTime += length.activeTime();
        }
        return totalActiveTime;
    }

    public long fastestLength() {
        if(lengths.size() < 1) {
            return 0;
        }
        long fastestTime = lengths.get(0).activeTime();
        for (PoolLength length: lengths) {
            if(length.activeTime() < fastestTime) {
                fastestTime = length.activeTime();
            }
        }
        return fastestTime;
    }

    public long slowestLength() {
        long slowestTime = 0;
        for (PoolLength length: lengths) {
            if(length.activeTime() > slowestTime) {
                slowestTime = length.activeTime();
            }
        }
        return slowestTime;
    }

    public int distance() {
        return lengthOfPool * getLengthCount();
    }

    public float averageSpeed() {
        float distanceInKM = distance()/1000.0f;
        float timeInHours = activeTime()/(60.0f * 60.0f * 1000.0f);
        return (distanceInKM/timeInHours);
    }

    public int strokes() {
        int totalStrokes = 0;
        for (PoolLength length: lengths) {
            totalStrokes += length.getStrokes();
        }
        return totalStrokes;
    }

    public long getEnd() {
        return end;
    }
}
