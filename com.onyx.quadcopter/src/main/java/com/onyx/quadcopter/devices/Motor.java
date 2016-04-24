package com.onyx.quadcopter.devices;

import com.onyx.common.utils.Constants;
import com.onyx.quadcopter.control.PwmControl;
import com.onyx.quadcopter.messaging.AclMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * @param id the DeviceID for this Motor.
   * @param pwmPin the GPIO pin for this Motor.
   */
  public Motor(final DeviceId id, final short pwmPin) {
    super(id);
    pwm = new PwmControl(pwmPin);
  }

  @Override
  protected void update() {
    super.update();
    setDisplay("Current speed of " + getId() + " is: " + currentSpeed + "%.");
  }

  @Override
  public void update(final AclMessage msg) {
    switch (msg.getActionId()) {
      case CHANGE_PULSE_WIDTH:
        setPulseWidth((int) msg.getValue());
        LOGGER.debug("PWM Speed changed to " + currentSpeed + "%.");
        break;
      case CHANGE_MOTOR_SPEED:
        setSpeed((int) msg.getValue());
        LOGGER.debug("PWM Speed changed to " + currentSpeed + "%.");
        break;
      case GET_SPEED:
        sendReply(currentSpeed + "");
        break;
      case GET_PULSE_WIDTH:
        sendReply(pwm.getCurrentPulseWidth() + "");
        break;
      default:
        break;
    }
  }

  /**
   * Set the speed of this motor to value.
   *
   * @param value the value to set this motors speed to.
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
