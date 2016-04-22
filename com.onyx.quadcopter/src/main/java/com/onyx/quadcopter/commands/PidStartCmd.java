package com.onyx.quadcopter.commands;

import com.onyx.quadcopter.communication.OnyxServerChannelHandler;
import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ActionId;

public class PidStartCmd extends NetworkCommand {
  
  /**
   * PID Start cmd.
   * @param ch
   *    the channel handler.
   */
  public PidStartCmd(final OnyxServerChannelHandler ch) {
    super(ch, Command.PID_START);
  }

  @Override
  public boolean execute() {

    if (getArgs().length != 2) {
      return false;
    }
    Controller.getInstance().sendMessageHigh(DeviceId.PID, 
        getArgs()[0], 0.0, ActionId.START_MOTORS);
    return true;
  }

  @Override
  protected String usage() {
    return "PID-START:{true|false} (Ex. PID-START:true)";
  }

}
