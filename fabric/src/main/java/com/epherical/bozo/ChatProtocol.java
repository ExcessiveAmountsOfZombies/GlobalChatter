package com.epherical.bozo;

import com.epherical.bozo.packets.HostBoundPlayerChatPacket;
import com.epherical.bozo.packets.HostBoundSystemChatPacket;
import com.google.common.collect.Maps;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ConnectionProtocol.PacketSet;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayChatPreviewPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

import java.util.Map;

public class ChatProtocol {

    private static int increment = 0;

    public static final ChatProtocol CHAT_PROTOCOL;

    private final int id;
    private final Map<PacketFlow, ? extends ConnectionProtocol.PacketSet<?>> flows;

    public ChatProtocol(Builder builder) {
        this.id = increment++;
        this.flows = builder.flow;
    }



    public static class Builder {
        private Map<PacketFlow, ConnectionProtocol.PacketSet<?>> flow = Maps.newEnumMap(PacketFlow.class);

        public <T extends PacketListener> Builder addFlow(PacketFlow packetFlow, ConnectionProtocol.PacketSet<T> packetSet) {
            this.flow.put(packetFlow, packetSet);
            return this;
        }
    }

    static {
        CHAT_PROTOCOL = new ChatProtocol(new Builder()
                .addFlow(PacketFlow.CLIENTBOUND, new PacketSet<ClientGamePacketListener>()
                        .addPacket(ClientboundCustomChatCompletionsPacket.class, ClientboundCustomChatCompletionsPacket::new)
                        .addPacket(ClientboundSystemChatPacket.class, ClientboundSystemChatPacket::new)
                        .addPacket(ClientboundPlayerChatPacket.class, ClientboundPlayerChatPacket::new)
                        .addPacket(ClientboundPlayerChatHeaderPacket.class, ClientboundPlayerChatHeaderPacket::new)
                        .addPacket(ClientboundChatPreviewPacket.class, ClientboundChatPreviewPacket::new) // todo; this one likely is not be needed
                        .addPacket(ClientboundSetDisplayChatPreviewPacket.class, ClientboundSetDisplayChatPreviewPacket::new)
                        .addPacket(ClientboundDeleteChatPacket.class, ClientboundDeleteChatPacket::new)
                        .addPacket(ClientboundCustomPayloadPacket.class, ClientboundCustomPayloadPacket::new)
                        .addPacket(ClientboundDisconnectPacket.class, ClientboundDisconnectPacket::new)
                        .addPacket(ClientboundSetActionBarTextPacket.class, ClientboundSetActionBarTextPacket::new)
                        .addPacket(ClientboundCustomSoundPacket.class, ClientboundCustomSoundPacket::new)
                        .addPacket(ClientboundServerDataPacket.class, ClientboundServerDataPacket::new))
                .addFlow(PacketFlow.SERVERBOUND, new PacketSet<ServerGamePacketListener>()
                        // really could call it HOSTBOUND, they're both servers
                        .addPacket(HostBoundSystemChatPacket.class, HostBoundSystemChatPacket::new)
                        .addPacket(HostBoundPlayerChatPacket.class, HostBoundPlayerChatPacket::new)
                ));
    }


}
