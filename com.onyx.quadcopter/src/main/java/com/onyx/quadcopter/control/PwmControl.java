package com.onyx.quadcopter.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO Implement
public class PwmControl {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PwmControl.class);

    /**
     * Create a new PwmControl.
     *
     * @param pinNum
     *            the GPIO pin to use.
     */
    public PwmControl(final int pinNum) {
    }

    /**
     * Setup this PWMControl.
     */
    public void setup() {
    }

    /**
     * Write to the PWM pin.
     *
     * @param value
     *            the value to be written.
     */
    public void pwmWrite(final int value) {
    }

    public void shutdown() {
    }

}
