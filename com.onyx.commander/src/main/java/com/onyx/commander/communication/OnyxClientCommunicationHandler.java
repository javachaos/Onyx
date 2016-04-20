package com.onyx.commander.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OnyxClientCommunicationHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxClientCommunicationHandler.class);
    
    public OnyxClientCommunicationHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
	if (msg instanceof String) {
	    String m = msg.toString();
            LOGGER.debug(m);
	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	LOGGER.debug(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}