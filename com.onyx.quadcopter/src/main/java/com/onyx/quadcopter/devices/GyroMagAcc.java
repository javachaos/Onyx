package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.AHRS;
import com.onyx.quadcopter.utils.AHRS.Quaternion;
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
	float[] gyrodata = lsm.getGyroscope();
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
//        float gyrodata[0], gyrodata[1], gyrodata[2],
//        	acceldata[0], ,
//        	magdata[0], magdata[1], magdata[2]);
        if (isNewMessage()) {
            switch (lastMessage.getActionID()) {
            case GET_ORIENT:
            case SEND_DATA:
                final ACLMessage m = new ACLMessage(MessageType.SEND);
                m.setActionID(lastMessage.getActionID());
                m.setReciever(lastMessage.getSender());
                m.setSender(getId());
                m.setContent(roll + ":" + pitch + ":" + heading);
                m.setValue(lsm.getTemperature());
                getController().getBlackboard().addMessage(m);
            default:
                break;
            }
        }
        
    }
    
    private float[] getRPH() {
	//float[] gyrodata = lsm.getGyroscope();
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
        return new float[] {roll, pitch, heading};
    }

    @Override
    protected void init() {
        if (Constants.SIMULATION) {
            LOGGER.debug("Initializing Gyro, Magnetometer and Accelerometer Device. (Simulated)");
            lsm = new SIMLSM9DS0();
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
	float[] rph = getRPH();
        LOGGER.debug("Yaw: " + Math.toDegrees(rph[0]) + " Pitch: "+Math.toDegrees(rph[1])+ " Roll: "+ Math.toDegrees(rph[2]));
        shutdown();
        init();
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
