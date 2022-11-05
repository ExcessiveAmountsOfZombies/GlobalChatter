package com.epherical.chatter;

import com.epherical.chatter.event.Events;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.UUID;

public class FabricPlatform extends CommonPlatform<FabricPlatform> {

    public final EventHandler hostEventHandler;

    public FabricPlatform() {
        hostEventHandler = new EventHandler();
    }


    @Override
    public FabricPlatform getPlatform() {
        return this;
    }

    @Override
    public boolean isClientEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isServerEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public Path getRootConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("GlobalChatter");
    }

    @Override
    public void firePlayerInfoEvent(ClientboundPlayerInfoPacket packet) {
        Events.PLAYER_INFO_EVENT.invoker().onPlayerInfo(packet);
    }

    @Override
    public void firePlayerInfoJoin(ClientboundPlayerInfoPacket packet, ServerPlayer player) {
        Events.PLAYER_JOINED.invoker().onPlayerJoin(packet, player);
    }

    @Override
    public void fireBroadcastChat(Component message, ChatType type, UUID fromUUID) {
        Events.BROADCAST_CHAT_EVENT.invoker().onBroadcast(message, type, fromUUID);
    }

}
