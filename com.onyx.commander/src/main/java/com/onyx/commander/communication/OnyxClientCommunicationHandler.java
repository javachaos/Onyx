package com.onyx.commander.communication;


import com.onyx.common.commands.Command;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxClientCommunicationHandler extends SimpleChannelInboundHandler<Command> {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(OnyxClientCommunicationHandler.class);

  private final OnyxClient client;
  
  /**
   * Onyx Client Comm handler.
   * @param client
   *    the client.
   */
  public OnyxClientCommunicationHandler(OnyxClient client) {
    this.client = client;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Command msg) {
    LOGGER.debug(msg.getMessage().getContent());
    client.addInMessage(msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.debug(cause.getMessage());
    cause.printStackTrace();
    ctx.close();
  }
}
