package com.onyx.quadcopter.communication;

import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.AclMessage;
import com.onyx.quadcopter.messaging.AclPriority;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.ThreadUtils;

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
import java.util.concurrent.TimeUnit;

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
    if (msg.equals("COMM:CLOSE")) {
      ctx.close();
    }
    
    final String[] data = msg.split(":");

    switch (data[0]) {
      case "MOTOR1-SPD":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR1,
              "null", Double.parseDouble(data[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      case "MOTOR2-SPD":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR2,
              "null", Double.parseDouble(data[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      case "MOTOR3-SPD":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR3,
              "null", Double.parseDouble(data[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      case "MOTOR4-SPD":
        Controller.getInstance().sendMessageHigh(DeviceId.MOTOR4,
              "null", Double.parseDouble(data[1]), ActionId.CHANGE_MOTOR_SPEED);
        break;
      case "PID-START":
        Controller.getInstance().sendMessageHigh(DeviceId.PID, data[1], 0.0, ActionId.START_MOTORS);
        break;
      case "PID-CONTROL":
        Controller.getInstance().sendMessageHigh(DeviceId.PID, data[1], 0.0, ActionId.CONTROL);
        break;
      case "DATA-GET":
        switch (data[1]) {
          case "ORIENT":
            AclMessage acl = new AclMessage(MessageType.SEND);
            acl.setActionId(ActionId.GET_ORIENT);
            acl.setPriority(AclPriority.MEDIUM);
            acl.setSender(DeviceId.COMM_CLIENT);
            acl.setReciever(DeviceId.GYRO_MAG_ACC);
            acl.setValue(0.0);
            acl.setContent("");
            Controller.getInstance().sendMessage(acl);
            ThreadUtils.await(1, 1, TimeUnit.SECONDS);
            break;
          default:
            break;
        }
        AclMessage aclmsg = new AclMessage(MessageType.EMPTY);
        //Block and wait until we get the requested data.
        aclmsg = Controller.getInstance()
            .getBlackboard()
            .getMessage(DeviceId.COMM_SERVER);
        ctx.writeAndFlush(aclmsg.getContent() + System.lineSeparator());
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
