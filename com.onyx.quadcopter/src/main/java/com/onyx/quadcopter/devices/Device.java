package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;
import com.onyx.common.utils.Constants;
import com.onyx.quadcopter.main.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;


public abstract class Device implements IDevice {

  /**
   * Logger.
   */
  protected static final Logger LOGGER = LoggerFactory.getLogger(Device.class);

  /**
   * Controller reference.
   */
  private Controller controller;

  /**
   * Keep track of the number of loops.
   */
  private int runCounter = 0;

  private DeviceId id;
  /**
   * True when the device has been initialized.
   */
  protected volatile boolean initialized = false;

  /**
   * The last message posted to the black board for this agent.
   */
  protected volatile AclMessage lastMessage;
  protected volatile AclMessage previousMessage;

  /**
   * The human readable name for this device.
   */
  private String name;

  /**
   * Stack of ACL Messages.
   */
  private PriorityBlockingQueue<AclMessage> messages = new PriorityBlockingQueue<AclMessage>();

  /**
   * Create a new Device.
   * @param id
   *    the device id.
   */
  public Device(final DeviceId id) {
    setId(id);
    setName(id.toString());
    controller = Controller.getInstance();
  }

  public Device() {
    setId(DeviceId.CONTROLLER);
    setName(DeviceId.CONTROLLER.name());
  }

  /**
   * Set the name of this device.
   * 
   * @param name
   *    the name of the device.
   */
  private void setName(final String name) {
    if ((name != null) && (name.length() > 0)) {
      this.name = name;
    }
  }

  /**
   * Update this device once every Controller update.
   */
  protected void update() {
    if (isNewMessage()) {
      while (getMessages().size() > 0) {
        AclMessage msg = getMessages().poll();
        previousMessage = lastMessage;
        if (previousMessage == null) {
          previousMessage = msg;
        }
        lastMessage = msg;
        this.update(msg);
      }
    }
  }

  @Override
  public synchronized void execute() {
    try {
      gatherMessages();
      update();
      runCounter++;
      if (runCounter == Constants.ALTERNATE_SPEED) {
        runCounter = 0;
        LOGGER.debug("Device heartbeat: " + getName() + ".");
        alternate();
      }
    } catch (Throwable t1) {
      LOGGER.error(getName() + ": " + t1.getMessage());
    }
  }

  /**
   * Accumulate all messages from the blackboard for this device.
   */
  protected void gatherMessages() {
    AclMessage msg = getController().getBlackboard().getMessage(this);
    while (msg != null && msg.isValid()) {
      messages.offer(msg);
      msg = getController().getBlackboard().getMessage(this);
    }
    if (messages.size() > Constants.MAX_BLACKBOARD_BUCKET_SIZE) {
      messages.clear();
    }
  }

  /**
   * Get the messages for this device.
   * 
   * @return
   *     the queue of messages for this device from the blackboard.
   */
  public PriorityBlockingQueue<AclMessage> getMessages() {
    return messages;
  }

  /**
   * Return true if there is a new message.
   * 
   * @return
   *    true if there is a message for this device.
   */
  protected boolean isNewMessage() {
    return messages.size() > 0;
  }

