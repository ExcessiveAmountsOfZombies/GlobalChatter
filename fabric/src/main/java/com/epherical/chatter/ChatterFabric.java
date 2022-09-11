package com.epherical.chatter;

import com.epherical.chatter.chat.ChatConnection;
import com.epherical.chatter.chat.ChatHost;
import com.epherical.chatter.chat.ChatListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.LazyLoadedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatterFabric implements DedicatedServerModInitializer {

    public static final LazyLoadedValue<NioEventLoopGroup> GLOBAL_CHAT_EVENT_GROUP = new LazyLoadedValue<>(() -> {
        return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Global Chat IO #%d").setDaemon(true).build());
    });


    public static final List<ChatConnection> connections = Collections.synchronizedList(new ArrayList<>());

    public static final String SERVER_NAME = System.getProperty("glbl.chatter.server-name", "Server");
    public static final String IP_ARG = System.getProperty("glbl.chatter.ip", "127.0.0.1");
    public static final int PORT_ARG = Integer.getInteger("glbl.chatter.port", 8192);
    public static final boolean HOSTING = Boolean.parseBoolean(System.getProperty("glbl.chatter.is_hosting", "false"));

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (HOSTING) {
                ChatHost.init(server);
            } else {
                ChatListener.init(server);
            }
        });
    }
}
