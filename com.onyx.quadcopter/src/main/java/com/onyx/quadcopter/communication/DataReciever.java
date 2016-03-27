package com.onyx.quadcopter.communication;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.onyx.quadcopter.devices.CommDevice;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Blackboard;
import com.onyx.quadcopter.utils.Constants;

public class DataReciever extends CommDevice implements MqttCallback {

    /**
     * Connection options.
     */
    private MqttConnectOptions connOpt;

    public DataReciever(final Controller c) throws OnyxException {
        super(c);
    }

    @Override
    protected void update(final Blackboard b) {
        MqttClient client = null;
        // Connect to Broker
        try {
            client = new MqttClient(Constants.BROKER_URL, Constants.MQTT_THING);
            client.setCallback(this);
            client.connect(connOpt);
        } catch (final MqttException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        try {
            final int subQoS = 2;
            final String topic = "RECV";
            client.subscribe(topic, subQoS);
        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void init() {
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(Constants.MQTT_KEEPALIVE);
        connOpt.setUserName(Constants.MQTT_USERNAME);
        connOpt.setPassword(Constants.MQTT_PASSWORD_MD5.toCharArray());
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void alternate() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean selfTest() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void connectionLost(final Throwable arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(final String arg0, final MqttMessage arg1) throws Exception {
        // TODO Auto-generated method stub

    }

}
