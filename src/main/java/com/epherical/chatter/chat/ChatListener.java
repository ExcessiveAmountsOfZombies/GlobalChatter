package com.epherical.chatter.chat;

import com.epherical.chatter.CommonPlatform;
import com.epherical.chatter.netty.ModifiedDecoder;
import com.epherical.chatter.netty.ModifiedEncoder;
import com.epherical.chatter.packets.handler.ListenerPacketHandler;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import static com.epherical.chatter.CommonPlatform.IP_ARG;
import static com.epherical.chatter.CommonPlatform.PORT_ARG;

public class ChatListener {

    public static long tick = 0;

    private static final Logger LOGGER = LogUtils.getLogger();

    public static ChannelFuture createConnection(ChatConnection connection) {
        return new Bootstrap().group(CommonPlatform.GLOBAL_CHAT_EVENT_GROUP.get()).handler(new ChannelInitializer<>() {
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
        }).channel(NioSocketChannel.class).connect(IP_ARG, PORT_ARG).syncUninterruptibly();
    }

    public static ChatConnection init(MinecraftServer server) {
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
        return connection;
    }

}
