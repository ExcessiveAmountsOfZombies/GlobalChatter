package com.epherical.chatter.packets;

import com.epherical.chatter.packets.handler.HostPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

import java.util.UUID;

public class HostBoundPlayerChatPacket implements Packet<ServerGamePacketListener> {

    private final Component message;
    private final ChatType type;
    private final UUID sender;

    public HostBoundPlayerChatPacket(Component $$0, ChatType $$1, UUID $$2) {
        this.message = $$0;
        this.type = $$1;
        this.sender = $$2;
    }

    public HostBoundPlayerChatPacket(FriendlyByteBuf $$0) {
        this.message = $$0.readComponent();
        this.type = $$0.readEnum(ChatType.class);
        this.sender = $$0.readUUID();
    }

    public void write(FriendlyByteBuf $$0) {
        $$0.writeComponent(this.message);
        $$0.writeEnum(this.type);
        $$0.writeUUID(this.sender);
    }

    @Override
    public void handle(ServerGamePacketListener handler) {
        if (handler instanceof HostPacketHandler listener) {
            listener.handleHostChat(this);
        }
    }

    public Component getMessage() {
        return this.message;
    }

    public ChatType getType() {
        return this.type;
    }

    public UUID getSender() {
        return this.sender;
    }

    public boolean isSkippable() {
        return true;
    }
}
