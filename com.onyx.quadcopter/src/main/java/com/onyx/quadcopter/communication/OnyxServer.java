package com.onyx.quadcopter.communication;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class OnyxServer extends Device implements Runnable {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxServer.class);

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
     * Ping clients for data.
     */
    private static final String pingRequest = "PING";

    public OnyxServer() {
	super(DeviceID.COMM_SERVER);

    }

    @Override
    protected void init() {
    }

    @Override
    public void run() {
	handler = new OnyxServerChannelHandler();
	LOGGER.debug("Starting CommServer.");
	try {
	    SelfSignedCertificate ssc = new SelfSignedCertificate();
	    SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
	    final ServerBootstrap b = new ServerBootstrap();
	    b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
		    .handler(new LoggingHandler(LogLevel.INFO))
		    .childHandler(new OnyxServerChannelInitializer(sslCtx, handler))
		    .option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

	    b.bind(PORT).sync().channel().closeFuture().sync();

	    LOGGER.debug("CommServer Started.");
	} catch (final InterruptedException | SSLException | CertificateException e) {
	    ExceptionUtils.logError(getClass(), e);
	    throw new OnyxException(e.getMessage(), LOGGER);
	} finally {
	    workerGroup.shutdownGracefully();
	    bossGroup.shutdownGracefully();
	}
    }

    @Override
    protected void update() {
	super.update();

	// Send client a request for data.
	handler.addData(pingRequest);
    }

    @Override
    public void update(final ACLMessage msg) {
	switch (msg.getActionID()) {
	case SEND_DATA:
	    handler.addData(msg.getContent());
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
	final String peek = handler.getDataStack().peek();
	if (peek != null) {
	    setDisplay("Latest Comm: " + peek);
	}
    }

    @Override
    public boolean selfTest() {
	return true;// TODO complete NettyCommServer selfTest.
    }

}