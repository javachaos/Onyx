package com.onyx.quadcopter.communication;

import java.sql.Timestamp;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.utils.Constants;

public class CommAsyncCallback implements MqttCallback {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(CommAsyncCallback.class);

    static final int BEGIN = 0;
    static final int CONNECTED = 1;
    static final int PUBLISHED = 2;
    static final int SUBSCRIBED = 3;
    static final int DISCONNECTED = 4;
    static final int FINISH = 5;
    static final int ERROR = 6;
    static final int DISCONNECT = 7;

    int state = BEGIN;

    // Private instance variables
    MqttAsyncClient client;
    String brokerUrl;
    private final boolean quietMode;
    private MqttConnectOptions conOpt;
    private final boolean clean;
    Throwable ex = null;
    Object waiter = new Object();
    boolean donext = false;
    private final String password;
    private final String userName;

    /**
     * Constructs an instance of this class
     *
     * @param brokerUrl
     *            the url to connect to
     * @param clientId
     *            the client id to connect with
     * @param cleanSession
     *            clear state at end of connection or not (durable or
     *            non-durable subscriptions)
     * @param quietMode
     *            whether debug should be printed to standard out
     * @param userName
     *            the username to connect with
     * @param password
     *            the password for the user
     * @throws MqttException
     */
    public CommAsyncCallback(final String brokerUrl, final String clientId, final boolean cleanSession,
            final boolean quietMode, final String userName, final String password) throws MqttException {
        this.brokerUrl = brokerUrl;
        this.quietMode = quietMode;
        clean = cleanSession;
        this.password = password;
        this.userName = userName;
        final String tmpDir = Constants.APPLICATION_DIR + "\tmp";
        final MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        try {
            conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(clean);
            if (password != null) {
                conOpt.setPassword(this.password.toCharArray());
            }
            if (userName != null) {
                conOpt.setUserName(this.userName);
            }

            // Construct the MqttClient instance
            client = new MqttAsyncClient(this.brokerUrl, clientId, dataStore);

            // Set this wrapper as the callback handler
            client.setCallback(this);

        } catch (final MqttException e) {
            e.printStackTrace();
            log("Unable to set up client: " + e.toString());
        }
    }

    /**
     * Publish / send a message to an MQTT server
     *
     * @param topicName
     *            the name of the topic to publish to
     * @param qos
     *            the quality of service to delivery the message at (0,1,2)
     * @param payload
     *            the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public void publish(final String topicName, final int qos, final byte[] payload) throws Throwable {
        // Use a state machine to decide which step to do next. State change
        // occurs
        // when a notification is received that an MQTT action has completed
        while (state != FINISH) {
            switch (state) {
            case BEGIN:
                // Connect using a non-blocking connect
                final MqttConnector con = new MqttConnector();
                con.doConnect();
                break;
            case CONNECTED:
                // Publish using a non-blocking publisher
                final Publisher pub = new Publisher();
                pub.doPublish(topicName, qos, payload);
                break;
            case PUBLISHED:
                state = DISCONNECT;
                donext = true;
                break;
            case DISCONNECT:
                final Disconnector disc = new Disconnector();
                disc.doDisconnect();
                break;
            case ERROR:
                throw ex;
            case DISCONNECTED:
                state = FINISH;
                donext = true;
                break;
            }

            waitForStateChange(10000);
        }
    }

    /**
     * Wait for a maximum amount of time for a state change event to occur
     *
     * @param maxTTW
     *            maximum time to wait in milliseconds
     * @throws MqttException
     */
    private void waitForStateChange(final int maxTTW) throws MqttException {
        synchronized (waiter) {
            if (!donext) {
                try {
                    waiter.wait(maxTTW);
                } catch (final InterruptedException e) {
                    log("timed out");
                    e.printStackTrace();
                }

                if (ex != null) {
                    throw (MqttException) ex;
                }
            }
            donext = false;
        }
    }

    /**
     * Subscribe to a topic on an MQTT server Once subscribed this method waits
     * for the messages to arrive from the server that match the subscription.
     * It continues listening for messages until the enter key is pressed.
     *
     * @param topicName
     *            to subscribe to (can be wild carded)
     * @param qos
     *            the maximum quality of service to receive messages at for this
     *            subscription
     * @throws MqttException
     */
    public void subscribe(final String topicName, final int qos) throws Throwable {
        while (state != FINISH) {
            switch (state) {
            case BEGIN:
                // Connect using a non-blocking connect
                final MqttConnector con = new MqttConnector();
                con.doConnect();
                break;
            case CONNECTED:
                // Subscribe using a non-blocking subscribe
                final Subscriber sub = new Subscriber();
                sub.doSubscribe(topicName, qos);
                break;
            case SUBSCRIBED:
                Thread.sleep(1);
                state = DISCONNECTED;
                donext = true;
                break;
            case DISCONNECT:
                final Disconnector disc = new Disconnector();
                disc.doDisconnect();
                break;
            case ERROR:
                throw ex;
            case DISCONNECTED:
                state = FINISH;
                donext = true;
                break;
            }

            waitForStateChange(Constants.STATE_CHANGE_TIMEOUT);
        }
    }

    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does
     * nothing
     *
     * @param message
     *            the message to log
     */
    void log(final String message) {
        if (!quietMode) {
            LOGGER.debug(message);
        }
    }

