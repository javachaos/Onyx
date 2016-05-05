package com.onyx.quadcopter.communication;

import com.onyx.common.commands.Command;
import com.onyx.common.commands.CommandType;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a server-side channel.
 */
@Sharable
public class OnyxServerChannelHandler extends SimpleChannelInboundHandler<Command> {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(OnyxServerChannelHandler.class);
  private static final ChannelGroup channels =
      new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  private Command lastCmd;
  private final OnyxServer server;
  
  /**
   * Onyx Server channel handler.
   * 
   * @param onyxServer
   *    the onyx server instance.
   */
  public OnyxServerChannelHandler(final OnyxServer onyxServer) {
    this.server = onyxServer;
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) {
    ctx.pipeline().get(SslHandler.class).handshakeFuture()
        .addListener(new GenericFutureListener<Future<Channel>>() {
          @Override
          public void operationComplete(Future<Channel> future) throws Exception {
            channels.add(ctx.channel());
          }
        });
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    LOGGER.error(cause.getMessage());
  }

  /**
   * Add Data to each connected channel.
   * @param msg
   *    the string data to send.
   */
  public synchronized void addData(final Command msg) {
    channels.parallelStream().forEach(e -> e.writeAndFlush(msg));
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
    if (msg.getCommandType() == CommandType.CLOSE) {
      ctx.close();
      return;
    }
    server.sendMessage(msg.getMessage());
    LOGGER.debug(msg.toString());
    lastCmd = msg;
  }

  public Command getLastCmd() {
    return lastCmd;
  }
}
