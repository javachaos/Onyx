package com.onyx.commander.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.utils.ConcurrentStack;
import com.onyx.quadcopter.utils.Constants;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class OnyxClientCommunicationHandler extends ChannelInboundHandlerAdapter {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxClientCommunicationHandler.class);
    private ConcurrentStack<String> dataStack;
    
    public OnyxClientCommunicationHandler() {
	dataStack = new ConcurrentStack<String>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	if (msg instanceof String) {
            LOGGER.debug(msg.toString());
            String m1 = dataStack.pop();
            if (m1 != null) {
                final ChannelFuture f = ctx.writeAndFlush(m1);
                f.addListener(ChannelFutureListener.CLOSE);
            }
	}
    }

    public synchronized void addData(final String data) {
	if (dataStack.size() >= Constants.NETWORK_BUFFER_SIZE) {
	    dataStack.clear();
	}
        dataStack.push(data);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	LOGGER.debug(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}