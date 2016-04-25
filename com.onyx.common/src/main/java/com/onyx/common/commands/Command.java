package com.onyx.common.commands;

import java.io.Serializable;
import java.util.UUID;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

public abstract class Command implements Serializable {

  /**
   * Generated SUID.
   */
  private static final long serialVersionUID = 4316230993090229307L;
  
  /**
   * The type of command.
   */
  private final CommandType type;
  private final DeviceId sender;
  protected AclMessage msg = new AclMessage(MessageType.EMPTY);
  
  public Command(final CommandType type, final DeviceId sender) {
    this.type = type;
    this.sender = sender;
  }

  /**
   * Get the type of command this is.
   * @return the type
   */
  public CommandType getCommandType() {
    return type;
  }
  
  /**
   * Get ACL Message.
   * 
   * @return
   *    the AclMessage to execute this command.
   */
  protected abstract AclMessage getAclMessage();
  
  /**
   * Get the ACL Message for this command.
   * @return
   *    the acl message for this command.
   */
  public AclMessage getMessage() {
    if (!msg.isValid()) {
      msg = getAclMessage();
    }
    return msg;
  }

  /**
   * @return the sender
   */
  public DeviceId getSender() {
    return sender;
  }

  public boolean isValid() {
    return msg.isValid() && sender != null;
  }

  /**
   * @return the commandId
   */
  public UUID getCommandId() {
    if (!msg.isValid()) {
      msg = getAclMessage();
    }
    return msg.getUuid();
  }
  
  @Override
  public String toString() {
    return getMessage().toString();
  }
}
