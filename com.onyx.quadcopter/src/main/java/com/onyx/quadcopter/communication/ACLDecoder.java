package com.onyx.quadcopter.communication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.utils.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ACLDecoder extends ByteToMessageDecoder {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ACLDecoder.class);

    final byte[] buffer = new byte[Constants.NETWORK_BUFFER_SIZE];
    final ByteArrayInputStream inputByteArray = new ByteArrayInputStream(buffer);
    ObjectInputStream ois;

    public ACLDecoder() {
        try {
            ois = new ObjectInputStream(inputByteArray);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

        final int msgSize = (int) in.readUnsignedInt();

        if (in.readableBytes() < msgSize) {
            return;
        }

        try {
            inputByteArray.read(in.readBytes(msgSize).array());
            out.add(ois.readObject());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }
}
