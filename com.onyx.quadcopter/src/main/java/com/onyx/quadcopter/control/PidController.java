package com.onyx.quadcopter.control;

import com.onyx.common.utils.Constants;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.messaging.AclMessage;
import com.onyx.quadcopter.messaging.ActionId;

/**
 * A simple PID Controller.
 * 
 * @author fred
 *
 */
public class PidController extends Device {

  private static final double GAIN_P_X = Constants.PID_GAIN_P_X;
  private static final double GAIN_I_X = Constants.PID_GAIN_I_X;
  private static final double GAIN_D_X = Constants.PID_GAIN_D_X;

  private static final double GAIN_P_Y = Constants.PID_GAIN_P_Y;
  private static final double GAIN_I_Y = Constants.PID_GAIN_I_Y;
  private static final double GAIN_D_Y = Constants.PID_GAIN_D_Y;

  private static final double GAIN_P_Z = Constants.PID_GAIN_P_Z;
  private static final double GAIN_I_Z = Constants.PID_GAIN_I_Z;
  private static final double GAIN_D_Z = Constants.PID_GAIN_D_Z;

  private Pid mxPid;
  private Pid myPid;
  private Pid mzPid;

  /**
   * The input orientation provided by gyro.
   */
  private double[] orientation = new double[3];
  private double[] gyro = new double[3];

  /**
   * Computed orientation.
   */
  private double[] computedGyro = new double[3];

  /**
   * The controller throttle.
   */
  private double throttle;
  private double esc1;
  private double esc2;
  private double esc3;
  private double esc4;
  private boolean started;

  /**
   * PID Controller ctor.
   */
  public PidController() {
    super(DeviceId.PID);
    mxPid = new Pid(GAIN_P_X, GAIN_I_X, GAIN_D_X);
    myPid = new Pid(GAIN_P_Y, GAIN_I_Y, GAIN_D_Y);
    mzPid = new Pid(GAIN_P_Z, GAIN_I_Z, GAIN_D_Z);
    mxPid.setSamplePeriod((Constants.CONTROLLER_PERIOD * 1000));
    mxPid.setPoint(0);
    myPid.setPoint(0);
    mzPid.setPoint(0);
  }

  /**
   * Limit the input value between min and max, if the value is less than the min return min, if the
   * value is greater than max return max.
   * 
   * @param max the max value to clamp to.
   * @param min the min value to clamp to.
   * @param value the value.
   * @return the value clamped between min and max.
   */
  private double limit(double max, double min, double value) {
    if (value >= max) {
      return max;
    }
    if (value <= min) {
      return min;
    }
    return value;
  }
  
  @Override
  protected void init() {

  }

  @Override
  public void shutdown() {

  }

  @Override
  protected void alternate() {

  }

  @Override
  public boolean selfTest() {
    return false;
  }

  @Override
  protected void update() {
    super.update();

    computedGyro[0] =
        mxPid.compute((computedGyro[0] * 0.8) + ((gyro[0] / Constants.GYRO_SCALE) * 0.2));
    computedGyro[1] =
        myPid.compute((computedGyro[1] * 0.8) + ((gyro[1] / Constants.GYRO_SCALE) * 0.2));
    computedGyro[2] =
        mzPid.compute((computedGyro[2] * 0.8) + ((gyro[2] / Constants.GYRO_SCALE) * 0.2));

    if (orientation[0] >= Constants.MAX_FLIGHT_INCLINE) {
      computedGyro[0] = 0;
    }

    if (orientation[1] >= Constants.MAX_FLIGHT_INCLINE) {
      computedGyro[1] = 0;
    }

    throttle = limit(Constants.MAX_THROTTLE, 0, throttle);

    final double prev_esc1 = esc1;
    final double prev_esc2 = esc2;
    final double prev_esc3 = esc3;
    final double prev_esc4 = esc4;

    esc1 = throttle - computedGyro[0] + computedGyro[1] - computedGyro[2];
    esc2 = throttle + computedGyro[0] + computedGyro[1] + computedGyro[2];
    esc3 = throttle + computedGyro[0] - computedGyro[1] - computedGyro[2];
    esc4 = throttle - computedGyro[0] - computedGyro[1] + computedGyro[2];

    if (started) { // Flight mode started, keep rotors spinning at 1200us.
      esc1 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc1);
      esc2 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc2);
      esc3 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc3);
      esc4 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc4);
    } else { // Not in flight mode so keep motors quiet at 1000us.
      esc1 = Constants.MOTOR_MIN_MS;
      esc2 = Constants.MOTOR_MIN_MS;
      esc3 = Constants.MOTOR_MIN_MS;
      esc4 = Constants.MOTOR_MIN_MS;
    }

    // Display the Computed ESC Speeds.
    setDisplay("ESC1: " + esc1 + System.lineSeparator() + "ESC2: " + esc2 + System.lineSeparator()
        + "ESC3: " + esc3 + System.lineSeparator() + "ESC4: " + esc4);

    // Only update motor speed if there is a noticeable change in value.
    if (Math.abs(prev_esc1 - esc1) > 0) {
      sendMessageHigh(DeviceId.MOTOR1, "", esc1, ActionId.CHANGE_PULSE_WIDTH);
    }
    if (Math.abs(prev_esc2 - esc2) > 0) {
      sendMessageHigh(DeviceId.MOTOR2, "", esc2, ActionId.CHANGE_PULSE_WIDTH);
    }
    if (Math.abs(prev_esc3 - esc3) > 0) {
      sendMessageHigh(DeviceId.MOTOR3, "", esc3, ActionId.CHANGE_PULSE_WIDTH);
    }
    if (Math.abs(prev_esc4 - esc4) > 0) {
      sendMessageHigh(DeviceId.MOTOR4, "", esc4, ActionId.CHANGE_PULSE_WIDTH);
    }
  }

  @Override
  public void update(AclMessage msg) {
    switch (msg.getActionId()) {
      case GYRO:
        String[] d0 = msg.getContent().split(":");
        gyro[0] = Double.parseDouble(d0[0]);
        gyro[1] = Double.parseDouble(d0[1]);
        gyro[2] = Double.parseDouble(d0[2]);
        break;
      case ORIENT:
        String[] d1 = msg.getContent().split(":");
        orientation[0] = Double.parseDouble(d1[0]);
        orientation[1] = Double.parseDouble(d1[1]);
        orientation[2] = Double.parseDouble(d1[2]);
        break;
      case CONTROL:
        String[] d2 = msg.getContent().split(",");
        mxPid.setPoint(Double.parseDouble(d2[0]));
        myPid.setPoint(Double.parseDouble(d2[1]));
        mzPid.setPoint(Double.parseDouble(d2[2]));
        throttle = Double.parseDouble(d2[3]);
        break;
      case START_MOTORS:
        // Start or stop motors.
        started = Boolean.parseBoolean(msg.getContent());
        throttle = 0;
        break;
      default:
        break;
    }
  }
}
