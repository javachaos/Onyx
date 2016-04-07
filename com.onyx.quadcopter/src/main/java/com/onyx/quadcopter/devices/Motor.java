package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.control.PwmControl;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;

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
        if (isNewMessage()) {
            switch (lastMessage.getActionID()) {
            case CHANGE_MOTOR_SPEED:
                setSpeed((int) lastMessage.getValue());
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
        pwm.setup();
        setSpeed(Constants.GPIO_MOTOR_INIT_SPEED);
    }

    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down " + getId() + "...");
        int i = getSpeed();
        while(i > 0) {
            setSpeed(i--);
            try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		LOGGER.error(e.getMessage());
	    }
        }
        pwm.shutdown();
        LOGGER.debug("Shutdown complete for " + getId() + ".");
    }

    /**
     * Get the current speed.
     * @return
     */
    private int getSpeed() {
	return currentSpeed;
    }

    @Override
    protected void alternate() {
        LOGGER.debug("Current speed of " + getId() + " is: " + currentSpeed + "%.");
    }

    @Override
    public boolean selfTest() {
        return true;
    }

}
