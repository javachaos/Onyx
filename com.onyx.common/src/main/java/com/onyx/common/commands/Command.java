package com.onyx.common.commands;

import java.io.Serializable;
import java.util.UUID;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

/**
 * This class represents an abstract Command.
 * This is the base class for all Commands within the
 * Onyx system.
 * This class implements the {@link Serializable} interface.
 * 
 * @author fred
 *
 */
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
  
  /**
   * Create a new Command.
   * 
   * @param type
   *    the {@link CommandType} of this {@link Command}.
   * @param sender
   *    the {@link DeviceId} of the sender.
   */
  public Command(final CommandType type, final DeviceId sender) {
    this.type = type;
    this.sender = sender;
  }

  /**
   * Get the type of command this is.
   * @return the {@link CommandType}
   */
  public CommandType getCommandType() {
    return type;
  }
  
  /**
   * Get ACL Message.
   * 
   * @return
   *    the {@link AclMessage} to execute this command.
   */
  protected abstract AclMessage getAclMessage();
  
  /**
   * Get the {@link AclMessage} for this command.
   * @return
   *    the {@link AclMessage} for this command.
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

  /**
   * Return true if this is a valid command,
   * this function stipulates that both the
   * {@link DeviceId sender} and the {@link AclMessage}
   * are valid and non null.
   * 
   * @return
   *    true if this {@link Command} is valid.
   */
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
