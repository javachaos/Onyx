package com.onyx.quadcopter.communication;

import com.onyx.common.commands.Command;

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
   * SSL Ctx.
   */
  private final SslContext sslCtx;
  private final SimpleChannelInboundHandler<Command> handler;

  public OnyxServerChannelInitializer(SslContext sslCtx,
      SimpleChannelInboundHandler<Command> handler) {
    this.sslCtx = sslCtx;
    this.handler = handler;
  }

  @Override
  public void initChannel(final SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(sslCtx.newHandler(ch.alloc()));
    pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())));
    pipeline.addLast(new ObjectEncoder());
    pipeline.addLast(handler);
  }
}
