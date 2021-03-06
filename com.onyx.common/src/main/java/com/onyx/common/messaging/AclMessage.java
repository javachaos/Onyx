package com.onyx.common.messaging;

import java.io.Serializable;
import java.util.UUID;


/**
 * Agent Communication Language Message.
 *
 * @author fredladeroute
 */

public final class AclMessage implements Serializable, Comparable<AclMessage> {

  /**
   * Generated UID.
   */
  private static final long serialVersionUID = 8967159441840581610L;

  /**
   * Message type for this ACL Message.
   */
  private MessageType messageType;

  /**
   * The action id for this ACL Message.
   */
  private ActionId actionId;

  /**
   * The contents of the message.
   */
  private String content;

  /**
   * The sender of the ACL Message.
   */
  private DeviceId sender;

  /**
   * The intended recipient of the ACL Message.
   */
  private DeviceId reciever;

  /**
   * True if this message is empty.
   */
  private boolean isEmpty = false;

  /**
   * A value stored in this message.
   */
  private double value;

  /**
   * The priority of this ACLMessage.
   */
  private AclPriority priority = AclPriority.MEDIUM;

  /**
   * UUID for this ACL Message.
   */
  private UUID uuid;

  /**
   * Contruct a new ACL message given a message type and an ActionID.
   *
   * @param type the MessageType which defines the message type of this message
   *
   * @param id the ActionID of this message
   */
  public AclMessage(final MessageType type, final ActionId id) {
    if (type == MessageType.EMPTY) {
      isEmpty = true;
    }
    setId(type);
    setActionId(id);
    uuid = UUID.randomUUID();
  }

  /**
   * Constructs a new Null ACL message, where the MessageType is id and the ActionID is null.
   *
   * @param id the MessageType for this ACL message
   */
  public AclMessage(final MessageType id) {
    this(id, ActionId.NULL);
  }
  
  /**
   * Set the UUID for this AclMessage.
   * @param uuid
   *    the UUID for this acl message.
   */
  public void setUuid(final UUID uuid) {
    this.uuid = uuid;
  }

  /**
   * Returns true if the message is empty and therefore has no content.
   *
   * @return true if the message is empty
   */
  public boolean isEmpty() {
    return isEmpty;
  }

  /**
   * Set the ActionID for this Message.
   *
   * @param id the ActionID to set.
   */
  public void setActionId(final ActionId id) {
    actionId = id;
  }

  /**
   * Returns this ACLMessages ActionID.
   *
   * @return the ActionID for this ACLMessage
   */
  public ActionId getActionId() {
    return actionId;
  }

  /**
   * Sets the MessageType for this ACL Message.
   *
   * @param id the MessageType to set
   */
  private void setId(final MessageType id) {
    messageType = id;
  }

  /**
   * Set the contents of this message.
   *
   * @param content the content to be set
   */
  public void setContent(final String content) {
    this.content = content;
  }

  /**
   * Returns the contents of this message.
   *
   * @return the contents of this message
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the sender of this message.
   *
   * @param id the senders AgentID to be set
   */
  public void setSender(final DeviceId id) {
    sender = id;
  }

  /**
   * Returns the sender of this ACL message.
   *
   * @return the sender of this ACL message
   */
  public DeviceId getSender() {
    return sender;
  }

  /**
   * Returns the expected receiver of this ACL message.
   *
   * @return the expected receiver of this ACL message
   */
  public DeviceId getReciever() {
    return reciever;
  }

  /**
   * Set the reciever of this ACL message.
   *
   * @param id the id of the reciever to be set
   */
  public void setReciever(final DeviceId id) {
    reciever = id;
  }

  /**
   * Return the message type of this ACL message.
   *
   * @return the message type of this ACL message.
   */
  public MessageType getMessageType() {
    return messageType;
  }

  @Override
  public String toString() {
    return "{" + messageType.name() + "," + sender.name() + "," + reciever.name() + "," + content
        + "}";
  }

  /**
   * Validate this ACL message to ensure that it can be sent to the blackboard.
   * @return true if this message is valid.
   */
  public boolean isValid() {
    if ((messageType != null) && (sender != null) && (reciever != null) && (actionId != null)) {
      return !isEmpty();
    }
    return false;
  }

  public double getValue() {
    return value;
  }

  public void setValue(final double value) {
    this.value = value;
  }

  /**
   * Get the priority of this ACLMessage in relation to all other messages.
   * 
   * @return the priority of this ACLMessage.
   */
  public AclPriority getPriority() {
    return priority;
  }

  /**
   * Set the priority of this ACLMessage.
   * 
   * @param priority The priority of this ACLMessage to be set.
   */
  public void setPriority(final AclPriority priority) {
    this.priority = priority;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((actionId == null) ? 0 : actionId.hashCode());
    result = (prime * result) + ((content == null) ? 0 : content.hashCode());
    result = (prime * result) + (isEmpty ? 1231 : 1237);
    result = (prime * result) + ((messageType == null) ? 0 : messageType.hashCode());
    result = (prime * result) + ((reciever == null) ? 0 : reciever.hashCode());
    result = (prime * result) + ((sender == null) ? 0 : sender.hashCode());
    long temp;
    temp = Double.doubleToLongBits(value);
    result = (prime * result) + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AclMessage other = (AclMessage) obj;
    if (actionId != other.actionId) {
      return false;
    }
    if (content == null) {
      if (other.content != null) {
        return false;
      }
    } else if (!content.equals(other.content)) {
      return false;
    }
    if (isEmpty != other.isEmpty) {
      return false;
    }
    if (messageType != other.messageType) {
      return false;
    }
    if (reciever != other.reciever) {
      return false;
    }
    if (sender != other.sender) {
      return false;
    }
    if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(AclMessage other) {
    return getPriority().compareTo(other.getPriority());
  }
  
  /**
   * Get the UUID for this Message.
   * @return
   *     the UUID for this ACL Message.
   */
  public UUID getUuid() {
    return uuid;
  }

}
