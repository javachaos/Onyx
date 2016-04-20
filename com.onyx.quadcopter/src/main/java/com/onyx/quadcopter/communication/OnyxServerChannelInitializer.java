package com.onyx.quadcopter.communication;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;

public class OnyxServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * SSL Ctx
     */
    private final SslContext sslCtx;
    private final SimpleChannelInboundHandler<String> handler;

    public OnyxServerChannelInitializer(SslContext sslCtx, SimpleChannelInboundHandler<String> handler) {
	this.sslCtx = sslCtx;
	this.handler = handler;
    }

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
	ChannelPipeline pipeline = ch.pipeline();

	// Add SSL handler first to encrypt and decrypt everything.
	// In this example, we use a bogus certificate in the server side
	// and accept any invalid certificates in the client side.
	// You will need something more complicated to identify both
	// and server in the real world.
	pipeline.addLast(sslCtx.newHandler(ch.alloc()));
	pipeline.addLast(new ObjectEncoder());
	pipeline.addLast(new ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(getClass().getClassLoader())));
	// and then business logic.
	pipeline.addLast(handler);
    }
}
