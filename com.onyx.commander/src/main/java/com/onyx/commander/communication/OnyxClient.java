package com.onyx.commander.communication;

import com.onyx.quadcopter.utils.ConcurrentStack;

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

  private ConcurrentStack<String> msgs;

  private String lastMsg;

  private boolean isConnected;

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
    msgs = new ConcurrentStack<String>();
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
      boot.handler(new OnyxClientChannelInitializer(sslCtx, host, port));

      Channel ch = boot.connect(host, port).sync().channel();
      ChannelFuture lastWriteFuture = null;

      for (;;) {
        String msg = msgs.peek();
        if (msg != null && !msg.isEmpty()) {
          lastWriteFuture = ch.writeAndFlush(lastMsg = msgs.pop() + System.lineSeparator());
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
   * 
   * @param cmd
   *        the command string to send to the server.
   */
  public void sendMessage(String cmd) {
    msgs.push(cmd);
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
    msgs.push("COMM:CLOSE");
  }
}
