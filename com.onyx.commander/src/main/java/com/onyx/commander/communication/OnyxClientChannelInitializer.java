package com.onyx.commander.communication;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;

public class OnyxClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    private final SslContext sslCtx;
    
    private final String host;
    private final int port;
    
    /**
     * OnyxClientChannelInitializer.
     * 
     * @param sslCtx
     * 		the sslCtx
     */
    public OnyxClientChannelInitializer(SslContext sslCtx, final String host, final int port) {
	this.sslCtx = sslCtx;
	this.host = host;
	this.port = port;
    }
    
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
	ChannelPipeline pipeline = ch.pipeline();
	// Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.
        pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
	pipeline.addLast(new ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(getClass().getClassLoader())));
	pipeline.addLast(new ObjectEncoder());
	pipeline.addLast(new ClientCommunicationHandler());
    }
}
