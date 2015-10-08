package de.justgeek.helloworld.processing.filters;


import java.util.LinkedList;

public class ModuloFilter {
    private LinkedList<Float> buffer = new LinkedList<>();
    private float threshold = 180;
    private int bufferSize = 9;
    private float shiftBy = 360;

    public ModuloFilter(int bufferSize, float threshold) {
        this.bufferSize = bufferSize;
        this.threshold = threshold;
    }

    public float update(float value) {
        shiftIn(value);

        int shouldShift = 0;
        for (int i = 0; i < buffer.size(); i++) {
            shouldShift += shouldShift(i);
        }

        if (shouldShift < 0) {
            return getShiftedValue(getMiddleValue());
        }
        return getMiddleValue();

    }

    private void shiftIn(float value) {
        if (buffer.size() >= bufferSize) {
            buffer.removeFirst();
        }
        buffer.add(value);
    }

    private int getMiddleIndex() {
        return buffer.size() / 2;
    }

    private float getMiddleValue() {
        return buffer.get(getMiddleIndex());
    }

    private int shouldShift(int index) {
        if (index == getMiddleIndex()) {
            return 0;
        }

        float reference = buffer.get(index);
        float value = buffer.get(getMiddleIndex());

        float shifted = getShiftedValue(value);
        if (Math.abs(reference - value) < Math.abs(reference - shifted)) {
            return 1;
        }
        return -1;

    }

    private float getShiftedValue(float value) {
        float shifted = value + shiftBy;
        if (value > threshold) {
            shifted = value - shiftBy;
        }
        return shifted;
    }

}
