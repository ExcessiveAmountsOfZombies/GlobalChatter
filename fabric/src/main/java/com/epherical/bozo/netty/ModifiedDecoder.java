package com.epherical.bozo.netty;

import com.epherical.bozo.chat.ChatConnection;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Modified version of {@link net.minecraft.network.PacketDecoder}
 */
public class ModifiedDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PacketFlow flow;

    public ModifiedDecoder(PacketFlow packetFlow) {
        this.flow = packetFlow;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int size = in.readableBytes();
        if (size != 0) {
            FriendlyByteBuf buf = new FriendlyByteBuf(in);
            int packetID = buf.readVarInt();
            Packet<?> packet = ctx.channel().attr(ChatConnection.ATTRIBUTE_CHAT_PROTOCOL).get().createPacket(this.flow, packetID, buf);
            if (packet == null) {
                throw new IOException("Bad packet id " + packetID);
            } else {
                int k = ctx.channel().attr(ChatConnection.ATTRIBUTE_CHAT_PROTOCOL).get().getId();
                JvmProfiler.INSTANCE.onPacketReceived(k, packetID, ctx.channel().remoteAddress(), size);
                if (buf.readableBytes() > 0) {
                    int id = (ctx.channel().attr(ChatConnection.ATTRIBUTE_CHAT_PROTOCOL).get()).getId();
                    throw new IOException("Packet " + id + "/" + packetID + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + buf.readableBytes() + " bytes extra whilst reading packet " + packetID);
                } else {
                    out.add(packet);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), packetID, packet.getClass().getName());
                    }

                }
            }
        }
    }



}
