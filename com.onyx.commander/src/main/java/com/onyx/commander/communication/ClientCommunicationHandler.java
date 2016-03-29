package com.onyx.commander.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.messaging.ACLMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientCommunicationHandler extends ChannelInboundHandlerAdapter {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ClientCommunicationHandler.class);
    
    //TODO implement message stack.
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ACLMessage m = (ACLMessage) msg;
        //TODO add M to message stack.
        LOGGER.debug(m.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	LOGGER.debug(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}