package de.justgeek.helloworld.processing;

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

    public void stop(long timestamp, long strokeCount, long pauses) {
        end = timestamp;
        strokes = strokeCount;
        breakTime = pauses;
    }

    public long duration() {
        return (end - start);
    }

    public long activetime() {
        return (duration() - breakTime);
    }

    public LapDirection getDirection() {
        return direction;
    }

    public long getStrokes() {
        return strokes;
    }
}
