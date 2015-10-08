package de.justgeek.helloworld.processing.Detectors;


import de.justgeek.helloworld.processing.Average;
import de.justgeek.helloworld.processing.AverageResult;
import de.justgeek.helloworld.processing.Filters.ModuloFilter;
import de.justgeek.helloworld.processing.Filters.NoiseFilter;
import de.justgeek.helloworld.processing.LapDirection;
import de.justgeek.helloworld.util.DataLogger;

public class LapClassifier {
    public static final int MINIMAL_LAP_TIME_IN_MS = (20 * 1000);
    private Average orientationAverage = new Average();

    private AverageResult currentDirection = AverageResult.UNDEFINED;
    private long calibrationUpdatesRemaining = 0;

    private long lastSensorUpdate;
    private long lastLapChange;
    private long nextLapChange;
    private LapDirection lastDirection = LapDirection.UNDEFINED;

    private DataLogger logger;

    ModuloFilter dataFilter = new ModuloFilter(9, 180);
    NoiseFilter lapFilter = new NoiseFilter(9, 3);
    NoiseFilter logFilter = new NoiseFilter(9, 3);

    public void start() {
        orientationAverage = new Average(180, 0.9998f, 10);
        currentDirection = AverageResult.UNDEFINED;

        calibrationUpdatesRemaining = 300;

        lastLapChange = 0;
        nextLapChange = 0;

        logger = new DataLogger("classifier");
    }

    public void updateAverages(float[] values, long timestamp) {
        lastSensorUpdate = timestamp;
        float value = dataFilter.update(values[0]);
        currentDirection = orientationAverage.update(value % 360);

        updateDirection();

        logData(values[0], value, currentDirection);
    }

    private void logData(float value, float filteredValue, AverageResult curDir) {

        int currentDirection = getDirection().toInt() * 30 + 140;
        String data = String.format("%f,%f,%f,%d\n", value, filteredValue, orientationAverage.getAverage(), currentDirection);
        logger.store(data);
    }

    private void updateDirection() {
        LapDirection direction = LapDirection.fromInt(lapFilter.update(getLapDirectionFromOrientation(currentDirection).toInt()));

        if (lastSensorUpdate < nextLapChange) {
            return;
        }

        if ((direction != lastDirection) && (direction != LapDirection.UNDEFINED)){
            lastLapChange = lastSensorUpdate;
        }

        if ((direction != lastDirection) && (lastDirection != LapDirection.UNDEFINED)){
            nextLapChange = lastSensorUpdate;
        }
        lastDirection = direction;

    }

    public LapDirection getDirection() {
        if (calibrationUpdatesRemaining> 0) {
            calibrationUpdatesRemaining--;
            return LapDirection.UNDEFINED;
        }

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