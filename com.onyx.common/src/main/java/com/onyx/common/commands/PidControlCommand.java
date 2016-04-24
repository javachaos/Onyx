package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

public class PidControlCommand extends Command {

  private String yaw;
  private String pitch;
  private String roll;
  private String throttle;
  
  public PidControlCommand(final String yaw, String pitch, String roll, String throttle) {
    super(CommandType.PID_CONTROL, DeviceId.COMM_CLIENT);
    this.yaw = yaw;
    this.pitch = pitch;
    this.roll = roll;
    this.throttle = throttle;
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.CONTROL);
    msg.setContent(yaw + ","
               + pitch + ","
               +  roll + "," + throttle);
    msg.setPriority(AclPriority.HIGH);
    msg.setReciever(DeviceId.PID);
    msg.setSender(getSender());
    return msg;
  }

}
