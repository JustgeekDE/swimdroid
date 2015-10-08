package de.justgeek.helloworld.processing.detectors;


import de.justgeek.helloworld.processing.Average;
import de.justgeek.helloworld.processing.AverageResult;
import de.justgeek.helloworld.processing.filters.ModuloFilter;

public class StrokeDetector {
    ModuloFilter dataFilter;
    boolean alternateStroke = false;
    private Average orientationAverage;
    private AverageResult lastDirection;

    public StrokeDetector() {
        reset(180);
    }

    public boolean updateAverages(float[] values, long timestamp) {
        float value = dataFilter.update(values[0]);
        AverageResult currentDirection = orientationAverage.update(value);

        if (currentDirection == AverageResult.UNDEFINED) {
            return false;
        }

        if (currentDirection == lastDirection) {
            return false;
        }
        lastDirection = currentDirection;
        alternateStroke = !alternateStroke;

        return alternateStroke;
    }

    public double getAverage() {
        return orientationAverage.getAverage();
    }

    public void reset(float startValue) {
        dataFilter = new ModuloFilter(5, 180);
        orientationAverage = new Average(startValue, 0.95f, 10);
        lastDirection = AverageResult.UNDEFINED;
    }
}
