package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.MessageType;
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
    private float[] last_orient;

    public GyroMagAcc(final Controller c) {
        super(c, DeviceID.GYRO_MAG_ACC);
    }

    @Override
    protected void update() {
        lsm.update();
        float[] orient = getRPH();
        orient = correct(orient);
        last_orient = orient;
        if (isNewMessage()) {
            switch (lastMessage.getActionID()) {
            case GET_ORIENT:
            case SEND_DATA:
                final ACLMessage m = new ACLMessage(MessageType.SEND);
                m.setActionID(lastMessage.getActionID());
                m.setReciever(lastMessage.getSender());
                m.setSender(getId());
                m.setContent(orient[0] + ":" + orient[1] + ":" + orient[2]);
                m.setValue(lsm.getTemperature());
                getController().getBlackboard().addMessage(m);
            default:
                break;
            }
        }
    }
    
    /**
     * Crude data filter.
     */
    private float[] correct(float[] orient) {
	float mse = MSE(last_orient, orient);
	if (mse > Constants.ORIENTATION_THRESHOLD) {
	    LOGGER.warn("Change in orientation too fast discarding bad value MSE: " + mse);
	    orient = last_orient;
	}
	return orient;
    }

    /**
     * Calculate the mean squared error between two vectors.
     * @param actual
     * @param predicted
     * @return
     */
    private float MSE(float[] actual, float[] predicted) {
	if (actual == null || predicted == null) {
	    return 0;
	}
	if (actual.length != predicted.length) {
	    return Float.POSITIVE_INFINITY;
	}
	int n = actual.length;
	int res = 0;
	for(int i = 0; i < n; i++) {
	    res += Math.pow(predicted[i] - actual[i], 2);
	}
	return (float) (res / n);
    }
    
    /**
     * Return the Roll Pitch and heading in degree's.
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
        heading = (float) Math.atan2((float)(magdata[2] * Math.sin(roll)) - magdata[1] * Math.cos(roll),
        	                             magdata[0] * Math.cos(pitch) + 
        	                             magdata[1] * Math.sin(pitch) * Math.sin(roll) + 
        	                             magdata[2] * Math.sin(pitch) * Math.cos(pitch));
        return new float[] {(float) Math.toDegrees(roll),
        	(float) Math.toDegrees(pitch),
        	(float) Math.toDegrees(heading)};
    }

    @Override
    protected void init() {
        if (Constants.SIMULATION) {
            LOGGER.debug("Initializing Gyro, Magnetometer and Accelerometer Device. (Simulated)");
            lsm = null;
        } else {
            LOGGER.debug("Initializing Gyro, Magnetometer and Accelerometer Device.");
            lsm = new upm_lsm9ds0.LSM9DS0(Constants.I2C_BUS_ID);
            lsm.init();
        }
    }

    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down Gyro, Magnetometer and Accelerometer Device.");
        lsm.delete();
        lsm = null;
    }

    @Override
    protected void alternate() {
	if(!Constants.SIMULATION) {
	    float[] rph = getRPH();
            LOGGER.debug("Yaw: " + rph[0] + " Pitch: "+ rph[1] + " Roll: "+ rph[2]);
            shutdown();
            init();
	} else {
            LOGGER.debug("Yaw: " + Math.random() * 180 + " Pitch: " + Math.random() * 180 + " Roll: " + Math.random() * 180);
	}
    }

    @Override
    public boolean selfTest() {
	init();
	if (lsm.getAccelerometer()[2] > 1) {
	    shutdown();
	    LOGGER.error("Vertical acceleration exceeds 9.8m/s^2 self test failed, please ensure aircraft is not in motion.");
	    return false;
	}
	shutdown();
        return true;
    }

}
