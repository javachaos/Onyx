package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;

public class EmptyCommand extends Command {

  /**
   * Generated SUID.
   */
  private static final long serialVersionUID = -8763343816731073938L;

  public EmptyCommand() {
    super(CommandType.EMPTY, DeviceId.CONTROLLER);
  }

  @Override
  protected AclMessage getAclMessage() {
    return null;
  }

}
