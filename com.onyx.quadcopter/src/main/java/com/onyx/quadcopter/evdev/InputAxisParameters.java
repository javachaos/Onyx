package com.onyx.quadcopter.evdev;

/**
 * Represents configurable parameters of an input axis. set*() should affect the
 * value in the device.
 * <p/>
 * Copyright (C) 2009 Giacomo Ferrari
 *
 * @author Giacomo Ferrari
 */
class InputAxisParameters {

    private final EventDevice device;
    private final int axis;

    private static final int VALUE_INDEX = 0;
    private static final int MIN_INDEX = 1;
    private static final int MAX_INDEX = 2;
    private static final int FUZZ_INDEX = 3;
    private static final int FLAT_INDEX = 4;

    public InputAxisParameters(final EventDevice device, final int axis) {
        this.device = device;
        this.axis = axis;
    }

    private int readStatus(final int i) {
        if ((i < 0) || (i > 4)) {
            throw new IllegalArgumentException("Field index has to be between 0 and 4");
        }
        final int[] resp = new int[5];
        synchronized (this) {
            device.ioctlEVIOCGABS(device.getDevicePath(), resp, axis);
        }
        return resp[i];
    }

    public int getValue() {
        return readStatus(VALUE_INDEX);
    }

    public int getMin() {
        return readStatus(MIN_INDEX);
    }

    public int getMax() {
        return readStatus(MAX_INDEX);
    }

    public int getFuzz() {
        return readStatus(FUZZ_INDEX);
    }

    public int getFlat() {
        return readStatus(FLAT_INDEX);
    }

    @Override
    public String toString() {
        return "Value: " + getValue() + " Min: " + getMin() + " Max: " + getMax() + " Fuzz: " + getFuzz() + " Flat: "
                + getFlat();
    }
}
