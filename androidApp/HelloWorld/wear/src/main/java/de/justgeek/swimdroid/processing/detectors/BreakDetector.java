package de.justgeek.swimdroid.processing.detectors;


public class BreakDetector {

    public static final int MAX_STROKE_TIME = 5000;
    long lastStroke;
    long accumulatedBreakTime;

    public void update(long timeStamp) {
        if (lastStroke == 0) {
            lastStroke = timeStamp;
            return;
        }

        long breakTime = timeStamp - lastStroke;
        breakTime -= MAX_STROKE_TIME;

        if (breakTime > 0) {
            accumulatedBreakTime += breakTime;
        }
        lastStroke = timeStamp;
    }

    public long stop(long currentTimeStamp) {
        update(currentTimeStamp);
        lastStroke = currentTimeStamp;

        long temp = accumulatedBreakTime;
        accumulatedBreakTime = 0;

        return temp;
    }
}
