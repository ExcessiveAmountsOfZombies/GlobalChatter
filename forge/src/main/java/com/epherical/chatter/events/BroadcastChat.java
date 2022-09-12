package com.epherical.chatter.events;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraftforge.eventbus.api.Event;

public class BroadcastChat extends Event {

    private final PlayerChatMessage message;
    private final ChatType.BoundNetwork network;

    public BroadcastChat(PlayerChatMessage message, ChatType.BoundNetwork network) {
        this.message = message;
        this.network = network;
    }

    public ChatType.BoundNetwork getNetwork() {
        return network;
    }

    public PlayerChatMessage getMessage() {
        return message;
    }
}
