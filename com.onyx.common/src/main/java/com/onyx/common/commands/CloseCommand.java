package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

public class CloseCommand extends Command {

  public CloseCommand() {
    super(CommandType.CLOSE, DeviceId.COMM_CLIENT);
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.CLOSE_CONNECTION);
    msg.setSender(getSender());
    msg.setReciever(DeviceId.COMM_SERVER);
    return msg;
  }

}
