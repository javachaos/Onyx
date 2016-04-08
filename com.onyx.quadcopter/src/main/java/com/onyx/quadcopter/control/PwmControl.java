package com.onyx.quadcopter.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.utils.Constants;

import upm_pca9685.PCA9685;

//TODO Implement
public class PwmControl {

    private PCA9685 pwm;

    /**
     * PWM Address.
     */
    private short pin;

    /**
     * Maximum pulse width. In steps.
     */
    private int period = 4096;

    /**
     * Maps from 0-4095 to 0-20000.
     */
    private double scale = 4.884004884;
    
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
    public PwmControl(final short pinNum) {
	if (pinNum > 15 || pinNum < 0) {
	    LOGGER.error("Pin number out of range. "+ pinNum);
	    pin = -1;
	} else {
	    pin = pinNum;
	}
    }

    /**
     * Setup this PWMControl.
     */
    public void setup() {
	pwm.setModeSleep(true);
	pwm.setPrescaleFromHz(Constants.PWM_FREQ);
	pwm.setModeSleep(false);
    }

    /**
     * Write pulseWidth to the PWM pin. (In microseconds)
     * Range must be between 1000us and 2000us.
     *
     * @param pulseWidth
     *            the pulseWidth to be written.
     */
    public void pwmWrite(final int pulseWidth) {
	//TODO Test PWMWrite
	if (pulseWidth > 2000 || pulseWidth < 1000) {
	    LOGGER.error("Pulse width is out of range. [1000us - 2000us] expected.");
	}else {
            pwm.ledOnTime(pin, 0);
            pwm.ledOffTime(pin, (int) (period - (pulseWidth / scale)));
	}
    }
    
    /**
     * Set the speed from [0-100].
     * @param percent
     * 		the speed from 0 - 100.
     */
    public void setSpeed(final int percent) {
	pwmWrite((10*percent) + 1000);
    }

    public void shutdown() {
	pwm.setModeSleep(true);
	pwm.delete();
    }

}