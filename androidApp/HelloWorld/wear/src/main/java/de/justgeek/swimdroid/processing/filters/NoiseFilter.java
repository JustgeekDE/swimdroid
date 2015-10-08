package de.justgeek.swimdroid.processing.filters;

import java.util.Arrays;
import java.util.LinkedList;

public class NoiseFilter {

    private LinkedList<Integer> buffer = new LinkedList<>();
    private int threshold = 3;
    private int bufferSize = 9;

    public NoiseFilter(int bufferSize, int threshold) {
        this.bufferSize = bufferSize;
        this.threshold = threshold;
    }

    public int update(int value) {
        shiftIn(value);

        int shouldSkip = 0;
        for (int i = 0; i < buffer.size(); i++) {
            shouldSkip += shouldSkip(i);
        }

        if (shouldSkip > threshold) {
            return getMedianValue();
        }
        return buffer.get(getMiddleIndex());

    }

    private void shiftIn(int value) {
        if (buffer.size() >= bufferSize) {
            buffer.removeFirst();
        }
        buffer.add(value);
    }

    private int getMiddleIndex() {
        return buffer.size() / 2;
    }

    private int getMedianValue() {
        Integer[] numArray = buffer.toArray(new Integer[buffer.size()]);
        ;
        Arrays.sort(numArray);
        int median = (int) numArray[numArray.length / 2];
        return median;
    }

    private int shouldSkip(int index) {
        if (index == getMiddleIndex()) {
            return 0;
        }

        if (buffer.get(index) != buffer.get(getMiddleIndex())) {
            return 1;
        }
        return 0;
    }
}