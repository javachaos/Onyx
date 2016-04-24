package com.onyx.common.communication;

public class NetworkMessage {

  private int msgId;
  private String msg;
  
  /**
   * Create a new Network Message.
   * 
   * @param id
   *    the id of this message.
   * @param message
   *    the message of this message.
   */
  public NetworkMessage(int id, String message) {
    this.msgId = id;
    this.msg = message;
  }
  
  /**
   * Get ID.
   * @return
   *    the Id of this message.
   */
  public int getId() {
    return msgId;
  }
  
  /**
   * Get the content of this message.
   * @return
   *    the content of this message.
   */
  public String getContent() {
    return msg;
  }
}
