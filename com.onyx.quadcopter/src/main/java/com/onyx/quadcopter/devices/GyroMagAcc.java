package com.onyx.quadcopter.devices;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.evdev.EventDevice;
import com.onyx.quadcopter.evdev.InputEvent;
import com.onyx.quadcopter.evdev.InputListener;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.Constants;

public class GyroMagAcc extends Device implements InputListener {

    /**
     * Input Device streams.
     */
    private EventDevice gyroDev;
    private EventDevice accelDev;
    private EventDevice magDev;

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

    @Override
    protected void init() {
        LOGGER.debug("Initializing Gyro, Magnetometer and Accelerometer Device.");
        if (Constants.SIMULATION) {
            try {
                gyroDev = new EventDevice(Constants.GYRO_DEV_FILE);
                gyroDev.addListener(this);
                accelDev = new EventDevice(Constants.GYRO_DEV_FILE);
                accelDev.addListener(this);
                magDev = new EventDevice(Constants.GYRO_DEV_FILE);
                magDev.addListener(this);
            } catch (final IOException e) {
                LOGGER.error(e.getMessage());
            }
        } else {
            try {
                gyroDev = new EventDevice(Constants.URANDOM);
                gyroDev.addListener(this);
                accelDev = new EventDevice(Constants.URANDOM);
                accelDev.addListener(this);
                magDev = new EventDevice(Constants.URANDOM);
                magDev.addListener(this);
            } catch (final IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

    }

    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down Gyro, Magnetometer and Accelerometer Device.");
        gyroDev.close();
        accelDev.close();
        magDev.close();
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

    @Override
    public void event(final InputEvent e) {
        switch (e.type) {
        case InputEvent.ABS_X:
            gyrX = (short) e.value;
            break;
        case InputEvent.ABS_Y:
            gyrY = (short) e.value;
            break;
        case InputEvent.ABS_Z:
            gyrZ = (short) e.value;
            break;
        }
    }

}
