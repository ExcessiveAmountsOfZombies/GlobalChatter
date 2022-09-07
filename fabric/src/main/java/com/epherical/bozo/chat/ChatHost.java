package com.epherical.bozo.chat;

import com.epherical.bozo.ServerPacketListener;
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
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static com.epherical.bozo.BozoFabric.*;

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
                        } catch (ChannelException ignored) {
                        }
                        ch.pipeline()
                                .addLast("splitter", (new Varint21FrameDecoder()))
                                .addLast("decoder", (new PacketDecoder(PacketFlow.SERVERBOUND)))
                                .addLast("prepender", (new Varint21LengthFieldPrepender()))
                                .addLast("encoder", (new PacketEncoder(PacketFlow.CLIENTBOUND)));
                        Connection connection = new Connection(PacketFlow.SERVERBOUND);
                        connections.add(connection);
                        connection.setListener(new ServerPacketListener(connection));
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                connection.setProtocol(ConnectionProtocol.PLAY);
                            }
                        }, 5L);
                        ch.pipeline().addLast("packet_handler", connection);
                    }
                })
                .group(GLOBAL_CHAT_EVENT_GROUP.get())
                .localAddress(IP_ARG, PORT_ARG)
                .bind()
                .syncUninterruptibly();

        registerListeners(server);
    }

    private static void registerListeners(MinecraftServer mainServer) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            synchronized (connections) {
                for (Iterator<Connection> iterator = connections.iterator(); iterator.hasNext(); ) {
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

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            for (Connection connection : connections) {
                connection.send(new ClientboundPlayerChatPacket(message, params.toNetwork(mainServer.registryAccess())));
            }
        });

        ServerMessageEvents.GAME_MESSAGE.register((server1, message, overlay) -> {
            for (Connection connection : connections) {
                connection.send(new ClientboundSystemChatPacket(message, overlay));
            }
        });
    }

}
