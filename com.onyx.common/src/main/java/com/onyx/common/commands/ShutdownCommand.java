package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

/**
 * Shutdown command.
 * @author fred
 *
 */
public class ShutdownCommand extends Command {

  /**
   * Generated SUID.
   */
  private static final long serialVersionUID = -2812678237358097336L;

  /**
   * Create a new shutdown command.
   */
  public ShutdownCommand(final DeviceId sender) {
    super(CommandType.SHUTDOWN, sender);
  }
  
  /**
   * Create a new shutdown command.
   */
  public ShutdownCommand() {
    super(CommandType.SHUTDOWN, DeviceId.COMM_SERVER);
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.SHUTDOWN);
    msg.setPriority(AclPriority.HIGH);
    msg.setReciever(DeviceId.CONTROLLER);
    msg.setSender(DeviceId.COMM_CLIENT);
    return msg;
  }

}
