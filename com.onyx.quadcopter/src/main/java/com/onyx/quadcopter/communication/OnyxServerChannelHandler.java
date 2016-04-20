package com.onyx.quadcopter.communication;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Handles a server-side channel.
 */
@Sharable
public class OnyxServerChannelHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxServerChannelHandler.class);
    
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private String lastMsg;
    
    /**
     * Handle a connection.
     *
     * @param data
     *            The data to send to clients
     */
    public OnyxServerChannelHandler() {
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
	ctx.flush();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
	
	// Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        ctx.writeAndFlush(
                                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure Onyx service!\n");
                        ctx.writeAndFlush(
                                "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite.\n");

                        channels.add(ctx.channel());
                    }
        });
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
	cause.printStackTrace();
	ctx.close();
	LOGGER.error(cause.getMessage());
	throw new OnyxException(cause.getMessage(), LOGGER);
    }

    public synchronized void addData(final String data) {
	channels.parallelStream().forEach(e -> e.writeAndFlush(data));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
	//controller.getBlackboard().addMessage(msg);
	switch(msg) {
	    case "CLOSE":
                ctx.close();
                break;
            default:
        	LOGGER.debug(msg);
        	lastMsg = msg;
                break;
	}
    }

    public String getLastMsg() {
	return lastMsg;
    }
}