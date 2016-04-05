package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.communication.CommunicationHandler;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.Constants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class NettyCommServer extends Device implements TimerTask {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(NettyCommServer.class);

    static final int PORT = Constants.PORT;

    /**
     * Boss group.
     */
    final EventLoopGroup bossGroup = new NioEventLoopGroup(Constants.NUM_NIO_THREADS);

    /**
     * Worker group.
     */
    final EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * Communication handler.
     */
    private final CommunicationHandler handler;

    /**
     * Ping clients for data.
     */
    private ACLMessage pingRequest;

    public NettyCommServer(final Controller c) {
        super(c, DeviceID.COMM_SERVER);
        handler = new CommunicationHandler(getController());
    }

    @Override
    protected void init() {
	pingRequest = new ACLMessage(MessageType.RELAY);
	pingRequest.setActionID(ActionId.SEND_DATA);
	pingRequest.setSender(getId());
	pingRequest.setReciever(DeviceID.COMM_CLIENT);
	pingRequest.setValue(System.currentTimeMillis());
    }

    @Override
    public void run(Timeout t) {
        LOGGER.debug("Starting CommServer.");
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(final SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(
                        	    ClassResolvers.softCachingConcurrentResolver(getClass().getClassLoader())), handler);
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            LOGGER.debug("CommServer Started.");
            // Bind and start to accept incoming connections.
            final ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to
            // gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } catch (final InterruptedException e) {
            LOGGER.error(e.getMessage());
            throw new OnyxException(e.getMessage(), LOGGER);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    protected void update() {
	//Send client a request for data.
        handler.addData(pingRequest);
        switch (lastMessage.getActionID()) {
        case SEND_DATA:
            handler.addData(lastMessage);
        default:
            break;
        }
    }

    @Override
    public void shutdown() {
        LOGGER.debug("CommServer shutdown initiated.");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        LOGGER.debug("CommServer shutdown complete.");
    }

    @Override
    protected void alternate() {
    }

    @Override
    public boolean selfTest() {
        return true;// TODO complete NettyCommServer selfTest.
    }

}