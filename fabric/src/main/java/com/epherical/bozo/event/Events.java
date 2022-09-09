package com.epherical.bozo.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;

public class Events {

    public static final Event<HeaderEvent> HEADER_EVENT = EventFactory.createArrayBacked(HeaderEvent.class, calls -> (bytes, header, signature) -> {
        for (HeaderEvent call : calls) {
            call.onBroadcastHeader(bytes, header, signature);
        }
    });

    public static final Event<PlayerInfo> PLAYER_INFO_EVENT = EventFactory.createArrayBacked(PlayerInfo.class, calls -> packet -> {
        for (PlayerInfo call : calls) {
            call.onPlayerInfo(packet);
        }
    });


    @FunctionalInterface
    public interface HeaderEvent {
        void onBroadcastHeader(byte[] bytes, SignedMessageHeader header, MessageSignature signature);
    }

    @FunctionalInterface
    public interface PlayerInfo {
        void onPlayerInfo(ClientboundPlayerInfoPacket packet);
    }
}
