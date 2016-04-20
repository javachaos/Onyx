package com.onyx.commander.communication;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

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
     * Onyx Client Communication Handler.
     */
    private OnyxClientCommunicationHandler handler;

    /**
     * Server port.
     */
    private int port;

    public OnyxClient(final String servHost, final int servPort) {
	this.host = servHost;
	this.port = servPort;
	handler = new OnyxClientCommunicationHandler();
    }
    
    /**
     * Add Messages.
     * @param m
     */
    public void addMessage(final String m) {
	handler.addData(m);
    }

    @Override
    public void run() {
	// Configure SSL.
	SslContext sslCtx = null;
	try {
	    sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	} catch (SSLException e1) {
	    LOGGER.error(e1.getMessage());
	}
	EventLoopGroup workerGroup = new NioEventLoopGroup();
	try {
	    Bootstrap b = new Bootstrap();
	    b.group(workerGroup);
	    b.channel(NioSocketChannel.class);
	    b.option(ChannelOption.SO_KEEPALIVE, true);
	    b.handler(new OnyxClientChannelInitializer(handler, sslCtx, host, port));

	    // Start the client.
	    ChannelFuture f;
	    try {
		f = b.connect(host, port).sync();
		f.channel().closeFuture().sync();
	    } catch (InterruptedException e) {
		e.printStackTrace();
		LOGGER.debug(e.getMessage());
	    }

	} finally {
	    workerGroup.shutdownGracefully();
	}
    }
}
