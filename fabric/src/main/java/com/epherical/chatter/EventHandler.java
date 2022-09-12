package com.epherical.chatter;

import com.epherical.chatter.chat.ChatConnection;
import com.epherical.chatter.event.Events;
import com.epherical.chatter.mixin.ClientboundPlayerInfoAccessor;
import com.epherical.chatter.packets.HostBoundPlayerChatPacket;
import com.epherical.chatter.packets.HostBoundSystemChatPacket;
import com.epherical.chatter.packets.HostboundPlayerChatHeaderPacket;
import com.epherical.chatter.packets.HostboundPlayerInfoPacket;
import com.epherical.chatter.packets.handler.HostPacketHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.epherical.chatter.chat.ChatListener.createConnection;
import static com.epherical.chatter.chat.ChatListener.tick;

public class EventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();


    public static void registerHostListeners() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            synchronized (CommonPlatform.connections) {
                for (Iterator<ChatConnection> iterator = CommonPlatform.connections.iterator(); iterator.hasNext(); ) {
                    Connection connection = iterator.next();
                    if (connection.isConnected()) {
                        try {
                            connection.tick();
                        } catch (Exception e) {
                            if (connection.isMemoryConnection()) {
                                throw new ReportedException(CrashReport.forThrowable(e, "Ticking memory connection"));
                            }
                            LOGGER.warn("Failed to handle packet for {}", connection.getRemoteAddress(), e);
                            Component component = Component.literal("Internal server error");
                            connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> {
                                connection.disconnect(component);
                            }));
                            connection.setReadOnly();
                        }
                    } else {
                        iterator.remove();
                        connection.handleDisconnection();
                    }
                }
            }
        });
        Events.BROADCAST_CHAT_EVENT.register((message, network) -> {
            //byte[] bytes = message.signedBody().hash().asBytes();
            for (ChatConnection connection : CommonPlatform.connections) {
                //connection.send(new ClientboundPlayerChatHeaderPacket(message.signedHeader(), message.headerSignature(), bytes));
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        connection.send(new ClientboundPlayerChatPacket(message, network));
                    }
                }, 1L);
            }
        });
        ServerMessageEvents.GAME_MESSAGE.register((server1, message, overlay) -> {
            for (ChatConnection connection : CommonPlatform.connections) {
                connection.send(new ClientboundSystemChatPacket(message, overlay));
            }
        });
        Events.PLAYER_INFO_EVENT.register(packet -> {
            if (packet.getAction() != ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
                for (ClientboundPlayerInfoPacket.PlayerUpdate entry : packet.getEntries()) {
                    GameProfile profile = entry.getProfile();
                    if (profile.getName() != null) {
                        HostPacketHandler.playersFromOtherServers.put(entry.getProfile(), entry);
                    }
                }
            } else {
                for (ClientboundPlayerInfoPacket.PlayerUpdate entry : packet.getEntries()) {
                    HostPacketHandler.playersFromOtherServers.remove(entry.getProfile());
                }
            }
            for (ChatConnection connection : CommonPlatform.connections) {
                connection.send(packet);
            }
        });
        Events.PLAYER_JOINED.register((packet, player) -> {
            ClientboundPlayerInfoAccessor accessor = (ClientboundPlayerInfoAccessor) packet;
            List<ClientboundPlayerInfoPacket.PlayerUpdate> playerUpdates = new ArrayList<>();
            for (ChatConnection connection : CommonPlatform.connections) {
                playerUpdates.addAll(connection.getCustomPacketListener().getPlayersFromOtherServers().values());
            }
            accessor.setEntries(playerUpdates);
            player.connection.send((Packet<?>) accessor);
        });
        // I'm not actually sure if we need to send headers... testing with only two players
        // leads me to believe we don't
        Events.CHAT_HEADER_EVENT.register((bytes, header, signature) -> {
            for (ChatConnection connection : CommonPlatform.connections) {
                connection.send(new ClientboundPlayerChatHeaderPacket(header, signature, bytes));
            }
        });
    }

    public static void registerClientListeners(ChatConnection connection) {
        ServerTickEvents.END_SERVER_TICK.register(server1 -> {
            if (connection.isConnected()) {
                connection.tick();
            } else {
                if (tick % 60 == 0) {
                    try {
                        LOGGER.warn("Connection to Host global chat was disconnected, attempting reconnection");
                        createConnection(connection);
                    } catch (Exception ignored) {}
                }
                tick++;
            }
        });
        ServerMessageEvents.GAME_MESSAGE.register((server1, message, overlay) -> {
            connection.send(new HostBoundSystemChatPacket(message, overlay));
        });

        Events.BROADCAST_CHAT_EVENT.register((message, network) -> {
            byte[] bytes = message.signedBody().hash().asBytes();
            connection.send(new HostboundPlayerChatHeaderPacket(message.signedHeader(), message.headerSignature(), bytes));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    connection.send(new HostBoundPlayerChatPacket(message, network));
                }
            }, 1L);
        });

        Events.PLAYER_INFO_EVENT.register(packet -> {
            HostboundPlayerInfoPacket hostboundPlayerInfoPacket = new HostboundPlayerInfoPacket(packet.getAction(), packet.getEntries());
            connection.send(hostboundPlayerInfoPacket);
        });

        // leave the header event here just in case.
        Events.CHAT_HEADER_EVENT.register((bytes, header, signature) -> {

        });

    }

}
