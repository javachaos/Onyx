package com.onyx.quadcopter.devices;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Blackboard {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Blackboard.class);

    private ConcurrentMap<DeviceID, PriorityBlockingQueue<ACLMessage>> blackboard;

    public Blackboard() {
	blackboard = new ConcurrentHashMap<DeviceID, PriorityBlockingQueue<ACLMessage>>(Constants.BLACKBOARD_SIZE);
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
	    PriorityBlockingQueue<ACLMessage> currentBucket = blackboard.get(aclMessage.getReciever());
	    if (currentBucket == null) {
		currentBucket = new PriorityBlockingQueue<ACLMessage>();
	    }
	    if (currentBucket.size() > Constants.MAX_BLACKBOARD_BUCKET_SIZE) {
		currentBucket.clear();
	    }
	    currentBucket.offer(aclMessage);
	    blackboard.put(aclMessage.getReciever(), currentBucket);
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
	PriorityBlockingQueue<ACLMessage> currentBucket = blackboard.get(id);
	if (currentBucket == null) {
	    currentBucket = new PriorityBlockingQueue<ACLMessage>();
	    blackboard.put(id, currentBucket);
	}
	final ACLMessage n = blackboard.get(id).poll();
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
