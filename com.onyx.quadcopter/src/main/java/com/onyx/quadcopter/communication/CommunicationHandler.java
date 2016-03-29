package com.onyx.quadcopter.communication;

import java.util.Stack;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
public class CommunicationHandler extends ChannelInboundHandlerAdapter {

    /**
     * The stack of data to push out over the wire as bytes.
     */
    private final Stack<ACLMessage> dataStack;

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
        dataStack = new Stack<ACLMessage>();
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
        final ChannelFuture f = ctx.writeAndFlush(dataStack.pop());
        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        // Close the connection when an exception is raised
        cause.printStackTrace();
        ctx.close();
    }

    public void addData(final ACLMessage data) {
        if (data.isValid()) {
            dataStack.push(data);
        }
    }
}