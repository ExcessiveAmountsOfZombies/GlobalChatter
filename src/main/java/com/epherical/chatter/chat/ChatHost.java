package com.epherical.chatter.chat;

import com.epherical.chatter.CommonPlatform;
import com.epherical.chatter.mixin.ClientboundPlayerInfoAccessor;
import com.epherical.chatter.netty.ModifiedDecoder;
import com.epherical.chatter.netty.ModifiedEncoder;
import com.epherical.chatter.packets.handler.HostPacketHandler;
import com.mojang.authlib.GameProfile;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.epherical.chatter.CommonPlatform.IP_ARG;
import static com.epherical.chatter.CommonPlatform.PORT_ARG;

public class ChatHost {

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
                        CommonPlatform.connections.add(connection);
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
                .group(CommonPlatform.GLOBAL_CHAT_EVENT_GROUP.get())
                .localAddress(IP_ARG, PORT_ARG)
                .bind()
                .syncUninterruptibly();

    }
}
