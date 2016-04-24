package com.onyx.common.commands;

public class CommandUtils {
  
  /**
   * Parse a command string and return a command.
   * @param cmdString
   * @return
   */
  public static Command parseCommand(final String cmdString) {
    String cmd;
    String[] args = null;
    if (cmdString.contains(":")) {
      final String[] data = cmdString.split(":");
      cmd = data[0];
      args = data[1].split(",");
    } else {
      cmd = cmdString;  
    }
    switch(CommandType.valueOf(cmd)) {
      case CLOSE:
        return new CloseCommand();
      case GET_DATA:
        return new GetDataCommand(args[0]);
      case MOTOR_SPD:
        return new MotorSpeedCommand(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
      case PID_CONTROL:
        return new PidControlCommand(args[0],args[1],args[2],args[3]);
      case PID_START:
        return new PidStartCommand();
      case SHUTDOWN:
        return new ShutdownCommand();
      default:
        break;
    }
    return null;
  }

}
