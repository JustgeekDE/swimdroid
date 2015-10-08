package de.justgeek.swimdroid.processing;

public class Average {
    private final float decayRate;
    private final float minDistance;

    private float average;

    public Average() {
        this(0, 0.9f, 50);
    }

    public Average(float startValue, float decayRate, float minDistance) {
        this.decayRate = decayRate;
        this.minDistance = minDistance;

        average = startValue;
    }

    public AverageResult classify(float value) {
        float distance = Math.abs(value - average);
        if (distance < minDistance) {
            return AverageResult.UNDEFINED;
        }

        if (value > average) {
            return AverageResult.ABOVE;
        }

        return AverageResult.BELOW;
    }

    public AverageResult update(float value) {
        AverageResult result = classify(value);

        average = (average * decayRate) + (value * (1.0f - decayRate));
        return result;
    }

    public double getAverage() {
        return average;
    }
}
