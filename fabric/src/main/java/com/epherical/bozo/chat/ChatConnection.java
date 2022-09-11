package com.epherical.bozo.chat;

import com.epherical.bozo.ChatProtocol;
import com.epherical.bozo.packets.handler.HostPacketHandler;
import com.epherical.bozo.mixin.ConnectionAccessor;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;

/**
 * An extension of connection so we can use our own packet encoder/decoder with our attribute instead
 * of the ConnectionProtocol in the regular Connection. it's not PERFECT, the other attribute is used for erroring
 * still, but we don't really care about anything if an error occurs...
 */
public class ChatConnection extends Connection {

    public static final AttributeKey<ChatProtocol> ATTRIBUTE_CHAT_PROTOCOL = AttributeKey.valueOf("chat_protocol");

    public ChatConnection(PacketFlow packetFlow) {
        super(packetFlow);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        super.exceptionCaught(channelHandlerContext, throwable);
        throwable.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        ConnectionAccessor accessor = (ConnectionAccessor) this;

        accessor.getChannel().attr(ATTRIBUTE_CHAT_PROTOCOL).set(ChatProtocol.CHAT_PROTOCOL);
    }

    @Override
    protected void sendPacket(Packet<?> packet, @Nullable PacketSendListener packetSendListener) {
        ConnectionAccessor accessor = (ConnectionAccessor) this;
        accessor.setSendPackets(accessor.getsentPackets() + 1);

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
    protected void doSendPacket(Packet<?> packet, @Nullable PacketSendListener packetSendListener, ConnectionProtocol connectionProtocol, ConnectionProtocol connectionProtocol2) {
        if (connectionProtocol != connectionProtocol2) {
            this.setProtocol(connectionProtocol);
        }
        ConnectionAccessor accessor = (ConnectionAccessor) this;

        ChannelFuture channelFuture =  accessor.getChannel().writeAndFlush(packet);
        if (packetSendListener != null) {
            channelFuture.addListener((future) -> {
                if (future.isSuccess()) {
                    packetSendListener.onSuccess();
                } else {
                    Packet<?> failedPacket = packetSendListener.onFailure();
                    if (failedPacket != null) {
                        ChannelFuture newFuture = accessor.getChannel().writeAndFlush(failedPacket);
                        newFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                }
            });
        }

        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public HostPacketHandler getCustomPacketListener() {
        return (HostPacketHandler) super.getPacketListener();
    }
}
