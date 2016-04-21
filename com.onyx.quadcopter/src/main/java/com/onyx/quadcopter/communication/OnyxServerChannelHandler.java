package com.onyx.quadcopter.communication;

import com.onyx.quadcopter.exceptions.OnyxException;

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

import java.net.InetAddress;

/**
 * Handles a server-side channel.
 */
@Sharable
public class OnyxServerChannelHandler extends SimpleChannelInboundHandler<String> {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(OnyxServerChannelHandler.class);
  private static final ChannelGroup channels =
      new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  private String lastMsg;

  @Override
  public void channelActive(final ChannelHandlerContext ctx) {

    // Once session is secured, send a greeting and register the channel to
    // the global channel
    // list so the channel received the messages from others.
    ctx.pipeline().get(SslHandler.class).handshakeFuture()
        .addListener(new GenericFutureListener<Future<Channel>>() {
          @Override
          public void operationComplete(Future<Channel> future) throws Exception {
            ctx.writeAndFlush(
                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure OnyxServer!\n");
            ctx.writeAndFlush("Your session is protected by "
                + ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite()
                + " cipher suite.\n");
            channels.add(ctx.channel());
          }
        });
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    cause.printStackTrace();
    ctx.close();
    LOGGER.error(cause.getMessage());
    throw new OnyxException(cause.getMessage(), LOGGER);
  }

  public synchronized void addData(final String data) {
    channels.parallelStream().forEach(e -> e.writeAndFlush(data + System.lineSeparator()));
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    switch (msg) {
      case "COMM:CLOSE":
        ctx.close();
        break;
      default:
        break;
    }
    LOGGER.debug(msg);
    lastMsg = msg;
  }

  public String getLastMsg() {
    return lastMsg;
  }
}
