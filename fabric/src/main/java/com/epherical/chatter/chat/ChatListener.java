package com.epherical.chatter.chat;

import com.epherical.chatter.packets.handler.ListenerPacketHandler;
import com.epherical.chatter.event.Events;
import com.epherical.chatter.netty.ModifiedDecoder;
import com.epherical.chatter.netty.ModifiedEncoder;
import com.epherical.chatter.packets.HostBoundPlayerChatPacket;
import com.epherical.chatter.packets.HostBoundSystemChatPacket;
import com.epherical.chatter.packets.HostboundPlayerChatHeaderPacket;
import com.epherical.chatter.packets.HostboundPlayerInfoPacket;
import com.epherical.chatter.ChatterFabric;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class ChatListener {

    private static long tick = 0;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static ChannelFuture createConnection(ChatConnection connection) {
        return new Bootstrap().group(ChatterFabric.GLOBAL_CHAT_EVENT_GROUP.get()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(@NotNull Channel ch) throws Exception {
                try {
                    ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                    ch.config().setOption(ChannelOption.SO_KEEPALIVE, true);
                } catch (ChannelException ignored) {
                }
                ch.pipeline()
                        .addLast("splitter", (new Varint21FrameDecoder()))
                        .addLast("decoder", (new ModifiedDecoder(PacketFlow.CLIENTBOUND)))
                        .addLast("prepender", (new Varint21LengthFieldPrepender()))
                        .addLast("encoder", (new ModifiedEncoder(PacketFlow.SERVERBOUND)))
                        // memory leak potential? the server would disconnect if it didn't have @Sharable
                        .addLast("packet_handler", connection);
            }
        }).channel(NioSocketChannel.class).connect(ChatterFabric.IP_ARG, ChatterFabric.PORT_ARG).syncUninterruptibly();
    }

    public static void init(MinecraftServer server) {
        ChatConnection connection = new ChatConnection(PacketFlow.CLIENTBOUND);
        createConnection(connection);
        connection.setListener(new ListenerPacketHandler(connection, server));
        // this is dirty and lame and probably not even necessary, but we do it anyways, just in case.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                connection.setProtocol(ConnectionProtocol.PLAY);
            }
        }, 1L);
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