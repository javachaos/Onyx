package com.onyx.commander.communication;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public OnyxClient(final String servHost, final int servPort) {
	this.host = servHost;
	this.port = servPort;
	msgs = new ConcurrentStack<String>();
    }

    @Override
    public void run() {
	EventLoopGroup workerGroup = new NioEventLoopGroup();
	try {
	    sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	    Bootstrap b = new Bootstrap();
	    b.group(workerGroup);
	    b.channel(NioSocketChannel.class);
	    b.handler(new OnyxClientChannelInitializer(sslCtx, host, port));

	    Channel ch = b.connect(host, port).sync().channel();
	    ChannelFuture lastWriteFuture = null;

	    while(true) {
		String m = msgs.peek();
		if (m != null && !m.isEmpty()) {
		    lastWriteFuture = ch.writeAndFlush(lastMsg = msgs.pop() + System.lineSeparator());
		}

		if (lastMsg.equals("CLOSE:CLOSE")) {
		    ch.closeFuture().sync();
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

    public void addMessage(String cmd) {
	msgs.push(cmd);
    }
}