  /**
   * Return the most recent ACL message.
   *
   * @return the most recent ACL message.
   */
  public AclMessage getLastAclMessage() {
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
   * Note: Only called during live tests. if Constants.SIMULATION is set to true this will not be
   * run.
   *
   * @return true if everything is OK
   */
  public abstract boolean selfTest();

  /**
   * Return the device ID.
   *
   * @return
   *    the device id.
   */
  public DeviceId getId() {
    return id;
  }
  


  /**
   * Send a message to the OLED device to display a message.
   * 
   * @param text the text to send to the display.
   */
  public void setDisplay(final String text) {
    sendMessage(DeviceId.OLED_DEVICE, text,
        ActionId.DISPLAY, AclPriority.MEDIUM);
  }

  /**
   * Send a message to receiver.
   * 
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessage(final MessageType type, final DeviceId receiver, final String content,
      final double value, final ActionId action, final AclPriority priority, final UUID uuid) {
    final AclMessage m = new AclMessage(type);
    m.setActionId(action);
    m.setContent(content);
    m.setReciever(receiver);
    m.setSender(getId());
    m.setValue(value);
    m.setPriority(priority);
    m.setUuid(uuid);
    getController().getBlackboard().addMessage(m);
  }
  
  /**
   * Send a message to receiver.
   * 
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessage(final MessageType type, final DeviceId receiver, final String content,
      final double value, final ActionId action, final AclPriority priority) {
    final AclMessage m = new AclMessage(type);
    m.setActionId(action);
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
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessage(final DeviceId receiver, final String content, final double value,
      final ActionId action, final AclPriority priority) {
    sendMessage(MessageType.SEND, receiver, content, value, action, priority);
  }

  /**
   * Send a message to receiver.
   * 
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessage(final DeviceId receiver, final String content, final ActionId action,
      final AclPriority priority) {
    sendMessage(receiver, content, 0.0, action, priority);
  }

  /**
   * Send a message to receiver. (ACLPriority.MEDIUM)
   * 
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessage(
      final DeviceId receiver, final String content, final ActionId action) {
    sendMessage(receiver, content, 0.0, action, AclPriority.MEDIUM);
  }

  /**
   * Send an ACLMessage.
   * 
   * @param aclm
   *    the ACLMessage to send.
   */
  public void sendMessage(AclMessage aclm) {
    sendMessage(aclm.getMessageType(),
        aclm.getReciever(), aclm.getContent(), aclm.getValue(), aclm.getActionId(),
        aclm.getPriority(), aclm.getUuid());
  }
  


  /**
   * Send a message to receiver. (ACLPriority.HIGH)
   * 
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessageHigh(final DeviceId receiver, final String content,
      final ActionId action) {
    sendMessage(receiver, content, 0.0, action, AclPriority.HIGH);
  }

  /**
   * Send a message to receiver. (ACLPriority.HIGH)
   * 
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessageHigh(final DeviceId receiver, final String content, final double value,
      final ActionId action) {
    sendMessage(receiver, content, value, action, AclPriority.HIGH);
  }

  /**
   * Send a message to receiver. (ACLPriority.LOW)
   * 
   * @param receiver the message recipient
   * @param content the contents of the message
   * @param action the actionId
   */
  public void sendMessageLow(final DeviceId receiver,
      final String content, final ActionId action) {
    sendMessage(receiver, content, 0.0, action, AclPriority.LOW);
  }

  /**
   * Send a reply to the last sender.
   * 
   * @param content
   *    the content of this message.
   * @param value
   *    the value of this message.
   * @param action
   *    the ActionID for this message.
   */
  public void sendReply(final String content, final double value, final ActionId action,
      final AclPriority priority) {
    sendMessage(MessageType.REPLY,
        lastMessage.getSender(), content, value, action, priority, lastMessage.getUuid());
  }

  /**
   * Send a reply to the last sender.
   * 
   * @param content
   *    the content of this message.
   * @param action
   *    the actionid of this message.
   */
  public void sendReply(final String content,
      final ActionId action, final AclPriority priority) {
    sendMessage(MessageType.REPLY,
        lastMessage.getSender(), content, 0.0, action, priority, lastMessage.getUuid());
  }

  /**
   * Send a reply to the last sender.
   * 
   * @param content
   *    the content of this message.
   */
  public void sendReply(final String content) {
    sendMessage(MessageType.REPLY, lastMessage.getSender(), content, 0.0, lastMessage.getActionId(),
        lastMessage.getPriority(), lastMessage.getUuid());
  }

  /**
   * Send a reply to the last sender.
   * 
   * @param content
   *     the content for this message
   * @param value
   *     the value of this message.
   */
  public void sendReply(final String content, final double value) {
    sendMessage(MessageType.REPLY, lastMessage.getSender(), content, value,
        lastMessage.getActionId(), lastMessage.getPriority(), lastMessage.getUuid());
  }

  /**
   * Set the device id.
   *
   * @param id
   *     the device id for this message.
   */
  private void setId(final DeviceId id) {
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
   *    the name of this Device.
   */
  protected String getName() {
    return name;
  }

  /**
   * Get a reference to the controller.
   * 
   * @return
   *    an instance of the controller.
   */
  protected Controller getController() {
    return controller;
  }

  /**
   * Initialize this device.
   */
  public void initialize() {
    if (!isInitialized()) {
      if (controller == null) {
        controller = Controller.getInstance();
      }
      try {
        LOGGER.debug("Initializing " + getName());
        init();
        LOGGER.debug("Device: " + getName() + " initialized.");
        initialized = true;
      } catch (Throwable t1) {
        LOGGER.error(getName() + ": " + t1.getMessage());
      }
    }
  }
}
