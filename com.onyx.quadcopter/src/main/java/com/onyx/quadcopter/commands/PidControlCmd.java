package com.onyx.quadcopter.commands;

import com.onyx.quadcopter.communication.OnyxServerChannelHandler;
import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ActionId;

public class PidControlCmd extends NetworkCommand {

  /**
   * Create a new PID Control command.
   * @param ch
   *    the channel handler.
   */
  public PidControlCmd(final OnyxServerChannelHandler ch) {
    super(ch, Command.PID_CONTROL);
  }

  @Override
  protected boolean execute() {
    if (getArgs().length != 4) {
      return false;//TODO Check more stuff
    }
    Controller.getInstance().sendMessageHigh(DeviceId.PID, getArgs()[0]
        + COMMAND_ARG_SEPARATOR + getArgs()[1]
        + COMMAND_ARG_SEPARATOR + getArgs()[2]
        + COMMAND_ARG_SEPARATOR + getArgs()[3], 0.0, ActionId.CONTROL);
    return false;
  }

  @Override
  protected String usage() {
    return "Bad command syntax. PID-CONTROL requires 4 args. "
        + "{PID-CONTROL:Yaw,Pitch,Roll,Throttle} (Ex. PID-CONTROL:0.0,0.1,0.1,25.0)";
  }
}
