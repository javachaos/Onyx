package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.Blackboard;
import com.onyx.quadcopter.utils.Constants;
import com.pi4j.wiringpi.SoftPwm;

public class Motor extends Device {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Device.class);

    /**
     * PWM GPIO Pin.
     */
    private final int pwmPin;

    /**
     * Initial PWM speed.
     */
    private final int initialSpeed = 0;

    /**
     * PWM Range.
     */
    private final int pwmRange = 100;

    /**
     * The current motor speed.
     */
    private double currentSpeed;

    /**
     * Motor constructor.
     *
     * @param c
     *            the controller.
     * @param id
     *            the DeviceID for this Motor.
     * @param pwmPin
     *            the GPIO pin for this Motor.
     */
    public Motor(final Controller c, final DeviceID id, final int pwmPin) {
        super(c, id);
        if ((pwmPin > Constants.GPIO_MIN) && (pwmPin < Constants.GPIO_MAX)) {
            this.pwmPin = pwmPin;
        } else {
            this.pwmPin = -1;
            LOGGER.error("PWM Pin out of range.");
        }
    }

    @Override
    protected void update() {
        final ACLMessage m = Blackboard.getMessage(this);
        switch (m.getActionID()) {
        case CHANGE_MOTOR_SPEED:
            setSpeed(m.getValue());
            LOGGER.debug("PWM Speed changed to " + m.getValue() + "%.");
            break;
        default:
            break;
        }
    }

    /**
     * Set the speed of this motor to value.
     *
     * @param value
     *            the value to set this motors speed to.
     */
    private void setSpeed(final double value) {
        currentSpeed = value;
        SoftPwm.softPwmWrite(pwmPin, (int) value);
    }

    @Override
    protected void init() {
        LOGGER.debug("Initializing " + getId() + "...");
        com.pi4j.wiringpi.Gpio.wiringPiSetup();
        SoftPwm.softPwmCreate(pwmPin, initialSpeed, pwmRange);
        LOGGER.debug("Initializing complete for " + getId() + ".");
    }

    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down " + getId() + "...");
        SoftPwm.softPwmWrite(pwmPin, initialSpeed);
        LOGGER.debug("Shutdown complete for " + getId() + ".");
    }

    @Override
    protected void alternate() {
        LOGGER.debug("Current speed of " + getId() + " is: " + currentSpeed + "%.");
    }

    @Override
    public boolean selfTest() {
        return true;// TODO Motor self test.
    }

}
