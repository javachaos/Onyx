package com.onyx.quadcopter.messaging;

import java.io.Serializable;

import com.onyx.quadcopter.devices.DeviceID;

/**
 * Agent Communication Language Message.
 *
 * @author fredladeroute
 */

public final class ACLMessage implements Serializable {

    /**
     * Generated UID
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
    private DeviceID sender;

    /**
     * The intended recipient of the ACL Message.
     */
    private DeviceID reciever;

    /**
     * True if this message is empty.
     */
    private boolean isEmpty = false;

    /**
     * A value stored in this message.
     */
    private double value;

    /**
     * Contruct a new ACL message given a message type and an ActionID.
     *
     * @param type
     *            the MessageType which defines the message type of this message
     *
     * @param id
     *            the ActionID of this message
     */
    public ACLMessage(final MessageType type, final ActionId id) {
        if (type == MessageType.EMPTY) {
            isEmpty = true;
        }
        setId(type);
        setActionID(id);
    }

    /**
     * Constructs a new Null ACL message, where the MessageType is id and the
     * ActionID is null.
     *
     * @param id
     *            the MessageType for this ACL message
     */
    public ACLMessage(final MessageType id) {
        this(id, ActionId.NULL);
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
     * @param id
     *            the ActionID to set.
     */
    public void setActionID(final ActionId id) {
        actionId = id;
    }

    /**
     * Returns this ACLMessages ActionID.
     *
     * @return the ActionID for this ACLMessage
     */
    public ActionId getActionID() {
        return actionId;
    }

    /**
     * Sets the MessageType for this ACL Message.
     *
     * @param id
     *            the MessageType to set
     */
    private void setId(final MessageType id) {
        messageType = id;
    }

    /**
     * Set the contents of this message.
     *
     * @param c
     *            the content to be set
     */
    public void setContent(final String c) {
        content = c;
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
     * @param id
     *            the senders AgentID to be set
     */
    public void setSender(final DeviceID id) {
        sender = id;
    }

    /**
     * Returns the sender of this ACL message.
     *
     * @return the sender of this ACL message
     */
    public DeviceID getSender() {
        return sender;
    }

    /**
     * Returns the expected receiver of this ACL message.
     *
     * @return the expected receiver of this ACL message
     */
    public DeviceID getReciever() {
        return reciever;
    }

    /**
     * Set the reciever of this ACL message.
     *
     * @param id
     *            the id of the reciever to be set
     */
    public void setReciever(final DeviceID id) {
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
        return "{" + messageType.name() + "," + sender.name() + "," + reciever.name() + "," + content + "}";
    }

    /**
     * Validate this ACL message to ensure that it can be sent to the
     * blackboard.
     *
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

}
