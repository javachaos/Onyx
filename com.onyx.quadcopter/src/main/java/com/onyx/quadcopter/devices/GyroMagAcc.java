package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GyroMagAcc extends Device {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(GyroMagAcc.class);

  static {
    try {
      System.load(Constants.GYRO_NATIVE_LIB);
    } catch (final UnsatisfiedLinkError e1) {
      ExceptionUtils.logError(GyroMagAcc.class, e1);
    }
  }

  private upm_lsm9ds0.LSM9DS0 lsm;

  private float[] orient;

  public GyroMagAcc() {
    super(DeviceId.GYRO_MAG_ACC);
  }

  @Override
  protected void update() {
    super.update();
    lsm.update();
    orient = getRph();
    setDisplay("Yaw: " + orient[0] + System.lineSeparator() + "Pitch: " + orient[1]
        + System.lineSeparator() + "Roll: " + orient[2]);
    float[] gyro = lsm.getGyroscope();
    sendMessage(DeviceId.PID,
        gyro[0] + ":" + gyro[1] + ":" + gyro[2], ActionId.GYRO);
    sendMessage(DeviceId.PID,
        orient[0] + ":" + orient[1] + ":" + orient[2], ActionId.ORIENT);
  }

  @Override
  public void update(final AclMessage msg) {
    switch (msg.getActionId()) {
      case GET_ORIENT:
      case SEND_DATA:
        sendReply(orient[0] + ":" + orient[1] + ":" + orient[2], lsm.getTemperature());
        break;
      default:
        break;
    }
  }

  /**
   * Return the Roll Pitch and heading in degree's.
   * 
   * @return
   *    the orientation. Yaw Pitch and Roll. In Degrees
   */
  private float[] getRph() {
    float[] acceldata = lsm.getAccelerometer();
    float[] magdata = lsm.getMagnetometer();
    float roll = (float) Math.atan2(acceldata[1], acceldata[2]);
    float pitch = 0;
    if (acceldata[1] * Math.sin(roll) + acceldata[2] * Math.cos(roll) == 0) {
      pitch = (float) (acceldata[0] > 0 ? (Math.PI / 2.0) : (-Math.PI / 2.0));
    } else {
      pitch = (float) Math
          .atan(-acceldata[0] / (acceldata[1] * Math.sin(roll) + acceldata[2] * Math.cos(roll)));
    }
    float heading = 0;
    heading =
        (float) Math.atan2((float) (magdata[2] * Math.sin(roll)) - magdata[1] * Math.cos(roll),
            magdata[0] * Math.cos(pitch) + magdata[1] * Math.sin(pitch) * Math.sin(roll)
                + magdata[2] * Math.sin(pitch) * Math.cos(pitch));
    return new float[] {(float) Math.toDegrees(roll), (float) Math.toDegrees(pitch),
        (float) Math.toDegrees(heading)};
  }

  @Override
  protected void init() {
    if (Constants.SIMULATION) {
      lsm = null;
    } else {
      lsm = new upm_lsm9ds0.LSM9DS0(Constants.I2C_BUS_ID);
      lsm.init();
    }
  }

  @Override
  public void shutdown() {
    if (!Constants.SIMULATION) {
      lsm.delete();
      lsm = null;
    }
  }

  @Override
  protected void alternate() {
    if (!Constants.SIMULATION) {
      float[] rph = getRph();
      String msg = "Yaw: " + rph[0] + System.lineSeparator() + "Pitch: " + rph[1]
          + System.lineSeparator() + "Roll: " + rph[2];
      LOGGER.debug(msg);
      shutdown();
      init();
    } else {
      LOGGER.debug("Yaw: " + Math.random() * 180 + " Pitch: " + Math.random() * 180 + " Roll: "
          + Math.random() * 180);
    }
  }

  @Override
  public boolean selfTest() {
    if (!Constants.SIMULATION && lsm.getAccelerometer()[2] > 1) {
      LOGGER.error(
          "Vertical acceleration exceeds 9.8m/s^2 self test failed,"
          + " please ensure aircraft is not in motion.");
      return false;
    }
    return true;
  }

}
