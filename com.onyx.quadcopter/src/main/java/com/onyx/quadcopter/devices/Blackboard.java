package com.onyx.quadcopter.devices;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.Constants;

/**
 * Blackboard message class.
 *
 * All devices share this class to communicate over.
 *
 * (Similar to a classroom blackboard. Each device takes a turn with the chalk,
 * writes a message and passes the chalk to the next device in sequence.)
 *
 * @author fred
 *
 */
public class Blackboard extends Device {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Blackboard.class);

    private ConcurrentMap<DeviceID, ACLMessage> blackboard;

    public Blackboard(final Controller c) {
        super(c, DeviceID.BLACKBOARD);
        blackboard = new ConcurrentHashMap<DeviceID, ACLMessage>(Constants.BLACKBOARD_SIZE);
    }

    @Override
    protected void update() {
    }

    @Override
    protected synchronized void init() {
    }

    @Override
    protected void alternate() {
        if (blackboard.size() >= Constants.BLACKBOARD_SIZE) {
            LOGGER.debug("Clearing blackboard.");
            blackboard.clear();
        }
    }

    @Override
    public boolean selfTest() {
        LOGGER.debug("Running blackboard self test.");
        return true; // TODO implement
    }

    /**
     * Add a message to the blackboard.
     *
     * @param aclMessage
     */
    public synchronized void addMessage(final ACLMessage aclMessage) {
        if (aclMessage.isValid()) {
            blackboard.put(aclMessage.getSender(), aclMessage);
        }
    }

    /**
     * Get a message for Device device. Searches the database for all messages
     * which are destined to device returning the first occurence.
     *
     * @param device
     *            the device to find messages for
     *
     * @return the first ACLMessage found within the blackboard
     *
     */
    public synchronized ACLMessage getMessage(final Device device) {
        final ACLMessage n = blackboard.get(device.getId());
        if (n != null) {
            return n;
        }
        return new ACLMessage(MessageType.EMPTY);
    }

    @Override
    public void shutdown() {
        blackboard.clear();
        blackboard = null;
    }
}
