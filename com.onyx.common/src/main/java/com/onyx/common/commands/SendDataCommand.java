package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

public class SendDataCommand extends Command {

  private String data;

  public SendDataCommand(DeviceId sender, final String data) {
    super(CommandType.SEND_DATA, sender);
    this.data = data;
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.SEND_DATA);
    msg.setContent(data);
    msg.setSender(getSender());
    msg.setReciever(DeviceId.COMM_CLIENT);
    return msg;
  }

}
