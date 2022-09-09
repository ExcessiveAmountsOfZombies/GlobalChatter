package com.epherical.bozo.packets;

import com.epherical.bozo.ServerPacketListener;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class HostboundPlayerInfoPacket implements Packet<ServerGamePacketListener> {

    private final ClientboundPlayerInfoPacket.Action action;
    private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

    public HostboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, ServerPlayer... serverPlayers) {
        this.action = action;
        this.entries = Lists.newArrayListWithCapacity(serverPlayers.length);
        int var4 = serverPlayers.length;

        for (ServerPlayer serverPlayer : serverPlayers) {
            this.entries.add(createPlayerUpdate(serverPlayer));
        }

    }

    public HostboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, Collection<ServerPlayer> collection) {
        this.action = action;
        this.entries = Lists.newArrayListWithCapacity(collection.size());
        for (ServerPlayer serverPlayer : collection) {
            this.entries.add(createPlayerUpdate(serverPlayer));
        }

    }

    public HostboundPlayerInfoPacket(FriendlyByteBuf friendlyByteBuf) {
        this.action = friendlyByteBuf.readEnum(ClientboundPlayerInfoPacket.Action.class);
        ClientboundPlayerInfoPacket.Action var10002 = this.action;
        Objects.requireNonNull(var10002);
        this.entries = friendlyByteBuf.readList(var10002::read);
    }

    private static ClientboundPlayerInfoPacket.PlayerUpdate createPlayerUpdate(ServerPlayer serverPlayer) {
        ProfilePublicKey profilePublicKey = serverPlayer.getProfilePublicKey();
        ProfilePublicKey.Data data = profilePublicKey != null ? profilePublicKey.data() : null;
        return new ClientboundPlayerInfoPacket.PlayerUpdate(serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName(), data);
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
