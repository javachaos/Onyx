package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;

public class Motor extends Device {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Device.class);

    /**
     * The current motor speed.
     */
    private int currentSpeed;

    /**
     * PWM Control pin.
     */
    private final PwmControl pwm;

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
        pwm = new PwmControl(pwmPin);
    }

    @Override
    protected void update() {
        final ACLMessage m = getController().getBlackboard().getMessage(this);
        if (m.isValid()) {
            switch (m.getActionID()) {
            case CHANGE_MOTOR_SPEED:
                setSpeed((int) m.getValue());
                LOGGER.debug("PWM Speed changed to " + currentSpeed + "%.");
                break;
            default:
                break;
            }
        }

    }

    /**
     * Set the speed of this motor to value.
     *
     * @param value
     *            the value to set this motors speed to.
     */
    private void setSpeed(final int value) {
        currentSpeed = value;
        pwm.pwmWrite(currentSpeed);
    }

    @Override
    protected void init() {
        LOGGER.debug("Initializing " + getId() + "...");
        pwm.setup();
        LOGGER.debug("Initializing complete for " + getId() + ".");
    }

    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down " + getId() + "...");
        pwm.shutdown();
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
