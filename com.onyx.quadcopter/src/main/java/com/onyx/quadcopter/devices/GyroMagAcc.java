package com.onyx.quadcopter.devices;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;
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

    private final byte[] timeval = new byte[16];
    private BigInteger time;
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
                m.setValue(time.doubleValue());
                getController().getBlackboard().addMessage(m);
            default:
                break;
            }
        }
    }

    /**
     * Get a snapshot of the X Y and Z data.
     */
    private void pollXYZ() {
        try {
            gyroStream.readFully(timeval);
            time = new BigInteger(timeval);
            gyroStream.readShort();
            gyroStream.readShort();
            gyrX = gyroStream.readShort();
            gyroStream.readShort();
            gyroStream.readShort();
            gyrY = gyroStream.readShort();
            gyroStream.readShort();
            gyroStream.readShort();
            gyrZ = gyroStream.readShort();

            accelStream.readFully(timeval);
            accelStream.readShort();
            accelStream.readShort();
            accX = accelStream.readShort();
            accelStream.readShort();
            accelStream.readShort();
            accY = accelStream.readShort();
            accelStream.readShort();
            accelStream.readShort();
            accZ = accelStream.readShort();

            magStream.readFully(timeval);
            magStream.readShort();
            magStream.readShort();
            magX = magStream.readShort();
            magStream.readShort();
            magStream.readShort();
            magY = magStream.readShort();
            magStream.readShort();
            magStream.readShort();
            magZ = magStream.readShort();
        } catch (final IOException e) {
            throw new OnyxException(e, LOGGER);
        }
    }

    @Override
    protected void init() {
        LOGGER.debug("Initializing Gyro, Magnetometer and Accelerometer Device.");
        try {
            gyroStream = new DataInputStream(new FileInputStream(Constants.GYRO_DEV_FILE));
            accelStream = new DataInputStream(new FileInputStream(Constants.ACC_DEV_FILE));
            magStream = new DataInputStream(new FileInputStream(Constants.MAG_DEV_FILE));
        } catch (final FileNotFoundException e) {
            throw new OnyxException(e, LOGGER);
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
            throw new OnyxException(e, LOGGER);
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