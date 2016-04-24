package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

public class PidStartCommand extends Command {
  
  /**
   * Generated SUID.
   */
  private static final long serialVersionUID = -3717501742803718516L;

  /**
   * Start the PID Controller.
   * @param sender
   *    the device tasked with sending this message.
   */
  public PidStartCommand(final DeviceId sender) {
    super(CommandType.PID_START, sender);
  }

  /**
   * Start the PID Controller.
   */
  public PidStartCommand() {
    this(DeviceId.COMM_CLIENT);
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.START_MOTORS);
    msg.setReciever(DeviceId.PID);
    msg.setPriority(AclPriority.HIGH);
    msg.setSender(getSender());
    return null;
  }

}
