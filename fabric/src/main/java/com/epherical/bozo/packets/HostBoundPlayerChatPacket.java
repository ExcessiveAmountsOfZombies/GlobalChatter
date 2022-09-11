package com.epherical.bozo.packets;

import com.epherical.bozo.packets.handler.HostPacketHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

import java.util.Optional;

public class HostBoundPlayerChatPacket implements Packet<ServerGamePacketListener> {

    private final PlayerChatMessage playerChatMessage;
    private final ChatType.BoundNetwork boundNetwork;

    public HostBoundPlayerChatPacket(FriendlyByteBuf buf) {
        this(new PlayerChatMessage(buf), new ChatType.BoundNetwork(buf));
    }

    public HostBoundPlayerChatPacket(PlayerChatMessage playerChatMessage, ChatType.BoundNetwork boundNetwork) {
        this.playerChatMessage = playerChatMessage;
        this.boundNetwork = boundNetwork;
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        this.playerChatMessage.write(buffer);
        this.boundNetwork.write(buffer);
    }

    @Override
    public void handle(ServerGamePacketListener handler) {
        if (handler instanceof HostPacketHandler listener) {
            listener.handleHostChat(this);
        }
    }

    public Optional<ChatType.Bound> resolveChatType(RegistryAccess registryAccess) {
        return this.boundNetwork.resolve(registryAccess);
    }

    public ChatType.BoundNetwork getBoundNetwork() {
        return boundNetwork;
    }

    public PlayerChatMessage getPlayerChatMessage() {
        return playerChatMessage;
    }
}
