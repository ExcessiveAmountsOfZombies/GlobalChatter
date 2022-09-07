package com.epherical.bozo;

import com.epherical.bozo.chat.ChatHost;
import com.epherical.bozo.chat.ChatListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.Connection;
import net.minecraft.util.LazyLoadedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class BozoFabric implements ModInitializer {

    public static final LazyLoadedValue<NioEventLoopGroup> GLOBAL_CHAT_EVENT_GROUP = new LazyLoadedValue<>(() -> {
        return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Global Chat IO #%d").setDaemon(true).build());
    });

    public static Supplier<Channel> CLIENT_CHANNEL;


    public static final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    public static final String IP_ARG = System.getProperty("glbl.chatter.ip", "127.0.0.1");
    public static final int PORT_ARG = Integer.getInteger("glbl.chatter.port", 8192);
    public static final boolean HOSTING = true;

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (HOSTING) {
                ChatHost.init(server);
            } else {
                ChatListener.init(server);
            }
        });
    }
}
