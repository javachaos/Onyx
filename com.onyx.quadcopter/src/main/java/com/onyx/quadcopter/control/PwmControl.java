package com.onyx.quadcopter.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.utils.Constants;
import com.pi4j.wiringpi.SoftPwm;

public class PwmControl {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PwmControl.class);

    /**
     * PWM GPIO Pin.
     */
    private final int pwmPin;

    //private PCA9685 pwm;
    /**
     * Initial PWM speed.
     */
    private final int initialSpeed = 0;

    /**
     * PWM Range. (Effectively sets the PWM frequency to 50Hz.)
     *
     * f = 1 / range * 100uS
     */
    private final int pwmRange = 200;

    /**
     * True for all invocations of PwnControl once setup() has been called.
     */
    private static boolean initialized = false;

    /**
     * Create a new PwmControl.
     *
     * @param pinNum
     *            the GPIO pin to use.
     */
    public PwmControl(final int pinNum) {
        if ((pinNum > Constants.GPIO_MIN) && (pinNum < Constants.GPIO_MAX)) {
            pwmPin = pinNum;
        } else {
            throw new OnyxException("PWM Pin out of range.", LOGGER);
        }
    }

    /**
     * Setup this PWMControl.
     */
    public void setup() {
        if (!initialized) {
            LOGGER.debug("Setting up wiringPi library.");
            com.pi4j.wiringpi.Gpio.wiringPiSetup();
            initialized = true;
        }

        if (Constants.SIMULATION) {
            LOGGER.debug("Setup simulation PWM control on pin: " + pwmPin);
        } else {
            LOGGER.debug("Setup PWM control on pin: " + pwmPin);
            SoftPwm.softPwmCreate(pwmPin, initialSpeed, pwmRange);
        }
    }

    /**
     * Write to the PWM pin.
     *
     * @param value
     *            the value to be written.
     */
    public void pwmWrite(final int value) {
        if ((value < 0) || (value > 200)) {
            throw new OnyxException("Cannot set pwmWrite of: " + value, LOGGER);
        }
        if (!Constants.SIMULATION) {
            SoftPwm.softPwmWrite(pwmPin, value);
        } else {
            LOGGER.info("PWM write simulated: " + value);
        }
    }

    public void shutdown() {
        if (!Constants.SIMULATION) {
            SoftPwm.softPwmWrite(pwmPin, initialSpeed);
        }
    }

}
