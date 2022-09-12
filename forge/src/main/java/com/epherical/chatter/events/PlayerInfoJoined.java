package com.epherical.chatter.events;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class PlayerInfoJoined extends Event {

    private final ClientboundPlayerInfoPacket packet;
    private final ServerPlayer player;

    public PlayerInfoJoined(ClientboundPlayerInfoPacket packet, ServerPlayer player) {
        this.packet = packet;
        this.player = player;
    }

    public ClientboundPlayerInfoPacket getPacket() {
        return packet;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
