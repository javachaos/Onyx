package com.onyx.quadcopter.devices;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.Constants;

public class GyroMagAcc extends Device {

    /**
     * Input Device streams.
     */
    private DataInputStream gyroStream;
    private DataInputStream accelStream;
    private DataInputStream magStream;

    private final byte[] timeval = new byte[24];
    private short gyrX, gyrY, gyrZ;
    private short accX, accY, accZ;
    private short magX, magY, magZ;
    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(GyroMagAcc.class);

    public GyroMagAcc(final Controller c) {
        super(c, DeviceID.GYRO_MAG_ACC);
    }

    @Override
    protected void update() {
        if (isNewMessage()) {
            switch (lastMessage.getActionID()) {
            case GET_ORIENT:
            case SEND_DATA:
                pollXYZ();
                final ACLMessage m = new ACLMessage(MessageType.SEND);
                m.setActionID(lastMessage.getActionID());
                m.setReciever(lastMessage.getSender());
                m.setSender(getId());
                m.setContent(gyrX + ":" + gyrY + ":" + gyrZ + ";" + accX + ":" + accY + ":" + accZ + ";" + magX + ":"
                        + magY + ":" + magZ);
                getController().getBlackboard().addMessage(m);
            default:
                break;
            }
        }
    }

    /**
     * Get a snapshot of the X Y and Z data.
     */
    private synchronized void pollXYZ() {
        try {
            gyroStream.read(timeval);
            gyroStream.readShort();
            gyroStream.readShort();
            gyrX = (short) gyroStream.readInt();
            gyroStream.readShort();
            gyroStream.readShort();
            gyrY = (short) gyroStream.readInt();
            gyroStream.readShort();
            gyroStream.readShort();
            gyrZ = (short) gyroStream.readInt();

            accelStream.read(timeval);
            accelStream.readShort();
            accelStream.readShort();
            accX = (short) accelStream.readInt();
            accelStream.readShort();
            accelStream.readShort();
            accY = (short) accelStream.readInt();
            accelStream.readShort();
            accelStream.readShort();
            accZ = (short) accelStream.readInt();

            magStream.read(timeval);
            magStream.readShort();
            magStream.readShort();
            magX = (short) magStream.readInt();
            magStream.readShort();
            magStream.readShort();
            magY = (short) magStream.readInt();
            magStream.readShort();
            magStream.readShort();
            magZ = (short) magStream.readInt();
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void init() {
        LOGGER.debug("Initializing Gyro, Magnetometer and Accelerometer Device.");
        if (Constants.SIMULATION) {
            try {
                gyroStream = new DataInputStream(new FileInputStream(Constants.URANDOM));
                accelStream = new DataInputStream(new FileInputStream(Constants.URANDOM));
                magStream = new DataInputStream(new FileInputStream(Constants.URANDOM));
            } catch (final FileNotFoundException e) {
                LOGGER.error(e.getMessage());
            }
        } else {
            try {
                gyroStream = new DataInputStream(new FileInputStream(Constants.GYRO_DEV_FILE));
                accelStream = new DataInputStream(new FileInputStream(Constants.ACC_DEV_FILE));
                magStream = new DataInputStream(new FileInputStream(Constants.MAG_DEV_FILE));
            } catch (final FileNotFoundException e) {
                LOGGER.error(e.getMessage());
            }
        }

    }

    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down Gyro, Magnetometer and Accelerometer Device.");
        try {
            gyroStream.close();
            accelStream.close();
            magStream.close();
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void alternate() {
        pollXYZ();
        LOGGER.debug(gyrX + ":" + gyrY + ":" + gyrZ + ";" + accX + ":" + accY + ":" + accZ + ";" + magX + ":" + magY
                + ":" + magZ);
    }

    @Override
    public boolean selfTest() {
        return true;// TODO Complete Gyro self test
    }

}
