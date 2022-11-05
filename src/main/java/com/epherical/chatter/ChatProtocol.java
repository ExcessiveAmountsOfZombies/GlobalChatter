package com.epherical.chatter;

import com.epherical.chatter.packets.HostBoundPlayerChatPacket;
import com.epherical.chatter.packets.HostboundPlayerInfoPacket;
import com.epherical.chatter.packets.handler.HostPacketHandler;
import com.epherical.chatter.packets.handler.ListenerPacketHandler;
import com.google.common.collect.Maps;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ConnectionProtocol.PacketSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChatProtocol {

    private static int increment = 0;

    /**
     * Clientbound packets are handled in {@link ListenerPacketHandler} these listeners will just send to players on
     * the server
     * <br>
     * Hostbound packets are handled in {@link HostPacketHandler} Host listeners need to send to the players on their
     * server as well as to listeners
     */
    public static final ChatProtocol CHAT_PROTOCOL;

    private final int id;
    private final Map<PacketFlow, ? extends ConnectionProtocol.PacketSet<?>> flows;

    public ChatProtocol(Builder builder) {
        this.id = increment++;
        this.flows = builder.flow;
    }

    @Nullable
    public Integer getPacketId(PacketFlow direction, Packet<?> packet) {
        return this.flows.get(direction).getId(packet.getClass());
    }

    @Nullable
    public Packet<?> createPacket(PacketFlow direction, int packetId, FriendlyByteBuf buffer) {
        return this.flows.get(direction).createPacket(packetId, buffer);
    }

    public int getId() {
        return id;
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
                        // Handled in ListenerPacketHandler
                        .addPacket(ClientboundCustomPayloadPacket.class, ClientboundCustomPayloadPacket::new)
                        .addPacket(ClientboundDisconnectPacket.class, ClientboundDisconnectPacket::new)
                        .addPacket(ClientboundSetActionBarTextPacket.class, ClientboundSetActionBarTextPacket::new)
                        .addPacket(ClientboundCustomSoundPacket.class, ClientboundCustomSoundPacket::new)
                        .addPacket(ClientboundChatPacket.class, ClientboundChatPacket::new)
                        //.addPacket(ClientboundPlayerInfoPacket.class, ClientboundPlayerInfoPacket::new)
                ).addFlow(PacketFlow.SERVERBOUND, new PacketSet<ServerGamePacketListener>()
                        // Handled in HostPacketHandler
                        // really could call it HOSTBOUND, they're both servers
                        // HostBound packets are just re-implementations of Clientbound vanilla packets for sending information back to the host server.
                        .addPacket(HostBoundPlayerChatPacket.class, HostBoundPlayerChatPacket::new)
                        //.addPacket(HostboundPlayerInfoPacket.class, HostboundPlayerInfoPacket::new)
                ));
    }


}
