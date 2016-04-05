package com.onyx.commander.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyCommClient implements Runnable {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(NettyCommClient.class);
    
    /**
     * Server hostname.
     */
    private String host;
    
    /**
     * Server port.
     */
    private int port;

    public NettyCommClient(final String servHost, final int servPort) {
	this.host = servHost;
	this.port = servPort;
    }

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
    
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(
                	    ClassResolvers.softCachingConcurrentResolver(getClass().getClassLoader())), new ClientCommunicationHandler());
                }
            });
    
            // Start the client.
            ChannelFuture f;
	    try {
		f = b.connect(host, port).sync();
	        f.channel().closeFuture().sync();
	    } catch (InterruptedException e) {
		e.printStackTrace();
		LOGGER.debug(e.getMessage());
	    } // (5)
    
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
