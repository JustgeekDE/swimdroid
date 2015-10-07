package de.justgeek.helloworld.processing;

public class Average {
    private final double decayRate;
    private final double minDistance;

    private double average;

    public Average() {
        this(0.0, 0.9, 50);
    }

    public Average(double startValue, double decayRate, double minDistance) {
        this.decayRate = decayRate;
        this.minDistance = minDistance;

        average = startValue;
    }

    public AverageResult classify(double value) {
        double distance = Math.abs(value - average);
        if (distance < minDistance) {
            return AverageResult.UNDEFINED;
        }

        if (value > average) {
            return AverageResult.ABOVE;
        }

        return AverageResult.BELOW;
    }

    public AverageResult update(double value) {
        AverageResult result = classify(value);

        average = (average * decayRate) + (value * (1.0 - decayRate));
        return result;
    }

    public double getAverage() {
        return average;
    }
}
