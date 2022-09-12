package com.epherical.chatter;

import com.epherical.chatter.chat.ChatConnection;
import com.epherical.chatter.chat.ChatListener;
import com.epherical.chatter.events.BroadcastChat;
import com.epherical.chatter.events.ChatHeaderEvent;
import com.epherical.chatter.events.GatherPlayerInfo;
import com.epherical.chatter.packets.HostBoundPlayerChatPacket;
import com.epherical.chatter.packets.HostboundPlayerChatHeaderPacket;
import com.epherical.chatter.packets.HostboundPlayerInfoPacket;
import com.mojang.logging.LogUtils;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import static com.epherical.chatter.chat.ChatListener.createConnection;
import static com.epherical.chatter.chat.ChatListener.tick;

public class ForgeClientHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ChatConnection connection;

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        connection = ChatListener.init(event.getServer());
    }


    @SubscribeEvent
    public void onEndTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            if (connection.isConnected()) {
                connection.tick();
            } else {
                if (tick % 60 == 0) {
                    try {
                        LOGGER.warn("Connection to Host global chat was disconnected, attempting reconnection");
                        createConnection(connection);
                    } catch (Exception ignored) {
                    }
                }
                tick++;
            }
        }
    }

    @SubscribeEvent
    public void serverChat(ServerChatEvent event) {
        // todo; need event or mixin or something
    }

    @SubscribeEvent
    public void broadcastChat(BroadcastChat event) {
        byte[] bytes = event.getMessage().signedBody().hash().asBytes();
        connection.send(new HostboundPlayerChatHeaderPacket(event.getMessage().signedHeader(), event.getMessage().headerSignature(), bytes));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                connection.send(new HostBoundPlayerChatPacket(event.getMessage(), event.getNetwork()));
            }
        }, 1L);
    }

    @SubscribeEvent
    public void playerInfo(GatherPlayerInfo info) {
        HostboundPlayerInfoPacket hostboundPlayerInfoPacket = new HostboundPlayerInfoPacket(info.getPacket().getAction(), info.getPacket().getEntries());
        connection.send(hostboundPlayerInfoPacket);
    }

    @SubscribeEvent
    public void headerEvent(ChatHeaderEvent event) {
        // nothing for now
    }


}
