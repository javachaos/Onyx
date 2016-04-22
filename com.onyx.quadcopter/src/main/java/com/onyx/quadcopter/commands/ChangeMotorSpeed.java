package com.onyx.quadcopter.commands;

import com.onyx.quadcopter.communication.OnyxServerChannelHandler;
import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.utils.Constants;

public class ChangeMotorSpeed extends NetworkCommand {

  
  /**
   * Change motor speed command.
   * @param ch
   *    the channel handler.
   */
  public ChangeMotorSpeed(final OnyxServerChannelHandler ch) {
    super(ch, Command.PID_CONTROL);
  }

  @Override
  public boolean execute() {
    if (getArgs()[0] == null || getArgs()[1] == null) {
      return false;
    }
    if (getArgs()[1] != null && !getArgs()[1].matches(Constants.DOUBLE_REGEX)) {
      return false;
    }
    switch (getArgs()[0]) {
      case "1":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR1,
              "null", Double.parseDouble(getArgs()[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      case "2":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR2,
              "null", Double.parseDouble(getArgs()[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      case "3":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR3,
              "null", Double.parseDouble(getArgs()[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      case "4":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR4,
              "null", Double.parseDouble(getArgs()[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      default:
        break;
    }
    return true;
  }

  @Override
  public String usage() {
    return "MOTOR-SPD requires 2 args. "
         + "[1] MotorID [1-4]. "
         + "[2] [0.0-100.0] Motor speed. "
         + "Ex. { MOTOR-SPD:1:100.0 (Set motor 1 to 100.0) }";
  }

}