    /****************************************************************/
    /* Methods to implement the MqttCallback interface */
    /****************************************************************/

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    @Override
    public void connectionLost(final Throwable cause) {
        // Called when the connection to the server has been lost.
        // An application may choose to implement reconnection
        // logic at this point. This sample simply exits.
        log("Connection to " + brokerUrl + " lost!" + cause);
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(final IMqttDeliveryToken token) {
        // Called when a message has been delivered to the
        // server. The token passed in here is the same one
        // that was returned from the original call to publish.
        // This allows applications to perform asynchronous
        // delivery without blocking until delivery completes.
        //
        // This sample demonstrates asynchronous deliver, registering
        // a callback to be notified on each call to publish.
        //
        // The deliveryComplete method will also be called if
        // the callback is set on the client
        //
        // note that token.getTopics() returns an array so we convert to a
        // string
        // before printing it on the console
        log("Delivery complete callback: Publish Completed " + Arrays.toString(token.getTopics()));
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    @Override
    public void messageArrived(final String topic, final MqttMessage message) throws MqttException {
        // Called when a message arrives from the server that matches any
        // subscription made by the client
        final String time = new Timestamp(System.currentTimeMillis()).toString();
        log("Time:\t" + time + "  Topic:\t" + topic + "  Message:\t" + new String(message.getPayload()) + "  QoS:\t"
                + message.getQos());
    }

    /****************************************************************/
    /* End of MqttCallback methods */
    /****************************************************************/

    /**
     * Connect in a non-blocking way and then sit back and wait to be notified
     * that the action has completed.
     */
    public class MqttConnector {

        public MqttConnector() {
        }

        public void doConnect() {
            log("Connecting to " + brokerUrl + " with client ID " + client.getClientId());

            final IMqttActionListener conListener = new IMqttActionListener() {
                @Override
                public void onSuccess(final IMqttToken asyncActionToken) {
                    log("Connected");
                    state = CONNECTED;
                    carryOn();
                }

                @Override
                public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    log("connect failed" + exception);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (waiter) {
                        donext = true;
                        waiter.notifyAll();
                    }
                }
            };

            try {
                // Connect using a non-blocking connect
                client.connect(conOpt, "Connect to Commander", conListener);
            } catch (final MqttException e) {
                // If though it is a non-blocking connect an exception can be
                // thrown if validation of parms fails or other checks such
                // as already connected fail.
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    /**
     * Publish in a non-blocking way and then sit back and wait to be notified
     * that the action has completed.
     */
    public class Publisher {
        public void doPublish(final String topicName, final int qos, final byte[] payload) {
            // Send / publish a message to the server
            // Get a token and setup an asynchronous listener on the token which
            // will be notified once the message has been delivered
            final MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);

            final String time = new Timestamp(System.currentTimeMillis()).toString();
            log("Publishing at: " + time + " to topic \"" + topicName + "\" qos " + qos);

            // Setup a listener object to be notified when the publish
            // completes.
            //
            final IMqttActionListener pubListener = new IMqttActionListener() {
                @Override
                public void onSuccess(final IMqttToken asyncActionToken) {
                    log("Publish Completed");
                    state = PUBLISHED;
                    carryOn();
                }

                @Override
                public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    log("Publish failed" + exception);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (waiter) {
                        donext = true;
                        waiter.notifyAll();
                    }
                }
            };

            try {
                // Publish the message
                client.publish(topicName, message, "Publish to Commander", pubListener);
            } catch (final MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    /**
     * Subscribe in a non-blocking way and then sit back and wait to be notified
     * that the action has completed.
     */
    public class Subscriber {
        public void doSubscribe(final String topicName, final int qos) {
            log("Subscribing to topic \"" + topicName + "\" qos " + qos);

            final IMqttActionListener subListener = new IMqttActionListener() {
                @Override
                public void onSuccess(final IMqttToken asyncActionToken) {
                    log("Subscribe Completed");
                    state = SUBSCRIBED;
                    carryOn();
                }

                @Override
                public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    log("Subscribe failed" + exception);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (waiter) {
                        donext = true;
                        waiter.notifyAll();
                    }
                }
            };

            try {
                client.subscribe(topicName, qos, "Subscribe to Commander context", subListener);
            } catch (final MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    /**
     * Disconnect in a non-blocking way and then sit back and wait to be
     * notified that the action has completed.
     */
    public class Disconnector {
        public void doDisconnect() {
            // Disconnect the client
            log("Disconnecting");

            final IMqttActionListener discListener = new IMqttActionListener() {
                @Override
                public void onSuccess(final IMqttToken asyncActionToken) {
                    log("Disconnect Completed");
                    state = DISCONNECTED;
                    carryOn();
                }

                @Override
                public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    log("Disconnect failed" + exception);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (waiter) {
                        donext = true;
                        waiter.notifyAll();
                    }
                }
            };

            try {
                client.disconnect("Disconnect onyx context", discListener);
            } catch (final MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

}
