package com.onyx.commander.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.commander.gui.GuiController;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.messaging.ACLMessage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientCommunicationHandler extends ChannelInboundHandlerAdapter {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ClientCommunicationHandler.class);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	if (msg instanceof ACLMessage) {
            final ACLMessage m = (ACLMessage) msg;
            GuiController.getBlackboard().addMessage(m);
            LOGGER.debug(m.toString());
            final ACLMessage m1 = GuiController.getBlackboard().getMessage(DeviceID.COMM_CLIENT);
            if (m1 != null) {
                final ChannelFuture f = ctx.writeAndFlush(m1);
                f.addListener(ChannelFutureListener.CLOSE);
            }
	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	LOGGER.debug(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}