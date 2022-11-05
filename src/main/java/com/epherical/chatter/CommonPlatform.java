package com.epherical.chatter;

import com.epherical.chatter.chat.ChatConnection;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.LazyLoadedValue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class CommonPlatform<T> {

    public static final LazyLoadedValue<NioEventLoopGroup> GLOBAL_CHAT_EVENT_GROUP = new LazyLoadedValue<>(() -> {
        return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Global Chat IO #%d").setDaemon(true).build());
    });

    public static final List<ChatConnection> connections = Collections.synchronizedList(new ArrayList<>());

    public static final String SERVER_NAME = System.getProperty("glbl.chatter.server-name", "Server");
    public static final String IP_ARG = System.getProperty("glbl.chatter.ip", "127.0.0.1");
    public static final int PORT_ARG = Integer.getInteger("glbl.chatter.port", 8192);
    public static final boolean HOSTING = Boolean.parseBoolean(System.getProperty("glbl.chatter.is_hosting", "false"));

    public static CommonPlatform<?> platform;

    protected CommonPlatform() {
        platform = this;
    }

    public static <T> void create(CommonPlatform<T> value) {
        platform = value;
    }

    public abstract T getPlatform();

    public abstract boolean isClientEnvironment();

    public abstract boolean isServerEnvironment();

    public abstract Path getRootConfigPath();

    public abstract void firePlayerInfoEvent(ClientboundPlayerInfoPacket packet);

    public abstract void firePlayerInfoJoin(ClientboundPlayerInfoPacket packet, ServerPlayer player);

    public abstract void fireBroadcastChat(Component message, ChatType type, UUID fromUUID);

}
