package com.onyx.quadcopter.commands;

import com.onyx.quadcopter.communication.OnyxServerChannelHandler;
import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.messaging.AclMessage;
import com.onyx.quadcopter.messaging.AclPriority;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;

/**
 * Get Data Command.
 * @author fred
 *
 */
public class GetDataCmd extends NetworkCommand {
  
  /**
   * Get Data Command.
   * 
   * @param ch
   *    channel handler.
   */
  public GetDataCmd(OnyxServerChannelHandler ch) {
    super(ch, Command.GET_DATA);
  }

  @Override
  protected boolean execute() {
    AclMessage acl = new AclMessage(MessageType.SEND);
    switch (getArgs()[0]) {
      case "ORIENT":
        acl.setActionId(ActionId.GET_ORIENT);
        acl.setPriority(AclPriority.MEDIUM);
        acl.setSender(DeviceId.COMM_SERVER);
        acl.setReciever(DeviceId.GYRO_MAG_ACC);
        acl.setValue(0.0);
        getChannelHandler().sendMessage(acl);
        return true;
      case "MOTOR1-SPD":
        acl.setActionId(ActionId.GET_SPEED);
        acl.setPriority(AclPriority.MEDIUM);
        acl.setSender(DeviceId.COMM_SERVER);
        acl.setReciever(DeviceId.MOTOR1);
        acl.setValue(0.0);
        getChannelHandler().sendMessage(acl);
        return true;
      case "MOTOR2-SPD":
        acl.setActionId(ActionId.GET_SPEED);
        acl.setPriority(AclPriority.MEDIUM);
        acl.setSender(DeviceId.COMM_SERVER);
        acl.setReciever(DeviceId.MOTOR2);
        acl.setValue(0.0);
        getChannelHandler().sendMessage(acl);
        return true;
      case "MOTOR3-SPD":
        acl.setActionId(ActionId.GET_SPEED);
        acl.setPriority(AclPriority.MEDIUM);
        acl.setSender(DeviceId.COMM_SERVER);
        acl.setReciever(DeviceId.MOTOR3);
        acl.setValue(0.0);
        getChannelHandler().sendMessage(acl);
        return true;
      case "MOTOR4-SPD":
        acl.setActionId(ActionId.GET_SPEED);
        acl.setPriority(AclPriority.MEDIUM);
        acl.setSender(DeviceId.COMM_SERVER);
        acl.setReciever(DeviceId.MOTOR4);
        acl.setValue(0.0);
        getChannelHandler().sendMessage(acl);
        return true;
      default:
        break;
    }
    return false;
  }

  @Override
  protected String usage() {
    return "DATA-GET:{device-name} (Ex. DATA-GET:MOTOR1-SPD)";
  }

}
