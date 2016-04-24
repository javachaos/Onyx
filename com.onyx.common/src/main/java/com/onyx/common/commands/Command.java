package com.onyx.common.commands;

import java.util.UUID;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;

public abstract class Command {

  /**
   * The type of command.
   */
  private final CommandType type;
  private final DeviceId sender;
  
  private final UUID commandId;
  
  public Command(final CommandType type, final DeviceId sender) {
    this.type = type;
    this.sender = sender;
    this.commandId = UUID.randomUUID();
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
  public abstract AclMessage getAclMessage();

  /**
   * @return the sender
   */
  public DeviceId getSender() {
    return sender;
  }

  public boolean isValid() {
    return getAclMessage().isValid() && sender != null;
  }

  /**
   * @return the commandId
   */
  public UUID getCommandId() {
    return commandId;
  }
}
