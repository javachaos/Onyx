package com.onyx.quadcopter.devices;

import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ACLPriority;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.Constants;

public abstract class Device implements Executable {

    /**
     * Logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(Device.class);

    /**
     * Controller reference.
     */
    private final Controller controller;

    /**
     * Keep track of the number of loops.
     */
    private int runCounter = 0;

    private DeviceID id;
    /**
     * True when the device has been initialized.
     */
    protected volatile boolean initialized = false;

    /**
     * The last message posted to the black board for this agent.
     */
    protected volatile ACLMessage lastMessage, previousMessage;

    /**
     * The human readable name for this device.
     */
    private String name;

    /**
     * Stack of ACL Messages.
     */
    private PriorityBlockingQueue<ACLMessage> messages = new PriorityBlockingQueue<ACLMessage>();

    /**
     * Create a new device.
     * 
     * @param c
     * @param id
     */
    public Device(final Controller c, final DeviceID id) {
	setId(id);
	setName(id.toString());
	if (c != null) {
	    controller = c;
	} else {
	    LOGGER.error("Device could not be constructed, controller null.");
	    throw new OnyxException("Device could not be constructed, controller null.", LOGGER);
	}
    }

    /**
     * Set the name of this device.
     * 
     * @param n
     */
    private void setName(final String n) {
	if ((n != null) && (n.length() > 0)) {
	    name = n;
	}
    }

    /**
     * Update this device given a reference to the blackboard.
     *
     * @param b
     *            Reference to the blackboard.
     *
     */
    protected abstract void update();

    @Override
    public synchronized void execute() {
	gatherMessages();
	update();
	runCounter++;
	if (runCounter == Constants.ALTERNATE_SPEED) {
	    runCounter = 0;
	    LOGGER.debug("Device heartbeat: " + getName() + ".");
	    alternate();
	}
    }
    
    /**
     * Accumulate all messages from the blackboard for this device.
     */
    private void gatherMessages() {
	ACLMessage m = getController().getBlackboard().getMessage(this);
	previousMessage = lastMessage;
	if (previousMessage == null)
	    previousMessage = m;
	lastMessage = m;
	while(m != null && m.isValid()) {
	    messages.offer(m);
	    m = getController().getBlackboard().getMessage(this);
	}
	if (messages.size() > Constants.MAX_BLACKBOARD_BUCKET_SIZE) {
	    messages.clear();
	}
    }
    
    /**
     * Get the messages for this device.
     * @return
     */
    public PriorityBlockingQueue<ACLMessage> getMessages() {
	return messages;
    }

    /**
     * Return true if there is a new message.
     * 
     * @return
     */
    protected boolean isNewMessage() {
	if ((previousMessage == null) && (lastMessage instanceof ACLMessage)) {
	    return lastMessage.isValid() && lastMessage.getReciever() == getId();
	}
	return !previousMessage.equals(lastMessage) && lastMessage.isValid();
    }

    /**
     * Return the most recent ACL message.
     *
     * @return the most recent ACL message.
     */
    public ACLMessage getLastACLMessage() {
	return lastMessage;
    }

    /**
     * True if device init() method has been called at least once.
     *
     * @return if the init() method has been called at least once.
     */
    public boolean isInitialized() {
	return initialized;
    }

    /**
     * Initialize the device.
     */
    protected abstract void init();

    /**
     * Shutdown this device.
     */
    public abstract void shutdown();

    /**
     * Called very ALTERNATE_SPEED, updates. Sort of like a slow update.
     */
    protected abstract void alternate();

    /**
     * Run self test code to ensure everything works.
     * 
     * Note: Only called during live tests. if Constants.SIMULATION is set to
     * true this will not be run.
     *
     * @return true if everything is OK
     */
    public abstract boolean selfTest();

    /**
     * Return the device ID.
     *
     * @return
     */
    public DeviceID getId() {
	return id;
    }

