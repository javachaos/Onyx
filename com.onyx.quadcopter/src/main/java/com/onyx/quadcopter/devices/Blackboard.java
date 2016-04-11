package com.onyx.quadcopter.devices;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.ConcurrentStack;
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
public class Blackboard {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Blackboard.class);

    private ConcurrentMap<DeviceID, ConcurrentStack<ACLMessage>> blackboard;

    public Blackboard() {
	blackboard = new ConcurrentHashMap<DeviceID, ConcurrentStack<ACLMessage>>(Constants.BLACKBOARD_SIZE);
    }

    public void update() {
	if (blackboard.size() >= Constants.BLACKBOARD_SIZE) {
	    LOGGER.debug("Clearing blackboard.");
	    blackboard.clear();
	}
    }

    /**
     * Add a message to the blackboard.
     *
     * @param aclMessage
     */
    public synchronized void addMessage(final ACLMessage aclMessage) {
	if (aclMessage.isValid()) {
	    if (blackboard.get(aclMessage.getReciever()) == null) {
		blackboard.put(aclMessage.getReciever(), new ConcurrentStack<ACLMessage>());
	    }
	    blackboard.get(aclMessage.getReciever()).push(aclMessage);
	}
    }

    /**
     * Get a message for Device device. Searches the database for all messages
     * which are destined to device returning the first occurence.
     *
     * @param id
     *            the device id to find messages for
     *
     * @return the first ACLMessage found within the blackboard
     *
     */
    public synchronized ACLMessage getMessage(DeviceID id) {
	final ACLMessage n = blackboard.get(id).pop();
	if (n != null && n.isValid()) {
	    return n;
	} else {
	    return new ACLMessage(MessageType.EMPTY);
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
	return getMessage(device.getId());
    }

    public void shutdown() {
	blackboard.clear();
	blackboard = null;
    }

}
