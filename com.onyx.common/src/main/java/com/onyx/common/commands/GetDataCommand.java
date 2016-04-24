package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

/**
 * Get Data.
 * @author fred
 *
 */
public class GetDataCommand extends Command {

  /**
   * Generated SUID.
   */
  private static final long serialVersionUID = -1124261640235297587L;
  
  /**
   * Device name.
   */
  private String device;

  /**
   * Get data.
   * @param device
   */
  public GetDataCommand(final String device) {
    super(CommandType.GET_DATA, DeviceId.COMM_CLIENT);
    this.device = device;
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.SEND_DATA);
    msg.setSender(getSender());
    msg.setReciever(DeviceId.valueOf(device));
    msg.setPriority(AclPriority.MEDIUM);
    return msg;
  }

}
