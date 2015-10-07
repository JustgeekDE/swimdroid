package de.justgeek.helloworld.processing;

public enum AverageResult {
    ABOVE, BELOW, UNDEFINED;

    public AverageResult getOpposite() {
        switch (this) {
            case ABOVE:
                return BELOW;
            case BELOW:
                return ABOVE;
            default:
                return UNDEFINED;
        }
    }
}
