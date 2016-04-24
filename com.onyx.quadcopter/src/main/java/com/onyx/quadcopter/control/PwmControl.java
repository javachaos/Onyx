package com.onyx.quadcopter.control;

import com.onyx.common.utils.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import upm_pca9685.PCA9685;

public class PwmControl {

  /**
   * PCA9685 pwm driver.
   */
  private static PCA9685 pwm = new PCA9685(Constants.I2C_BUS_ID, Constants.PCA9685_I2C_ADDRESS);

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
   * Define ALL Leds.
   */
  private static final short PCA9685_ALL_LED = 0xff;
  
  private int currentPulseWidth;
  
  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(PwmControl.class);

  /**
   * Create a new PwmControl.
   *
   * @param pinNum the GPIO pin to use.
   */
  public PwmControl(final short pinNum) {
    if (pinNum > 15 || pinNum < 0) {
      LOGGER.error("Pin number out of range. " + pinNum);
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
    disable();
    pwm.ledOffTime(PCA9685_ALL_LED, 0);
    enable();
  }

  /**
   * Write pulseWidth to the PWM pin. (In microseconds) Range must be between 1000us and 2000us.
   *
   * @param pulseWidth the pulseWidth to be written.
   */
  public void pwmWrite(final int pulseWidth) {
    if (pulseWidth > 2000 || pulseWidth < 1000) {
      LOGGER.error("Pulse width is out of range. [1000us - 2000us] expected.");
    } else {
      currentPulseWidth = pulseWidth;
      pwm.ledFullOn(pin, false);
      pwm.ledOffTime(pin, 0);
      pwm.ledOnTime(pin, (int) (period - (pulseWidth / scale)));
    }
  }

  /**
   * Set the speed from [0-100].
   * 
   * @param percent the speed from 0 - 100.
   */
  public void setSpeed(final int percent) {
    pwmWrite((10 * percent) + 1000);
  }

  /**
   * Enable this PWM Control.
   */
  public void enable() {
    pwm.ledFullOff(pin, false);
  }

  /**
   * Enable this PWM Control.
   */
  public void disable() {
    pwm.ledFullOff(pin, true);
  }

  public void shutdown() {
    disable();
    pwm.setModeSleep(true);
  }

  /**
   * Get the current Pulse Width.
   * @return the currentPulseWidth
   */
  public int getCurrentPulseWidth() {
    return currentPulseWidth;
  }

}
