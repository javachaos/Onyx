package com.onyx.quadcopter.communication;

import com.onyx.quadcopter.commands.ChangeMotorSpeed;
import com.onyx.quadcopter.commands.GetDataCmd;
import com.onyx.quadcopter.commands.NetworkCommand;
import com.onyx.quadcopter.commands.PidControlCmd;
import com.onyx.quadcopter.commands.PidStartCmd;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.messaging.AclMessage;
import com.onyx.quadcopter.utils.Constants;

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
import java.util.concurrent.ArrayBlockingQueue;

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
  private final OnyxServer server;

  private final ArrayBlockingQueue<NetworkCommand> networkCommands;
  
  /**
   * Onyx Server channel handler.
   * 
   * @param onyxServer
   *    the onyx server instance.
   */
  public OnyxServerChannelHandler(final OnyxServer onyxServer) {
    this.server = onyxServer;
    this.networkCommands = new ArrayBlockingQueue<NetworkCommand>(Constants.NUM_NET_COMMANDS);
    init();
  }

  private void init() {
    addNetworkCommand(new ChangeMotorSpeed(this));
    addNetworkCommand(new GetDataCmd(this));
    addNetworkCommand(new PidControlCmd(this));
    addNetworkCommand(new PidStartCmd(this));
  }
  
  /**
   * Add a network command.
   * @param cmd
   *     the command to be added to this Server handler.
   */
  public void addNetworkCommand(final NetworkCommand cmd) {
    try {
      networkCommands.put(cmd);
    } catch (InterruptedException e1) {
      LOGGER.error(e1.getMessage());
    }
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) {
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

  /**
   * Add Data to each connected channel.
   * @param data
   *    the string data to send.
   */
  public synchronized void addData(final String data) {
    channels.parallelStream().forEach(e -> e.writeAndFlush(data + System.lineSeparator()));
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    if (msg.equals("COMM:CLOSE")) {
      ctx.close();
    }
    networkCommands.parallelStream().forEach(e -> e.run(msg));
    LOGGER.debug(msg);
    lastMsg = msg;
  }

  public String getLastMsg() {
    return lastMsg;
  }

  public void sendMessage(AclMessage acl) {
    server.sendMessage(acl);
  }
}
