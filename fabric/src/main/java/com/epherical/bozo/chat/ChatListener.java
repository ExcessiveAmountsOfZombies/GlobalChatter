package com.epherical.bozo.chat;

import com.epherical.bozo.ClientChatPacketListener;
import com.epherical.bozo.event.Events;
import com.epherical.bozo.netty.ModifiedDecoder;
import com.epherical.bozo.netty.ModifiedEncoder;
import com.epherical.bozo.packets.HostBoundPlayerChatPacket;
import com.epherical.bozo.packets.HostBoundSystemChatPacket;
import com.epherical.bozo.packets.HostboundPlayerChatHeaderPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
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

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import static com.epherical.bozo.BozoFabric.*;

public class ChatListener {

    public static void init(MinecraftServer server) {
        ChatConnection connection = new ChatConnection(PacketFlow.CLIENTBOUND);
        new Bootstrap().group(GLOBAL_CHAT_EVENT_GROUP.get()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(@NotNull Channel ch) throws Exception {
                try {
                    ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                    ch.config().setOption(ChannelOption.SO_KEEPALIVE, true);
                } catch (ChannelException ignored) {
                }
                CLIENT_CHANNEL = () -> ch;
                ch.pipeline()
                        .addLast("splitter", (new Varint21FrameDecoder()))
                        .addLast("decoder", (new ModifiedDecoder(PacketFlow.CLIENTBOUND)))
                        .addLast("prepender", (new Varint21LengthFieldPrepender()))
                        .addLast("encoder", (new ModifiedEncoder(PacketFlow.SERVERBOUND)))
                        .addLast("packet_handler", connection);
            }
        }).channel(NioSocketChannel.class).connect(IP_ARG, PORT_ARG).syncUninterruptibly();
        connection.setListener(new ClientChatPacketListener(connection, server));
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
                CLIENT_CHANNEL.get().connect(new InetSocketAddress(IP_ARG, PORT_ARG));
            }
        });
        ServerMessageEvents.GAME_MESSAGE.register((server1, message, overlay) -> {
            connection.send(new HostBoundSystemChatPacket(message, overlay));
        });
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            byte[] bytes = message.signedBody().hash().asBytes();
            connection.send(new HostboundPlayerChatHeaderPacket(message.signedHeader(), message.headerSignature(), bytes));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    connection.send(new HostBoundPlayerChatPacket(message, params.toNetwork(server.registryAccess())));
                }
            }, 1L);
        });
        Events.HEADER_EVENT.register((bytes, header, signature) -> {

        });
    }

}
