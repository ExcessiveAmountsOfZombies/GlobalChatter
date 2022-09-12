package com.epherical.chatter;

import com.epherical.chatter.chat.ChatConnection;
import com.epherical.chatter.chat.ChatHost;
import com.epherical.chatter.events.BroadcastChat;
import com.epherical.chatter.events.ChatHeaderEvent;
import com.epherical.chatter.events.GatherPlayerInfo;
import com.epherical.chatter.events.PlayerInfoJoined;
import com.epherical.chatter.mixin.ClientboundPlayerInfoAccessor;
import com.epherical.chatter.packets.handler.HostPacketHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ForgeHostHandler{
    private static final Logger LOGGER = LogUtils.getLogger();

    public ForgeHostHandler() {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ChatHost.init(event.getServer());
    }

    @SubscribeEvent
    public void onEndServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
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
        }
    }

    @SubscribeEvent
    public void onBroadcastChat(BroadcastChat event) {
        //byte[] bytes = message.signedBody().hash().asBytes();
        for (ChatConnection connection : CommonPlatform.connections) {
            //connection.send(new ClientboundPlayerChatHeaderPacket(message.signedHeader(), message.headerSignature(), bytes));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    connection.send(new ClientboundPlayerChatPacket(event.getMessage(), event.getNetwork()));
                }
            }, 1L);
        }
    }

    @SubscribeEvent
    public void onSystemChat(ServerChatEvent event) {
        // todo; might need mixin forge doesn't seem to have a game message event
    }

    @SubscribeEvent
    public void onPlayerInfo(GatherPlayerInfo event) {
        ClientboundPlayerInfoPacket packet = event.getPacket();
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
    }

    @SubscribeEvent
    public void onPlayerJoined(PlayerInfoJoined event) {
        ServerPlayer player = event.getPlayer();
        ClientboundPlayerInfoPacket packet = event.getPacket();
        ClientboundPlayerInfoAccessor accessor = (ClientboundPlayerInfoAccessor) packet;
        List<ClientboundPlayerInfoPacket.PlayerUpdate> playerUpdates = new ArrayList<>();
        for (ChatConnection connection : CommonPlatform.connections) {
            playerUpdates.addAll(connection.getCustomPacketListener().getPlayersFromOtherServers().values());
        }
        accessor.setEntries(playerUpdates);
        player.connection.send((Packet<?>) accessor);
    }

    @SubscribeEvent
    public void onChatHeader(ChatHeaderEvent event) {
        for (ChatConnection connection : CommonPlatform.connections) {
            connection.send(new ClientboundPlayerChatHeaderPacket(event.getHeader(), event.getSignature(), event.getBytes()));
        }
    }


}
