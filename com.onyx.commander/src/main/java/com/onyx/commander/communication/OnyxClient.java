package com.onyx.commander.communication;

import com.onyx.common.commands.CloseCommand;
import com.onyx.common.commands.Command;
import com.onyx.common.commands.CommandType;
import com.onyx.common.commands.EmptyCommand;
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

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import javax.net.ssl.SSLException;

/**
 * Onyx Client.
 * 
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

  /**
   * SSL Context.
   */
  private SslContext sslCtx;
  
  /**
   * Out bound Command queue.
   */
  private ArrayBlockingQueue<Command> outMsgs;
  
  /**
   * In bound Command queue.
   */
  private ArrayBlockingQueue<Command> inMsgs;
  
  /**
   * True if connected.
   */
  private boolean isConnected;
  
  /**
   * The last command sent to the out bound queue.
   */
  private Command lastOutMsg;

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

      ChannelFuture lastFuture = null;
      for (;;) {
        // Poll the next command from the queue
        final Command next = outMsgs.poll();
        
        // if the next command is not null
        if (next != null) {
          // write the command over the pipe and save the channel future
          lastFuture = ch.writeAndFlush(next);
        }

        isConnected = true;
        
        // If the last command we sent out is a CLOSE command
        if (lastOutMsg != null && lastOutMsg.getCommandType() == CommandType.CLOSE) {
          // Close the channel
          ch.closeFuture().sync();
          isConnected = false;
          break;
        }

      }

      // if the connection is closed and the last future isn't null
      if (lastFuture != null) {
        // sync the last future and wait for the server to close the channel.
        lastFuture.sync();
      }

    } catch (SSLException | InterruptedException e1) {
      LOGGER.error(e1.getMessage());
    } finally {
      workerGroup.shutdownGracefully();
    }
  }

  /**
   * Send a command over the network.
   * @param cmd
   *    the command to be sent.
   */
  public void sendMessage(final Command cmd) {
    // If the cmd isn't null
    if (cmd != null) {
      // add the command to the command message queue
      outMsgs.offer(lastOutMsg = cmd);
    }
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
  
  /**
   * Get the next message from the in bound queue.
   * @return
   *    the next message from the in bound queue.
   */
  public Command getNextMessage() {
    return inMsgs.poll();
  }
  
  /**
   * Find next message that matches the UUID uuid.
   * 
   * @param uuid
   *    the uuid to match.
   * @return
   *    the first message which matches the uuid.
   */
  public Command findMessage(final UUID uuid) {
    // Search the in bound command queue in parallel for
    // the message with UUID uuid.
    return inMsgs
        .parallelStream()
        .filter(e -> e.getCommandId().equals(uuid)).findFirst()
        .orElse(new EmptyCommand());
  }
}
