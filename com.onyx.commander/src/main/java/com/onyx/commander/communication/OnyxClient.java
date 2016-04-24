package com.onyx.commander.communication;

import com.onyx.common.commands.CloseCommand;
import com.onyx.common.commands.Command;
import com.onyx.common.commands.CommandType;
import com.onyx.common.utils.Constants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

import javax.net.ssl.SSLException;

/**
 * Onyx Client.
 * @author fred
 *
 */
public class OnyxClient implements Runnable {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(OnyxClient.class);

  /**
   * Server hostname.
   */
  private String host;

  /**
   * Server port.
   */
  private int port;

  private SslContext sslCtx;
  private ArrayBlockingQueue<Command> outMsgs;
  private ArrayBlockingQueue<Command> inMsgs;
  private boolean isConnected;
  
  private Command lastOutMsg;
  private Command lastInMsg;

  /**
   * Onyx Client ctor.
   * @param servHost
   *    the server hostname.
   * @param servPort
   *    the server port number.
   */
  public OnyxClient(final String servHost, final int servPort) {
    this.host = servHost;
    this.port = servPort;
    outMsgs = new ArrayBlockingQueue<Command>(Constants.NETWORK_BUFFER_SIZE);
    inMsgs = new ArrayBlockingQueue<Command>(Constants.NETWORK_BUFFER_SIZE);
  }

  @Override
  public void run() {
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      sslCtx =
          SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
      Bootstrap boot = new Bootstrap();
      boot.group(workerGroup);
      boot.channel(NioSocketChannel.class);
      boot.handler(new OnyxClientChannelInitializer(this, sslCtx, host, port));
      Channel ch = boot.connect(host, port).sync().channel();

      ChannelFuture lastWriteFuture = ch.writeAndFlush(lastInMsg);

      for (;;) {
        Command msg = outMsgs.peek();
        if (msg != null && !msg.isValid()) {
          lastWriteFuture = ch.writeAndFlush(lastOutMsg = outMsgs.poll());
        }
        isConnected = true;
        if (lastOutMsg != null && lastOutMsg.getCommandType() == CommandType.CLOSE) {
          ch.closeFuture().sync();
          isConnected = false;
          break;
        }
      }

      if (lastWriteFuture != null) {
        lastWriteFuture.sync();
      }

    } catch (SSLException | InterruptedException e1) {
      LOGGER.error(e1.getMessage());
    } finally {
      workerGroup.shutdownGracefully();
    }
  }
  
  /**
   * Send a message to the Onyx server.
   * Blocks until the response is recieved.
   * 
   * @param msg
   *        the command string to send to the server.
   */
  public Command sendMessageAwaitReply(Command msg) {
    outMsgs.offer(msg);
    while (isConnected) {
      if (inMsgs.peek().getCommandId().compareTo(lastOutMsg.getCommandId()) == 0) {
        lastInMsg = inMsgs.poll();
        break;
      }
    }
    return lastInMsg;
  }

  /**
   * True if isConnected.
   * @return true if Client is connected to the server.
   */
  public boolean isConnected() {
    return isConnected;
  }

  /**
   * Shutdown the connection.
   */
  public void shutdown() {
    outMsgs.offer(new CloseCommand());
  }

  /**
   * Add a message to the inMsg stack.
   * @param msg
   *    the next Input message from the server.
   */
  public void addInMessage(Command msg) {
    inMsgs.offer(msg);
  }

  /**
   * Return the inMsgs stack.
   * 
   * @return
   *    the inMsgs stack.
   */
  public ArrayBlockingQueue<Command> getInMessages() {
    return inMsgs;
  }
}
