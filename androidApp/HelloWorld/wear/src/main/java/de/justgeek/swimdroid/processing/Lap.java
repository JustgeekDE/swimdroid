package de.justgeek.swimdroid.processing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Lap {
    private LapDirection direction;
    private long start;
    private long end;
    private long strokes;
    private long breakTime;

    public Lap(LapDirection direction, long timestamp) {
        this.direction = direction;
        this.start = timestamp;
        strokes = 0;
    }

    public static Lap fromString(String lapData) {
        Gson gson = new GsonBuilder().create();
        Lap lap = gson.fromJson(lapData, Lap.class);
        return lap;
    }

    public void stop(long timestamp, long strokeCount, long pauses) {
        end = timestamp;
        strokes = strokeCount;
        breakTime = pauses;
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
}
