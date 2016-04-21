package com.onyx.quadcopter.devices;

import com.onyx.quadcopter.messaging.AclMessage;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * Blackboard message class.
 * All devices share this class to communicate over.
 * (Similar to a classroom blackboard. Each device takes a turn with the chalk, writes a message and
 * passes the chalk to the next device in sequence.)
 *
 * @author fred
 *
 */
public class Blackboard {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(Blackboard.class);

  private ConcurrentMap<DeviceId, PriorityBlockingQueue<AclMessage>> blackboard;

  public Blackboard() {
    blackboard = new ConcurrentHashMap<DeviceId, PriorityBlockingQueue<AclMessage>>(
        Constants.BLACKBOARD_SIZE);
  }

  /**
   * Periodically update this Blackboard.
   */
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
   *        the message to be added to this blackboard.
   */
  public synchronized void addMessage(final AclMessage aclMessage) {
    if (aclMessage.isValid()) {
      PriorityBlockingQueue<AclMessage> currentBucket = blackboard.get(aclMessage.getReciever());
      if (currentBucket == null) {
        currentBucket = new PriorityBlockingQueue<AclMessage>();
      }
      if (currentBucket.size() > Constants.MAX_BLACKBOARD_BUCKET_SIZE) {
        currentBucket.clear();
      }
      currentBucket.offer(aclMessage);
      blackboard.put(aclMessage.getReciever(), currentBucket);
    }
  }

  /**
   * Get a message for Device device. Searches the database for all messages which are destined to
   * device returning the first occurence.
   *
   * @param id the device id to find messages for
   *
   * @return the first ACLMessage found within the blackboard
   *
   */
  public synchronized AclMessage getMessage(DeviceId id) {
    PriorityBlockingQueue<AclMessage> currentBucket = blackboard.get(id);
    if (currentBucket == null) {
      currentBucket = new PriorityBlockingQueue<AclMessage>();
      blackboard.put(id, currentBucket);
    }
    final AclMessage n = blackboard.get(id).poll();
    if (n != null && n.isValid()) {
      return n;
    } else {
      return new AclMessage(MessageType.EMPTY);
    }
  }

  /**
   * Get a message for Device device. Searches the database for all messages which are destined to
   * device returning the first occurence.
   *
   * @param device the device to find messages for
   *
   * @return the first ACLMessage found within the blackboard
   *
   */
  public synchronized AclMessage getMessage(final Device device) {
    return getMessage(device.getId());
  }

  public void shutdown() {
    blackboard.clear();
    blackboard = null;
  }

}
