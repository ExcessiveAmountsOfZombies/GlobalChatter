package com.epherical.chatter.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * A collection of poorly named events.
 */
public class Events {

    public static final Event<PlayerInfo> PLAYER_INFO_EVENT = EventFactory.createArrayBacked(PlayerInfo.class, calls -> packet -> {
        for (PlayerInfo call : calls) {
            call.onPlayerInfo(packet);
        }
    });

    public static final Event<PlayerJoined> PLAYER_JOINED = EventFactory.createArrayBacked(PlayerJoined.class, calls -> (packet, player) -> {
        for (PlayerJoined call : calls) {
            call.onPlayerJoin(packet, player);
        }
    });

    public static final Event<BroadcastChat> BROADCAST_CHAT_EVENT = EventFactory.createArrayBacked(BroadcastChat.class, calls -> (message, type, uuid) -> {
        for (BroadcastChat call : calls) {
            call.onBroadcast(message, type, uuid);
        }
    });


    @FunctionalInterface
    public interface PlayerInfo {
        void onPlayerInfo(ClientboundPlayerInfoPacket packet);
    }

    @FunctionalInterface
    public interface PlayerJoined {
        void onPlayerJoin(ClientboundPlayerInfoPacket packet, ServerPlayer player);
    }

    @FunctionalInterface
    public interface BroadcastChat {
        void onBroadcast(Component message, ChatType type, UUID fromUUID);
    }
}
