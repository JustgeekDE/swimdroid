package de.justgeek.helloworld.processing;


import de.justgeek.helloworld.processing.Filters.ModuloFilter;
import de.justgeek.helloworld.processing.Filters.NoiseFilter;
import de.justgeek.helloworld.util.DataLogger;

public class LapClassifier {
    public static final int MINIMAL_LAP_TIME_IN_MS = (20 * 1000);
    private Average orientationAverage = new Average();

    private AverageResult currentDirection = AverageResult.UNDEFINED;
    private long calibrationUpdatesRemaining = 0;

    private long lastSensorUpdate;
    private long lastLapChange;
    private LapDirection lastDirection = LapDirection.UNDEFINED;

    private DataLogger logger;

    ModuloFilter dataFilter = new ModuloFilter(9, 180);
    NoiseFilter lapFilter = new NoiseFilter(9, 3);

    private int counter = 0;

    public void start() {
        orientationAverage = new Average(180, 0.9998, 10);
        currentDirection = AverageResult.UNDEFINED;

        calibrationUpdatesRemaining = 300;

        logger = new DataLogger("classifier");
    }

    public void updateAverages(float[] values, long timestamp) {
        lastSensorUpdate = timestamp;
        float value = dataFilter.update(values[0]);
        currentDirection = orientationAverage.update(value);

        logData(values[0], value, currentDirection);
    }

    private void logData(float value, float filteredValue, AverageResult curDir) {
        counter++;

        LapDirection direction = getLapDirectionFromOrientation(curDir);
        int currentDirection = lapFilter.update(direction.toInt());
        currentDirection = LapDirection.fromInt(currentDirection) == LapDirection.DIRECTION_A ? 190 : -10;

        String data = String.format("%f,%f,%f,%d,%d\n", value, filteredValue, orientationAverage.getAverage(), currentDirection, counter);
        logger.store(data);
    }


    public LapDirection getDirection() {
        if (calibrationUpdatesRemaining > 0) {
            calibrationUpdatesRemaining--;
            return LapDirection.UNDEFINED;
        }

        if (lastSensorUpdate < (lastLapChange + MINIMAL_LAP_TIME_IN_MS)) {
            return lastDirection;
        }

        lastDirection = getLapDirectionFromOrientation(currentDirection);
        lastLapChange = lastSensorUpdate;
        return lastDirection;
    }

    private LapDirection getLapDirectionFromOrientation(AverageResult xDirection) {
        switch (xDirection) {
            case ABOVE:
                return LapDirection.DIRECTION_A;
            case BELOW:
                return LapDirection.DIRECTION_B;
            default:
                return LapDirection.UNDEFINED;
        }
    }
}
