package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.control.PwmControl;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;

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
    public Motor(final DeviceID id, final short pwmPin) {
	super(id);
	pwm = new PwmControl(pwmPin);
    }

    @Override
    public void update(final ACLMessage msg) {
	switch (msg.getActionID()) {
	case CHANGE_PULSE_WIDTH:
	    setPulseWidth((int) msg.getValue());
	    LOGGER.debug("PWM Speed changed to " + currentSpeed + "%.");
	case CHANGE_MOTOR_SPEED:
	    setSpeed((int) msg.getValue());
	    LOGGER.debug("PWM Speed changed to " + currentSpeed + "%.");
	    break;
	default:
	    break;
	}
	sendMessage(DeviceID.OLED_DEVICE, "Current speed of " + getId() + " is: " + currentSpeed + "%.",
		ActionId.DISPLAY);
    }

    /**
     * Set the speed of this motor to value.
     *
     * @param value
     *            the value to set this motors speed to.
     */
    private void setSpeed(final int value) {
	currentSpeed = value;
	pwm.setSpeed(currentSpeed);
    }

    private void setPulseWidth(final int width) {
	currentSpeed = (width - 1000) / 10;
	pwm.pwmWrite(width);
    }

    @Override
    protected void init() {
	pwm.setup();
	setSpeed(Constants.MOTOR_MAX_SPEED);
	try {
	    Thread.sleep(Constants.MOTOR_INIT_DELAY);
	} catch (InterruptedException e) {
	    ExceptionUtils.logError(getClass(), e);
	}
	setSpeed(Constants.MOTOR_INIT_SPEED);
    }

    @Override
    public void shutdown() {
	LOGGER.debug("Shutting down " + getId() + "...");
	setSpeed(0);
	pwm.shutdown();
	LOGGER.debug("Shutdown complete for " + getId() + ".");
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
