package com.onyx.quadcopter.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.utils.ConcurrentStack;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a server-side channel.
 */
@Sharable
public class OnyxServerChannelHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxServerChannelHandler.class);

    /**
     * The stack of data to push out over the wire as bytes.
     */
    private final ConcurrentStack<String> dataStack;
    
    /**
     * Handle a connection.
     *
     * @param data
     *            The data to send to clients
     */
    public OnyxServerChannelHandler() {
	dataStack = new ConcurrentStack<String>();
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
	ctx.flush();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
	final String msg = dataStack.pop();
	if (msg != null) {
	    final ChannelFuture f = ctx.writeAndFlush(msg);
	    f.addListener(ChannelFutureListener.CLOSE);
	}
	try {
	    super.channelActive(ctx);
	} catch (Exception e) {
	    ExceptionUtils.logError(getClass(), e);
	}
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
	cause.printStackTrace();
	ctx.close();
	LOGGER.error(cause.getMessage());
	throw new OnyxException(cause.getMessage(), LOGGER);
    }

    public synchronized void addData(final String data) {
	if (dataStack.size() >= Constants.NETWORK_BUFFER_SIZE) {
	    dataStack.clear();
	}
	dataStack.push(data);
    }

    /**
     * Get this connection handlers data stack.
     * 
     * @return
     */
    public ConcurrentStack<String> getDataStack() {
	return dataStack;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
	LOGGER.debug(msg);
	//controller.getBlackboard().addMessage(msg);
	ctx.close();
    }
}