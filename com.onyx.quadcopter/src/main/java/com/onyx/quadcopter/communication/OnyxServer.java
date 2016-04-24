package com.onyx.quadcopter.communication;

import com.onyx.common.concurrent.ConcurrentStack;
import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.exceptions.OnyxException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;

/**
 * Onyx Server implemented using the Netty API.
 * @author fred
 *
 */
public class OnyxServer extends Device implements Runnable {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(OnyxServer.class);

  /**
   * Server Port.
   */
  static final int PORT = Constants.PORT;

  /**
   * Boss group.
   */
  final EventLoopGroup bossGroup = new NioEventLoopGroup(Constants.NUM_NIO_THREADS);

  /**
   * Worker group.
   */
  final EventLoopGroup workerGroup = new NioEventLoopGroup();

  /**
   * Communication handler.
   */
  private OnyxServerChannelHandler handler;
  
  /**
   * ConcurrentStack of responses.
   */
  private ConcurrentStack<String> responses;

  private OnyxServerChannelInitializer initializer;

  public OnyxServer() {
    super(DeviceId.COMM_SERVER);
  }

  @Override
  protected void init() {
    try {
      SelfSignedCertificate ssc = new SelfSignedCertificate();
      SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
      handler = new OnyxServerChannelHandler(this);
      initializer = new OnyxServerChannelInitializer(sslCtx, handler);
    } catch (final SSLException | CertificateException e1) {
      ExceptionUtils.logError(getClass(), e1);
      throw new OnyxException(e1.getMessage(), LOGGER);
    }
  }

  @Override
  public void run() {
    LOGGER.debug("Starting CommServer.");
    try {
      final ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(initializer);
      b.bind(PORT).sync().channel().closeFuture().sync();
      LOGGER.debug("CommServer Started.");
    } catch (final InterruptedException e1) {
      ExceptionUtils.logError(getClass(), e1);
      throw new OnyxException(e1.getMessage(), LOGGER);
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

  @Override
  protected void update() {
    super.update();
  }

  @Override
  public void update(final AclMessage msg) {
    handler.addData(msg);
  }

  @Override
  public void shutdown() {
    LOGGER.debug("CommServer shutdown initiated.");
    workerGroup.shutdownGracefully();
    bossGroup.shutdownGracefully();
    LOGGER.debug("CommServer shutdown complete.");
  }

  @Override
  protected void alternate() {
    final String peek = handler.getLastCmd().getMessage().getContent();
    if (peek != null) {
      setDisplay("Latest Comm: " + peek);
    }
  }

  @Override
  public boolean selfTest() {
    return true;// TODO complete NettyCommServer selfTest.
  }

  /**
   * Get the next response to send to client.
   * @return
   *    the next response to send to the client.
   */
  public String getNextResponse() {
    return responses.pop();
  }

}
