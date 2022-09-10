package com.epherical.bozo.packets;

import com.epherical.bozo.ServerPacketListener;
import com.google.common.base.MoreObjects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

import java.util.List;
import java.util.Objects;

public class HostboundPlayerInfoPacket implements Packet<ServerGamePacketListener> {

    private final ClientboundPlayerInfoPacket.Action action;
    private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

    public HostboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, List<ClientboundPlayerInfoPacket.PlayerUpdate> collection) {
        this.action = action;
        this.entries = collection;
    }

    public HostboundPlayerInfoPacket(FriendlyByteBuf friendlyByteBuf) {
        this.action = friendlyByteBuf.readEnum(ClientboundPlayerInfoPacket.Action.class);
        ClientboundPlayerInfoPacket.Action var10002 = this.action;
        Objects.requireNonNull(var10002);
        this.entries = friendlyByteBuf.readList(var10002::read);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
        ClientboundPlayerInfoPacket.Action var10002 = this.action;
        Objects.requireNonNull(var10002);
        buffer.writeCollection(this.entries, var10002::write);
    }

    @Override
    public void handle(ServerGamePacketListener handler) {
        if (handler instanceof ServerPacketListener listener) {
            listener.handleHostPlayerInfo(this);
        }
    }

    public List<ClientboundPlayerInfoPacket.PlayerUpdate> getEntries() {
        return this.entries;
    }

    public ClientboundPlayerInfoPacket.Action getAction() {
        return this.action;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
    }
}
