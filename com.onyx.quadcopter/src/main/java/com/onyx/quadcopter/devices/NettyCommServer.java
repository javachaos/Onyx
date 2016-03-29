package com.onyx.quadcopter.devices;

import com.onyx.quadcopter.communication.ACLDecoder;
import com.onyx.quadcopter.communication.ACLEncoder;
import com.onyx.quadcopter.communication.CommunicationHandler;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyCommServer extends Device {

    static final int PORT = Constants.PORT;

    /**
     * Communication handler.
     */
    private final CommunicationHandler handler;

    public NettyCommServer(final Controller c) {
        super(c, DeviceID.COMM_SERVER);
        handler = new CommunicationHandler(getController());
    }

    public void run() throws Exception {

        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(final SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ACLEncoder(), new ACLDecoder(), handler);
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            final ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to
            // gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    protected void update() {
        switch (lastMessage.getActionID()) {
        case SEND_DATA:
            handler.addData(lastMessage);
        default:
            break;
        }
    }

    @Override
    protected void init() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    protected void alternate() {
    }

    @Override
    public boolean selfTest() {
        // TODO Auto-generated method stub
        return false;
    }

}