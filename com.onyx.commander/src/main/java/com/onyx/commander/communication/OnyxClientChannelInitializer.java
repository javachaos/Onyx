package com.onyx.commander.communication;

import com.onyx.quadcopter.utils.Constants;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * Onyx Client Channel Initializer.
 * 
 * @author fred
 *
 */
public class OnyxClientChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final SslContext sslCtx;
  private final String host;
  private final int port;
  private final OnyxClient client; 

  /**
   * OnyxClientChannelInitializer.
   * @param onyxClient 
   * 
   * @param sslCtx the sslCtx
   */
  public OnyxClientChannelInitializer(OnyxClient onyxClient, SslContext sslCtx,
      final String host, final int port) {
    this.sslCtx = sslCtx;
    this.host = host;
    this.port = port;
    this.client = onyxClient;
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
    pipeline.addLast(
        new DelimiterBasedFrameDecoder(Constants.NIO_MAX_FRAMELEN, Delimiters.lineDelimiter()));
    pipeline.addLast(new StringDecoder());
    pipeline.addLast(new StringEncoder());
    pipeline.addLast(new OnyxClientCommunicationHandler(client));
  }
}
