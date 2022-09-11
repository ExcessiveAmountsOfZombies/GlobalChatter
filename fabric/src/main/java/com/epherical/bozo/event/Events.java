package com.epherical.bozo.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * A collection of poorly named events.
 */
public class Events {

    public static final Event<ChatHeader> CHAT_HEADER_EVENT = EventFactory.createArrayBacked(ChatHeader.class, calls -> (bytes, header, signature) -> {
        for (ChatHeader call : calls) {
            call.onBroadcastHeader(bytes, header, signature);
        }
    });

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

    public static final Event<BroadcastChat> BROADCAST_CHAT_EVENT = EventFactory.createArrayBacked(BroadcastChat.class, calls -> (message, network) -> {
        for (BroadcastChat call : calls) {
            call.onBroadcast(message, network);
        }
    });


    @FunctionalInterface
    public interface ChatHeader {
        void onBroadcastHeader(byte[] bytes, SignedMessageHeader header, MessageSignature signature);
    }

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
        void onBroadcast(PlayerChatMessage message, ChatType.BoundNetwork network);
    }
}
