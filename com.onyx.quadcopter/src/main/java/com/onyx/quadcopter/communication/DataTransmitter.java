package com.onyx.quadcopter.communication;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.CommDevice;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Blackboard;
import com.onyx.quadcopter.utils.Constants;

public class DataTransmitter extends CommDevice implements MqttCallback {

    public DataTransmitter(final Controller c) throws OnyxException {
        super(c);
    }

    static final String BROKER_URL = "tcp://onyx.cnc.io:1883";
    static final String M2MIO_DOMAIN = "onyx";
    static final String M2MIO_STUFF = "onyx";
    static final String M2MIO_THING = "onyx";
    static final String M2MIO_USERNAME = "fred";
    static final String M2MIO_PASSWORD_MD5 = "";

    // the following two flags control whether this example is a publisher, a
    // subscriber or both
    static final Boolean subscriber = true;
    static final Boolean publisher = true;

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(DataTransmitter.class);

    /**
     * Connection was long, attempt reconnect.
     */
    @Override
    public void connectionLost(final Throwable t) {
        LOGGER.error("Connection Lost!");
        LOGGER.error(t.getMessage());
    }

    /**
     * Message delivered.
     */
    @Override
    public void deliveryComplete(final IMqttDeliveryToken token) {
        LOGGER.debug("Message Delivered.");
    }

    /**
     * New message has arrived, parse it.
     */
    @Override
    public void messageArrived(final String topic, final MqttMessage msg) throws Exception {
        LOGGER.debug(
                "New message arrived. On topic: " + topic + " {" + new String(msg.getPayload(), Constants.UTF8) + "}");
        // TODO Parse message and do something with it.
    }

    @Override
    protected void update(final Blackboard b) {
        // setup MQTT Client
        // String clientID = M2MIO_THING;
        final MqttConnectOptions connOpt = new MqttConnectOptions();

        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);
        connOpt.setUserName(M2MIO_USERNAME);
        connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
        MqttClient myClient = null;
        // Connect to Broker
        try {
            myClient = new MqttClient(BROKER_URL, M2MIO_THING);
            myClient.setCallback(this);
            myClient.connect(connOpt);
        } catch (final MqttException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }

        System.out.println("Connected to " + BROKER_URL);
        // setup topic
        // topics on m2m.io are in the form <domain>/<stuff>/<thing>
        final String myTopic = M2MIO_DOMAIN + "/" + M2MIO_STUFF + "/" + M2MIO_THING;
        final MqttTopic topic = myClient.getTopic(myTopic);
        // subscribe to topic if subscriber
        if (subscriber) {
            try {
                final int subQoS = 0;
                myClient.subscribe(myTopic, subQoS);
            } catch (final Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }

        // publish messages if publisher
        if (publisher) {
            for (int i = 1; i <= 10; i++) {
                final String pubMsg = "{\"pubmsg\":" + i + "}";
                final int pubQoS = 0;
                final MqttMessage message = new MqttMessage(pubMsg.getBytes());
                message.setQos(pubQoS);
                message.setRetained(false);

                // Publish the message
                System.out.println("Publishing to topic \"" + topic + "\" qos " + pubQoS);
                MqttDeliveryToken token = null;
                try {
                    // publish message to broker
                    token = topic.publish(message);
                    // Wait until the message has been delivered to the broker
                    token.waitForCompletion();
                    Thread.sleep(100);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // disconnect
        try {
            // wait to ensure subscribed messages are delivered
            if (subscriber) {
                Thread.sleep(5000);
            }
            myClient.disconnect();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void init() {
    }

    @Override
    protected void alternate() {
    }

    @Override
    public boolean selfTest() {
        return false;
    }

    @Override
    public void shutdown() {

    }
}
