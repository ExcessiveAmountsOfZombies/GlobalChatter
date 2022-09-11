package com.epherical.chatter.chat;

import com.epherical.chatter.packets.handler.HostPacketHandler;
import com.epherical.chatter.event.Events;
import com.epherical.chatter.mixin.ClientboundPlayerInfoAccessor;
import com.epherical.chatter.netty.ModifiedDecoder;
import com.epherical.chatter.netty.ModifiedEncoder;
import com.epherical.chatter.ChatterFabric;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatHost {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init(MinecraftServer server) {
        ChannelFuture future = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(@NotNull Channel ch) {
                        try {
                            ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                            ch.config().setOption(ChannelOption.SO_KEEPALIVE, true);
                        } catch (ChannelException ignored) {
                        }
                        ch.pipeline()
                                .addLast()
                                .addLast("splitter", (new Varint21FrameDecoder()))
                                .addLast("decoder", (new ModifiedDecoder(PacketFlow.SERVERBOUND)))
                                .addLast("prepender", (new Varint21LengthFieldPrepender()))
                                .addLast("encoder", (new ModifiedEncoder(PacketFlow.CLIENTBOUND)));
                        ChatConnection connection = new ChatConnection(PacketFlow.SERVERBOUND);
                        ChatterFabric.connections.add(connection);
                        connection.setListener(new HostPacketHandler(connection, server));
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                connection.setProtocol(ConnectionProtocol.PLAY);
                            }
                        }, 1L);
                        ch.pipeline().addLast("packet_handler", connection);
                    }
                })
                .group(ChatterFabric.GLOBAL_CHAT_EVENT_GROUP.get())
                .localAddress(ChatterFabric.IP_ARG, ChatterFabric.PORT_ARG)
                .bind()
                .syncUninterruptibly();

        registerListeners(server);
    }

    private static void registerListeners(MinecraftServer mainServer) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            synchronized (ChatterFabric.connections) {
                for (Iterator<ChatConnection> iterator = ChatterFabric.connections.iterator(); iterator.hasNext(); ) {
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
            for (ChatConnection connection : ChatterFabric.connections) {
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
            for (ChatConnection connection : ChatterFabric.connections) {
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
            for (ChatConnection connection : ChatterFabric.connections) {
                connection.send(packet);
            }
        });

        Events.PLAYER_JOINED.register((packet, player) -> {
            ClientboundPlayerInfoAccessor accessor = (ClientboundPlayerInfoAccessor) packet;
            List<ClientboundPlayerInfoPacket.PlayerUpdate> playerUpdates = new ArrayList<>();
            for (ChatConnection connection : ChatterFabric.connections) {
                playerUpdates.addAll(connection.getCustomPacketListener().getPlayersFromOtherServers().values());
            }
            accessor.setEntries(playerUpdates);
            player.connection.send((Packet<?>) accessor);
        });


        // I'm not actually sure if we need to send headers... testing with only two players
        // leads me to believe we don't
        Events.CHAT_HEADER_EVENT.register((bytes, header, signature) -> {
            for (ChatConnection connection : ChatterFabric.connections) {
                connection.send(new ClientboundPlayerChatHeaderPacket(header, signature, bytes));
            }
        });
    }

}
