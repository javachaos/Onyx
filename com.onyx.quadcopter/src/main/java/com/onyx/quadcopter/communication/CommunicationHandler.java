package com.onyx.quadcopter.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.ConcurrentStack;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
public class CommunicationHandler extends ChannelInboundHandlerAdapter {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(CommunicationHandler.class);

    /**
     * The stack of data to push out over the wire as bytes.
     */
    private final ConcurrentStack<ACLMessage> dataStack;

    /**
     * Controller reference.
     */
    private final Controller controller;

    /**
     * Handle a connection.
     *
     * @param data
     *            The data to send to clients
     */
    public CommunicationHandler(final Controller c) {
        controller = c;
        dataStack = new ConcurrentStack<ACLMessage>();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        controller.getBlackboard().addMessage(((ACLMessage) msg));
        ctx.close();
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final ACLMessage msg = dataStack.pop();
        if (msg != null) {
            final ChannelFuture f = ctx.writeAndFlush(msg);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        throw new OnyxException(cause.getMessage(), LOGGER);
    }

    public synchronized void addData(final ACLMessage data) {
        if (data.isValid()) {
            dataStack.push(data);
        }
    }
}