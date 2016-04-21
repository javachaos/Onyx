package com.onyx.quadcopter.messaging;

/**
 * Defines an ACL message type.
 *
 * @author fredladeroute
 *
 */
public enum MessageType {

  /**
   * Reply message.
   * This message is a reply to a received message.
   */
  REPLY(0),

  /**
   * An initial send message.
   */
  SEND(1),

  /**
   * This message is an empty message.
   */
  EMPTY(2),

  /**
   * This message is a relay message to no need to read the contents.
   */
  RELAY(3),

  /**
   * This message contains base64 encoded image data.
   */
  IMAGE(4);

  /**
   * The id of the MessageType instance.
   */
  private int id;

  /**
   * Private constructor of a MessageType.
   *
   * @param idx the id of the MessageType
   * 
   */
  private MessageType(final int idx) {
    id = idx;
  }

  /**
   * Return the id of the given MessageType.
   *
   * @param id the MessageType to get the id of
   * 
   * @return the ordinal value of the MessageType t
   */
  public static int getId(final MessageType id) {
    return id.id;
  }
}
