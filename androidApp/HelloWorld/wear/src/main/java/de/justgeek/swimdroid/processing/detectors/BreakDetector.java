package de.justgeek.swimdroid.processing.detectors;


import de.justgeek.common.util.DataLogger;
import de.justgeek.swimdroid.processing.Average;

public class BreakDetector {

    public static final float MAX_STROKE_VARIANCE = 4.0f;
    long lastStroke;
    long accumulatedBreakTime;
    Average strokeTimeAverage = new Average(0, 0.95f, 10);
    DataLogger logger = new DataLogger("breaks");

    public void update(long timeStamp) {

        if (lastStroke == 0) {
            lastStroke = timeStamp;
            return;
        }


        long breakTime = timeStamp - lastStroke;
        strokeTimeAverage.update(breakTime);

        breakTime -= MAX_STROKE_VARIANCE * strokeTimeAverage.getAverage();

        if (breakTime > 0) {
            accumulatedBreakTime += breakTime;
        }
        String data = String.format("%d, %f, %d, %d, %d\n", timeStamp, strokeTimeAverage.getAverage(), breakTime, accumulatedBreakTime, (breakTime > 0 ? 50 : -50));
        logger.store(data);

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
