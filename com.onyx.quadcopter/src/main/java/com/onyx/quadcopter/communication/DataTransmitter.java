package com.onyx.quadcopter.communication;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.Constants;

public class DataTransmitter extends Device {

    private CommAsyncCallback callback;
    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(DataTransmitter.class);

    public DataTransmitter(final Controller c) throws OnyxException {
        super(c, DeviceID.DATA_TRANSMITTER);
    }

    @Override
    protected void update() {
        final ACLMessage msg = getController().getBlackboard().getMessage(this);
        try {
            if (!msg.isEmpty() && msg.isValid()) {
                callback.publish(msg.getMessageType().toString(), Constants.TRANSMIT_QOS, msg.getContent().getBytes());
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void init() {
        try {
            callback = new CommAsyncCallback(Constants.BROKER_URL, Constants.MQTT_THING, true, Constants.MQTT_QUIET,
                    Constants.MQTT_USERNAME, Constants.MQTT_PASSWORD_MD5);
        } catch (final MqttException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void alternate() {
    }

    @Override
    public boolean selfTest() {
        return true;// TODO Complete DataTransmit selfTest
    }

    @Override
    public void shutdown() {
    }
}
