package com.epherical.bozo.packets;

import com.epherical.bozo.packets.handler.HostPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class HostboundPlayerChatHeaderPacket implements Packet<ServerGamePacketListener> {


    private final SignedMessageHeader header;
    private final MessageSignature headerSignature;
    private final byte[] bodyDigest;

    public HostboundPlayerChatHeaderPacket(PlayerChatMessage playerChatMessage) {
        this(playerChatMessage.signedHeader(), playerChatMessage.headerSignature(), playerChatMessage.signedBody().hash().asBytes());
    }

    public HostboundPlayerChatHeaderPacket(FriendlyByteBuf friendlyByteBuf) {
        this(new SignedMessageHeader(friendlyByteBuf), new MessageSignature(friendlyByteBuf), friendlyByteBuf.readByteArray());
    }

    public HostboundPlayerChatHeaderPacket(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
        this.header = signedMessageHeader;
        this.headerSignature = messageSignature;
        this.bodyDigest = bs;

    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        this.header.write(buffer);
        this.headerSignature.write(buffer);
        buffer.writeByteArray(this.bodyDigest);
    }

    @Override
    public void handle(ServerGamePacketListener handler) {
        try {
            if (handler instanceof HostPacketHandler listener) {
                listener.handleHostHeader(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getBodyDigest() {
        return bodyDigest;
    }

    public MessageSignature getHeaderSignature() {
        return headerSignature;
    }

    public SignedMessageHeader getHeader() {
        return header;
    }
}
