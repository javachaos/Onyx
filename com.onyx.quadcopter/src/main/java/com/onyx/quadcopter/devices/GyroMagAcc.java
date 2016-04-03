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
    private short gyrX, gyrY, gyrZ;
    private short accX, accY, accZ;
    private short magX, magY, magZ;

    public GyroMagAcc(final Controller c) {
        super(c, DeviceID.GYRO_MAG_ACC);
    }

    @Override
    protected void update() {
        lsm.update();
        if (isNewMessage()) {
            switch (lastMessage.getActionID()) {
            case GET_ORIENT:
            case SEND_DATA:
                final ACLMessage m = new ACLMessage(MessageType.SEND);
                m.setActionID(lastMessage.getActionID());
                m.setReciever(lastMessage.getSender());
                m.setSender(getId());
                m.setContent(lsm.getAccelerometer() + ";" + lsm.getGyroscope() + ";" + lsm.getMagnetometer());
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
    }

    @Override
    protected void alternate() {
        LOGGER.debug(gyrX + ":" + gyrY + ":" + gyrZ + ";" + accX + ":" + accY + ":" + accZ + ";" + magX + ":" + magY
                + ":" + magZ);
    }

    @Override
    public boolean selfTest() {
        return true;// TODO Complete Gyro self test
    }

}
