package com.epherical.bozo.packets;

import com.epherical.bozo.ServerPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;


public class HostBoundSystemChatPacket implements Packet<ServerGamePacketListener> {

    private final Component component;
    private final boolean overlay;

    public HostBoundSystemChatPacket(FriendlyByteBuf buf) {
        this(buf.readComponent(), buf.readBoolean());
    }

    public HostBoundSystemChatPacket(Component component, boolean bl) {
        this.component = component;
        this.overlay = bl;
    }

    public Component getComponent() {
        return component;
    }

    public boolean isOverlay() {
        return overlay;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeComponent(this.component);
        buffer.writeBoolean(this.overlay);
    }

    @Override
    public void handle(ServerGamePacketListener handler) {
        if (handler instanceof ServerPacketListener listener) {
            listener.handleHostSystem(this);
        }
    }
}
