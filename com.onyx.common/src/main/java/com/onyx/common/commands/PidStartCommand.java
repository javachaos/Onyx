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
   * True or False.
   */
  private String trueFalse;

  /**
   * Start the PID Controller.
   * @param sender
   *    the device tasked with sending this message.
   */
  public PidStartCommand(final DeviceId sender, final String trueFalse) {
    super(CommandType.PID_START, sender);
    this.trueFalse = trueFalse;
  }

  /**
   * Start the PID Controller.
   */
  public PidStartCommand(final String trueFalse) {
    this(DeviceId.COMM_CLIENT, trueFalse);
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.START_MOTORS);
    msg.setReciever(DeviceId.PID);
    msg.setContent(trueFalse);
    msg.setPriority(AclPriority.HIGH);
    msg.setSender(getSender());
    return msg;
  }
}
