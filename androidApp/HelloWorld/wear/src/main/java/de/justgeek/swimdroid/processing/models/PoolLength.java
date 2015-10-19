package de.justgeek.swimdroid.processing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.justgeek.swimdroid.processing.LapDirection;

public class PoolLength {
    private LapDirection direction;
    private long start;
    private long end;
    private long strokes;
    private long breakTime;
    private int nr;

    public PoolLength(LapDirection direction, long timestamp) {
        this.direction = direction;
        this.start = timestamp;
        strokes = 0;
    }

    public static PoolLength fromString(String lapData) {
        Gson gson = new GsonBuilder().create();
        PoolLength lap = gson.fromJson(lapData, PoolLength.class);
        return lap;
    }

    public void stop(long timestamp, long strokeCount, long pauses, int count) {
        end = timestamp;
        strokes = strokeCount;
        breakTime = pauses;
        nr = count;
    }

    public long duration() {
        return (end - start);
    }

    public long activeTime() {
        return (duration() - breakTime);
    }

    public LapDirection getDirection() {
        return direction;
    }

    public long getStrokes() {
        return strokes;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public int getNr() {
        return nr;
    }

    public long getStartTime() {
        return start;
    }

    public long getEndTime() {
        return end;
    }
}
