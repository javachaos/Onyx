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


    /**
     * AHRS algorithm implementation.
     */
    private AHRS ahrs;
    private upm_lsm9ds0.LSM9DS0 lsm;

    public GyroMagAcc(final Controller c) {
        super(c, DeviceID.GYRO_MAG_ACC);
        ahrs = new AHRS();
    }

    @Override
    protected void update() {
        lsm.update();
	float[] gyrodata = lsm.getGyroscope();
	float[] acceldata = lsm.getAccelerometer();
	float[] magdata = lsm.getMagnetometer();
        ahrs.update(gyrodata[0], gyrodata[1], gyrodata[2],
        	acceldata[0], acceldata[1], acceldata[2],
        	magdata[0], magdata[1], magdata[2]);
        if (isNewMessage()) {
            switch (lastMessage.getActionID()) {
            case GET_ORIENT:
            case SEND_DATA:
                final ACLMessage m = new ACLMessage(MessageType.SEND);
                m.setActionID(lastMessage.getActionID());
                m.setReciever(lastMessage.getSender());
                m.setSender(getId());
                Quaternion q = ahrs.getQuaternion();
                m.setContent(q.getQ0() + ":" + q.getQ1() + ":" + q.getQ2() + ":" 
                	+ q.getQ3());
                m.setValue(lsm.getTemperature());
                getController().getBlackboard().addMessage(m);
            default:
                break;
            }
        }
        
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
        Quaternion q = ahrs.getQuaternion();
        float x,y,z,w;
        w = q.getQ0();
        x = q.getQ1();
        y = q.getQ2();
        z = q.getQ3();
        
        LOGGER.debug(q.getQ0() + ":" + q.getQ1() + ":" + q.getQ2() + ":" 
        	+ q.getQ3());
        float roll  = (float) Math.atan2(2*y*w - 2*x*z, 1 - 2*y*y - 2*z*z);
        float pitch = (float) Math.atan2(2*x*w - 2*y*z, 1 - 2*x*x - 2*z*z);
        float yaw   = (float) Math.asin(2*x*y + 2*z*w);
        LOGGER.debug("Yaw: " + yaw + "Pitch: "+pitch+ "Roll: "+ roll);
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
