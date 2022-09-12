package com.epherical.chatter.events;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraftforge.eventbus.api.Event;

public class GatherPlayerInfo extends Event {

    private final ClientboundPlayerInfoPacket packet;

    public GatherPlayerInfo(ClientboundPlayerInfoPacket packet) {
        this.packet = packet;
    }

    public ClientboundPlayerInfoPacket getPacket() {
        return packet;
    }
}
