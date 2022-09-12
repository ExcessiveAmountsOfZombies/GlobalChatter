package com.epherical.chatter;

import com.epherical.chatter.chat.ChatConnection;
import com.epherical.chatter.chat.ChatHost;
import com.epherical.chatter.chat.ChatListener;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import static com.epherical.chatter.CommonPlatform.HOSTING;

public class ChatterFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CommonPlatform.create(new FabricPlatform());


        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (HOSTING) {
                ChatHost.init(server);
                EventHandler.registerHostListeners();
            } else {
                ChatConnection init = ChatListener.init(server);
                // ???
                EventHandler.registerClientListeners(init);
            }
        });
    }
}
