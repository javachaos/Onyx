package com.onyx.quadcopter.commands;

import com.onyx.quadcopter.communication.OnyxServerChannelHandler;

public abstract class NetworkCommand {

  /**
   * Command Argument separator.
   */
  public static final String COMMAND_ARG_SEPARATOR = ",";
  private static final String COMMAND_SEPARATOR = ":";
  
  private OnyxServerChannelHandler ch;
  private String[] args;

  /**
   * The name of this command.
   */
  private final Command cmd;

  /**
   * Network Command.
   * 
   * @param cmd
   *    the command.
   * @param ch
   *    the channel handler.
   */
  public NetworkCommand(final OnyxServerChannelHandler ch, final Command cmd) {
    this.cmd = cmd;
    this.ch = ch;
  } 
  
  /**
   * Run this command.
   */
  public boolean run(String cmd) {
    final String[] data = cmd.split(COMMAND_SEPARATOR);
    if (isCmd(Command.valueOf(data[0]))) {
      this.args = data[1].split(COMMAND_ARG_SEPARATOR);
      if (execute()) {
        return true;
      } else {
        getChannelHandler().addData(usage());
        return false;
      }
    } else {
      return false;
    }
  }

  public boolean isCmd(Command cmd) {
    return (getCommand() == cmd);
  }
  
  /**
   * Execute this command.
   * @return
   *    true if the args are correct.
   */
  protected abstract boolean execute();
  
  /**
   * Print the usage to network client.
   * @return 
   *    how to use this command.
   */
  protected abstract String usage();

  /**
   * Get the channel handler.
   * @return the channel handler.
   */
  public OnyxServerChannelHandler getChannelHandler() {
    return ch;
  }

  /**
   * Get arguments.
   * @return the args
   */
  public String[] getArgs() {
    return args;
  }

  /**
   * Return the command.
   * @return the Command
   */
  public Command getCommand() {
    return cmd;
  }
}
