package com.onyx.commander.communication;

import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.common.concurrent.ConcurrentStack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

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
  private ConcurrentStack<String> outMsgs;
  private ConcurrentStack<String> inMsgs;
  private String lastMsg;
  private boolean isConnected;
  private AtomicInteger messageId;

  /**
   * Last message recieved from Server.
   */
  private String lastInMsg = "COMM:START";

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
    outMsgs = new ConcurrentStack<String>();
    inMsgs = new ConcurrentStack<String>();
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
      
      ChannelFuture lastWriteFuture = ch.writeAndFlush(lastInMsg + System.lineSeparator());
      for (;;) {
        String msg = outMsgs.peek();
        if (msg != null && !msg.isEmpty()) {
          lastWriteFuture = ch.writeAndFlush(lastMsg = outMsgs.pop() + System.lineSeparator());
        }
        isConnected = true;
        if (lastMsg != null && lastMsg.equals("COMM:CLOSE")) {
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
   * @param cmd
   *        the command string to send to the server.
   */
  public String sendMessageAwaitReply(final String cmd) {
    outMsgs.push(messageId.getAndIncrement()+"#" + cmd);
    while (isConnected) {
      //Wait for response.
      if (!inMsgs.peek().equals(lastInMsg)) {
        lastInMsg = inMsgs.pop();
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
    outMsgs.push("COMM:CLOSE");
  }

  /**
   * Add a message to the inMsg stack.
   * @param msg
   *    the next Input message from the server.
   */
  public void addInMessage(String msg) {
    inMsgs.push(msg);
  }

  /**
   * Return the inMsgs stack.
   * 
   * @return
   *    the inMsgs stack.
   */
  public ConcurrentStack<String> getInMessages() {
    return inMsgs;
  }
}
