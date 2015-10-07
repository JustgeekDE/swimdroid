package de.justgeek.helloworld.processing;

public class Lap {
    private LapDirection direction;
    private long start;
    private long end;

    public Lap(LapDirection direction, long timestamp) {
        this.direction = direction;
        this.start = timestamp;
    }

    public void stop(long timestamp) {
        this.end = timestamp;
    }

    public long duration() {
        return (end - start);
    }

    public LapDirection getDirection() {
        return direction;
    }
}