    /**
     * Send a message to receiver.
     * 
     * @param receiver
     *            the message recipient
     * @param content
     *            the contents of the message
     * @param action
     *            the actionId
     */
    protected void sendMessage(final MessageType type, final DeviceID receiver, final String content,
	    final double value, final ActionId action, final ACLPriority priority) {
	final ACLMessage m = new ACLMessage(type);
	m.setActionID(action);
	m.setContent(content);
	m.setReciever(receiver);
	m.setSender(getId());
	m.setValue(value);
	m.setPriority(priority);
	getController().getBlackboard().addMessage(m);
    }

    /**
     * Send a message to receiver.
     * 
     * @param receiver
     *            the message recipient
     * @param content
     *            the contents of the message
     * @param action
     *            the actionId
     */
    protected void sendMessage(final DeviceID receiver, final String content, final double value,
	    final ActionId action, final ACLPriority priority) {
	sendMessage(MessageType.SEND, receiver, content, value, action, priority);
    }

    /**
     * Send a message to receiver.
     * 
     * @param receiver
     *            the message recipient
     * @param content
     *            the contents of the message
     * @param action
     *            the actionId
     */
    protected void sendMessage(final DeviceID receiver, final String content, final ActionId action, final ACLPriority priority) {
	sendMessage(receiver, content, 0.0, action, priority);
    }
    
    /**
     * Send a message to receiver. (ACLPriority.MEDIUM)
     * 
     * @param receiver
     *            the message recipient
     * @param content
     *            the contents of the message
     * @param action
     *            the actionId
     */
    protected void sendMessage(final DeviceID receiver, final String content, final ActionId action) {
	sendMessage(receiver, content, 0.0, action, ACLPriority.MEDIUM);
    }

    /**
     * Send a message to receiver. (ACLPriority.HIGH)
     * 
     * @param receiver
     *            the message recipient
     * @param content
     *            the contents of the message
     * @param action
     *            the actionId
     */
    protected void sendMessageHigh(final DeviceID receiver, final String content, final ActionId action) {
	sendMessage(receiver, content, 0.0, action, ACLPriority.HIGH);
    }

    /**
     * Send a message to receiver. (ACLPriority.LOW)
     * 
     * @param receiver
     *            the message recipient
     * @param content
     *            the contents of the message
     * @param action
     *            the actionId
     */
    protected void sendMessageLow(final DeviceID receiver, final String content, final ActionId action) {
	sendMessage(receiver, content, 0.0, action, ACLPriority.LOW);
    }

    
    /**
     * Send a reply to the last sender.
     * 
     * @param content
     * @param value
     * @param action
     */
    protected void sendReply(final String content, final double value, final ActionId action, final ACLPriority priority) {
	sendMessage(MessageType.REPLY, lastMessage.getSender(), content, value, action, priority);
    }

    /**
     * Send a reply to the last sender.
     * 
     * @param content
     * @param action
     */
    protected void sendReply(final String content, final ActionId action, final ACLPriority priority) {
	sendMessage(MessageType.REPLY, lastMessage.getSender(), content, 0.0, action, priority);
    }

    /**
     * Send a reply to the last sender.
     * 
     * @param content
     * @param action
     */
    protected void sendReply(final String content) {
	sendMessage(MessageType.REPLY, lastMessage.getSender(), content, 0.0, lastMessage.getActionID(), lastMessage.getPriority());
    }

    /**
     * Send a reply to the last sender.
     * 
     * @param content
     * @param value
     * @param action
     */
    protected void sendReply(final String content, final double value) {
	sendMessage(MessageType.REPLY, lastMessage.getSender(), content, value, lastMessage.getActionID(), lastMessage.getPriority());
    }

    /**
     * Set the device id.
     *
     * @param id
     */
    private void setId(final DeviceID id) {
	this.id = id;
    }

    @Override
    public String toString() {
	return getName();
    }

    /**
     * Get the name of this device.
     * 
     * @return
     */
    private String getName() {
	return name;
    }

    /**
     * Get a reference to the controller.
     * 
     * @return
     */
    protected Controller getController() {
	return controller;
    }

    /**
     * Initialize this device.
     */
    public void initialize() {
	if (!isInitialized()) {
	    LOGGER.debug("Initializing " + getName());
	    init();
	    LOGGER.debug("Device: " + getName() + " initialized.");
	    initialized = true;
	}
    }
}
