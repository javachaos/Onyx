package com.onyx.quadcopter.communication;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.common.commands.Command;
import com.onyx.common.commands.SendDataCommand;
import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.exceptions.OnyxException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

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

  private OnyxServerChannelInitializer initializer;

  /**
   * Server channel.
   */
  private Channel ch;

  /**
   * Last Server message as a string.
   */
  private String lastMsg;

  public OnyxServer() {
    super(DeviceId.COMM_SERVER);
  }

  @Override
  protected void init() {}

  @Override
  public void run() {
    LOGGER.debug("Starting CommServer.");
    try {
      SelfSignedCertificate ssc = new SelfSignedCertificate();
      SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
      handler = new OnyxServerChannelHandler(this);
      initializer = new OnyxServerChannelInitializer(sslCtx, handler);
      final ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(initializer)
          .option(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.SO_KEEPALIVE, true);
      ch = b.bind(PORT).sync().channel();
      ch.closeFuture().sync();
      LOGGER.debug("CommServer Started.");
    } catch (final InterruptedException | SSLException | CertificateException e1) {
      ExceptionUtils.logError(getClass(), e1);
      throw new OnyxException(e1.getMessage(), LOGGER);
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

  @Override
  protected void update() {
	if (handler == null) {
		return;//Wait until the handler is ready.
	}
    super.update();
	Command c = handler.getLastCmd();
	if (c != null && c.isValid()) {
	  lastMsg = c.getMessage().getContent();
	}
	try {
	  if (lastMsg != null) {
	    setDisplay("Latest Comm: " + lastMsg + System.lineSeparator()
	    + "IP: " 
	    + getLocalHostLANAddress() + System.lineSeparator()
	    + "Connected: " + Boolean.toString(ch.isActive()));
	  } else {
	    setDisplay("IP: " + getLocalHostLANAddress()
	    + System.lineSeparator()
	    + "Connected: " + Boolean.toString(ch.isActive()));
	  }
	} catch (UnknownHostException e) {
	  LOGGER.error(e.getMessage());
	}
    
  }

  @Override
  public void update(final AclMessage msg) {
    switch (msg.getActionId()) {
      case SEND_DATA:
        final Command cmd = new SendDataCommand(
            msg.getSender(), 
            msg.getContent(), 
            msg.getUuid());
        handler.addData(cmd);
        break;
      case CLOSE_CONNECTION:
        break;
      default:
        break;
    }
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
  }

  @Override
  public boolean selfTest() {
    return true;// TODO complete NettyCommServer selfTest.
  }
  
  /**
   * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
   * <p/>
   * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
   * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
   * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
   * specify the algorithm used to select the address returned under such circumstances, and will often return the
   * loopback address, which is not valid for network communication. Details
   * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
   * <p/>
   * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
   * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
   * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
   * first site-local address if the machine has more than one), but if the machine does not hold a site-local
   * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
   * <p/>
   * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
   * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
   * <p/>
   *
   * @throws UnknownHostException If the LAN address of the machine cannot be found.
   */
  private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
      try {
          InetAddress candidateAddress = null;
          // Iterate all NICs (network interface cards)...
          for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
              NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
              // Iterate all IP addresses assigned to each card...
              for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                  InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                  if (!inetAddr.isLoopbackAddress()) {

                      if (inetAddr.isSiteLocalAddress()) {
                          // Found non-loopback site-local address. Return it immediately...
                          return inetAddr;
                      }
                      else if (candidateAddress == null) {
                          // Found non-loopback address, but not necessarily site-local.
                          // Store it as a candidate to be returned if site-local address is not subsequently found...
                          candidateAddress = inetAddr;
                          // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                          // only the first. For subsequent iterations, candidate will be non-null.
                      }
                  }
              }
          }
          if (candidateAddress != null) {
              // We did not find a site-local address, but we found some other non-loopback address.
              // Server might have a non-site-local address assigned to its NIC (or it might be running
              // IPv6 which deprecates the "site-local" concept).
              // Return this non-loopback candidate address...
              return candidateAddress;
          }
          // At this point, we did not find a non-loopback address.
          // Fall back to returning whatever InetAddress.getLocalHost() returns...
          InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
          if (jdkSuppliedAddress == null) {
              throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
          }
          return jdkSuppliedAddress;
      }
      catch (Exception e) {
          UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
          unknownHostException.initCause(e);
          throw unknownHostException;
      }
  }
}
