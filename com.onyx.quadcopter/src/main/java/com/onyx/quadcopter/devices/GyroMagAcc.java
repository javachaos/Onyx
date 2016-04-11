package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.utils.Constants;

public class GyroMagAcc extends Device {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(GyroMagAcc.class);

    static {
	try {
	    System.load(Constants.GYRO_NATIVE_LIB);
	} catch (final UnsatisfiedLinkError e) {
	    LOGGER.error(e.getMessage());
	}
    }

    private upm_lsm9ds0.LSM9DS0 lsm;

    public GyroMagAcc(final Controller c) {
	super(c, DeviceID.GYRO_MAG_ACC);
    }

    @Override
    protected void update() {
	lsm.update();
	float[] orient = getRPH();
	if (isNewMessage()) {
	    switch (lastMessage.getActionID()) {
	    case GET_ORIENT:
	    case SEND_DATA:
		sendReply(orient[0] + ":" + orient[1] + ":" + orient[2], lsm.getTemperature());
	    default:
		break;
	    }
	}
    }

    /**
     * Return the Roll Pitch and heading in degree's.
     * 
     * @return
     */
    private float[] getRPH() {
	float[] acceldata = lsm.getAccelerometer();
	float[] magdata = lsm.getMagnetometer();
	float roll = (float) Math.atan2(acceldata[1], acceldata[2]);
	float pitch = 0;
	if (acceldata[1] * Math.sin(roll) + acceldata[2] * Math.cos(roll) == 0) {
	    pitch = (float) (acceldata[0] > 0 ? (Math.PI / 2.0) : (-Math.PI / 2.0));
	} else {
	    pitch = (float) Math.atan(-acceldata[0] / (acceldata[1] * Math.sin(roll) + acceldata[2] * Math.cos(roll)));
	}
	float heading = 0;
	heading = (float) Math.atan2((float) (magdata[2] * Math.sin(roll)) - magdata[1] * Math.cos(roll),
		magdata[0] * Math.cos(pitch) + magdata[1] * Math.sin(pitch) * Math.sin(roll)
			+ magdata[2] * Math.sin(pitch) * Math.cos(pitch));
	return new float[] { (float) Math.toDegrees(roll), (float) Math.toDegrees(pitch),
		(float) Math.toDegrees(heading) };
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
	    float[] rph = getRPH();
	    String msg = "Yaw: " + rph[0] + System.lineSeparator() + "Pitch: " + rph[1] + System.lineSeparator()
		    + "Roll: " + rph[2];
	    LOGGER.debug(msg);
	    sendMessage(DeviceID.OLED_DEVICE, msg, ActionId.DISPLAY);
	    shutdown();
	    init();
	} else {
	    LOGGER.debug(
		    "Yaw: " + Math.random() * 180 + " Pitch: " + Math.random() * 180 + " Roll: " + Math.random() * 180);
	}
    }

    @Override
    public boolean selfTest() {
	if (!Constants.SIMULATION && lsm.getAccelerometer()[2] > 1) {
	    LOGGER.error(
		    "Vertical acceleration exceeds 9.8m/s^2 self test failed, please ensure aircraft is not in motion.");
	    return false;
	}
	return true;
    }

}
