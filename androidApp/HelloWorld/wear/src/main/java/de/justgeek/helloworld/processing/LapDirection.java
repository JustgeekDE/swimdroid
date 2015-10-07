package de.justgeek.helloworld.processing;

public enum LapDirection {
    DIRECTION_A, DIRECTION_B, UNDEFINED;

    public int toInt(){
        switch (this) {
            case DIRECTION_A:
                return 10;
            case DIRECTION_B:
                return -10;

            default:
                return 0;
        }
    }

    public static LapDirection fromInt(int value) {
        if (value > 0) {
            return DIRECTION_A;
        }

        if (value < 0) {
            return DIRECTION_B;
        }

        return UNDEFINED;
    }
}
