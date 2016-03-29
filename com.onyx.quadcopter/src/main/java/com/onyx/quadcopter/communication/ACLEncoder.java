package com.onyx.quadcopter.communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ACLEncoder extends MessageToByteEncoder<ACLMessage> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ACLEncoder.class);

    private ByteArrayOutputStream baos;
    private ObjectOutputStream oos;

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ACLMessage msg, final ByteBuf out) {
        // out.write
        try {
            baos = new ByteArrayOutputStream(Constants.NETWORK_BUFFER_SIZE);
            oos = new ObjectOutputStream(baos);
            oos.writeObject(msg);
            final byte[] bytes = baos.toByteArray();
            out.writeBytes(ByteBuffer.allocate(4).putInt(bytes.length));
            // Pad the message header with 4bytes to represent the size of the
            // serialized ACLMessage
            out.writeBytes(bytes);
            // Write the message over the wire.
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        } finally {
            try {
                oos.close();
                baos.close();
            } catch (final IOException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }

    }
}
