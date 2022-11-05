package com.epherical.chatter.chat;

import com.epherical.chatter.ChatProtocol;
import com.epherical.chatter.mixin.ConnectionAccessor;
import com.epherical.chatter.packets.handler.HostPacketHandler;
import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * An extension of connection so we can use our own packet encoder/decoder with our attribute instead
 * of the ConnectionProtocol in the regular Connection. it's not PERFECT, the other attribute is used for erroring
 * still, but we don't really care about anything if an error occurs...
 */
@ChannelHandler.Sharable
public class ChatConnection extends Connection {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final AttributeKey<ChatProtocol> ATTRIBUTE_CHAT_PROTOCOL = AttributeKey.valueOf("chat_protocol");

    public ChatConnection(PacketFlow packetFlow) {
        super(packetFlow);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        super.exceptionCaught(channelHandlerContext, throwable);
        LOGGER.info("Printing stacktrace, likely one of the listeners disconnected.");
        throwable.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        ConnectionAccessor accessor = (ConnectionAccessor) this;

        accessor.getChannel().attr(ATTRIBUTE_CHAT_PROTOCOL).set(ChatProtocol.CHAT_PROTOCOL);
    }

    @Override
    protected void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> packetSendListener) {
        ConnectionAccessor accessor = (ConnectionAccessor) this;
        accessor.setSendPackets(accessor.getSentPackets() + 1);

        if (accessor.getChannel().eventLoop().inEventLoop()) {
            // ConnectionProtocol does not matter, so the fields are nulled.
            this.doSendPacket(packet, packetSendListener, null, null);
        } else {
            accessor.getChannel().eventLoop().execute(() -> {
                this.doSendPacket(packet, packetSendListener, null, null);
            });
        }
    }

    @Override
    protected void doSendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> packetSendListener, ConnectionProtocol connectionProtocol, ConnectionProtocol connectionProtocol2) {
        if (connectionProtocol != connectionProtocol2) {
            this.setProtocol(connectionProtocol);
        }

        ConnectionAccessor accessor = (ConnectionAccessor) this;
        ChannelFuture channelFuture = accessor.getChannel().writeAndFlush(packet);
        if (packetSendListener != null) {
            channelFuture.addListener(packetSendListener);
        }
        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public HostPacketHandler getCustomPacketListener() {
        return (HostPacketHandler) super.getPacketListener();
    }
}
