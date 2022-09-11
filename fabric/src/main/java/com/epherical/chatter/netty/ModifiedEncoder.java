package com.epherical.chatter.netty;

import com.epherical.chatter.ChatProtocol;
import com.epherical.chatter.chat.ChatConnection;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Modified version of {@link PacketEncoder}
 */
public class ModifiedEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PacketFlow flow;

    public ModifiedEncoder(PacketFlow flow) {
        this.flow = flow;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) throws Exception {
        ChatProtocol protocol = ctx.channel().attr(ChatConnection.ATTRIBUTE_CHAT_PROTOCOL).get();
        if (protocol == null) {
            throw new RuntimeException("unknown protocol " + msg);
        } else {
            Integer integer = protocol.getPacketId(this.flow, msg);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}",
                        ctx.channel().attr(ChatConnection.ATTRIBUTE_CHAT_PROTOCOL).get(), integer, msg.getClass().getName());
            }

            if (integer == null) {
                throw new IOException("Can't serialize unregistered packet");
            } else {
                FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(out);
                friendlyByteBuf.writeVarInt(integer);

                try {
                    int index = friendlyByteBuf.writerIndex();
                    msg.write(friendlyByteBuf);
                    int size = friendlyByteBuf.writerIndex() - index;
                    if (size > 8388608) {
                        throw new IllegalArgumentException("Packet too big (is " + size + ", should be less than 8388608): " + msg);
                    } else {
                        int id = ctx.channel().attr(ChatConnection.ATTRIBUTE_CHAT_PROTOCOL).get().getId();
                        JvmProfiler.INSTANCE.onPacketSent(id, integer, ctx.channel().remoteAddress(), size);
                    }
                } catch (Throwable e) {
                    LOGGER.error("Error receiving packet {}", integer, e);
                    if (msg.isSkippable()) {
                        throw new SkipPacketException(e);
                    } else {
                        throw e;
                    }
                }
            }

        }

    }
}
