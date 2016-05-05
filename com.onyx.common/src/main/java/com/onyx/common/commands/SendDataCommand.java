package com.onyx.common.commands;

import java.util.UUID;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

public class SendDataCommand extends Command {

  /**
   * Generated SUID.
   */
  private static final long serialVersionUID = -8342981818920036650L;
  
  private String data;

  /**
   * UUID of this command.
   */
  private UUID uuid;

  public SendDataCommand(DeviceId sender, final String data, final UUID uuid) {
    super(CommandType.SEND_DATA, sender);
    this.data = data;
    this.uuid = uuid;
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.SEND_DATA);
    msg.setContent(data);
    msg.setSender(getSender());
    msg.setReciever(DeviceId.COMM_CLIENT);
    msg.setUuid(uuid);
    return msg;
  }

}
